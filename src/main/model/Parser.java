package main.model;

import main.exception.InvalidInputException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {

    /**
     * Parse file .txt menjadi Board, mendeteksi exit 'K' di atas/samping/bawah.
     *
     * Format:
     * Baris 1        : ROWS COLS
     * Baris berikut : (opsional) count mobil (angka saja)
     *                 (opsional) baris exit atas (panjang=COLS dengan satu 'K')
     *                 ROWS baris grid (length=COLS or COLS+1 jika exit samping)
     *                 (opsional) baris exit bawah (panjang=COLS dengan satu 'K')
     *
     * @param filePath path ke file input
     * @return Board terbangun
     * @throws IOException               jika I/O gagal
     * @throws InvalidInputException    jika format salah
     */
    @SuppressWarnings("unused")
    public static Board parse(String filePath)
            throws IOException, InvalidInputException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // 1) Baca dimensi
            String header = br.readLine();
            if (header == null) throw new InvalidInputException("File kosong");
            String[] dims = header.trim().split("\\s+");
            if (dims.length != 2)
                throw new InvalidInputException("Baris dimensi harus berisi 2 angka");
            int rows = Integer.parseInt(dims[0]);
            int cols = Integer.parseInt(dims[1]);

            // 2) Abaikan baris count jika ada
            br.mark(1024);
            String maybeCount = br.readLine();
            if (maybeCount == null) throw new InvalidInputException("Tidak ada grid");
            boolean skippedCount = maybeCount.trim().matches("\\d+");
            if (!skippedCount) br.reset();

            // 3) Cek exit di atas (panjang=cols, satu K)
            br.mark(1024);
            String first = br.readLine();
            first = (first != null ? first.replaceAll("\\s+", "") : null);
            int exitRow = Integer.MIN_VALUE, exitCol = -1;
            List<String> rawGrid = new ArrayList<>();

            if (first != null
                    && first.length() == cols
                    && first.chars().filter(ch -> ch == 'K').count() == 1) {
                // exit di atas grid
                exitRow = -1;
                exitCol = first.indexOf('K');
            } else {
                // rollback jika bukan baris exit
                br.reset();
            }

            // 4) Baca persis rows baris grid (termasuk kemungkinan exit samping)
            for (int r = 0; r < rows; r++) {
                String line = br.readLine();
                if (line == null)
                    throw new InvalidInputException("Grid kurang dari " + rows + " baris");
                line = line.replaceAll("\\s+", "");
                if (line.length() == cols) {
                    rawGrid.add(line);
                } else if (line.length() == cols + 1) {
                    // exit samping: K di index 0 atau index cols
                    long countK = line.chars().filter(ch -> ch == 'K').count();
                    if (countK != 1)
                        throw new InvalidInputException(
                            "Baris ke-" + (r + 1) + " panjang " + line.length() +
                            ", harus ada tepat satu 'K' jika ekstra");
                    int idxK = line.indexOf('K');
                    if (idxK == 0) {
                        exitRow = r;
                        exitCol = -1;
                        rawGrid.add(line.substring(1));
                    } else if (idxK == cols) {
                        exitRow = r;
                        exitCol = cols;
                        rawGrid.add(line.substring(0, cols));
                    } else {
                        throw new InvalidInputException(
                            "Baris ke-" + (r + 1) + " ekstra 'K' harus di tepi");
                    }
                } else {
                    throw new InvalidInputException(
                        "Panjang baris ke-" + (r + 1) + " harus " + cols +
                        " atau " + (cols + 1));
                }
            }

            // 5) Cek exit di bawah (panjang=cols, satu K)
            br.mark(1024);
            String last = br.readLine();
            last = (last != null ? last.replaceAll("\\s+", "") : null);
            if (last != null
                    && last.length() == cols
                    && last.chars().filter(ch -> ch == 'K').count() == 1) {
                exitRow = rows;
                exitCol = last.indexOf('K');
            } else {
                br.reset();
            }

            if (exitRow == Integer.MIN_VALUE) {
                throw new InvalidInputException(
                    "Tidak ditemukan exit 'K' di atas, samping, atau bawah");
            }

            // 6) Bangun grid char[][] dan kumpulkan posisi mobil
            char[][] grid = new char[rows][cols];
            Map<Character, List<int[]>> posMap = new HashMap<>();
            for (int r = 0; r < rows; r++) {
                String s = rawGrid.get(r);
                grid[r] = s.toCharArray();
                for (int c = 0; c < cols; c++) {
                    char ch = grid[r][c];
                    if (ch == '.') continue;
                    posMap.computeIfAbsent(ch, k -> new ArrayList<>())
                          .add(new int[]{r, c});
                }
            }

            // 7) Buat list Car dari posMap
            List<Car> cars = new ArrayList<>();
            for (var e : posMap.entrySet()) {
                char id = e.getKey();
                List<int[]> coords = e.getValue();
                int length = coords.size();
                int minR = coords.stream().mapToInt(p -> p[0]).min().getAsInt();
                int minC = coords.stream().mapToInt(p -> p[1]).min().getAsInt();
                boolean horiz = coords.stream().allMatch(p -> p[0] == minR);
                cars.add(new Car(id, horiz, length, minR, minC));
            }

            // 8) Generate Zobrist table & buat Board
            long[][][] zTable = generateZobristTable(rows, cols);
            return new Board(rows, cols, cars, zTable, exitRow, exitCol);
        }
    }

    /**
     * Buat tabel Zobrist [rows][cols][26] untuk 'A'..'Z'.
     * @param rows jumlah baris grid
     * @param cols jumlah kolom grid
     */
    private static long[][][] generateZobristTable(int rows, int cols) {
        Random rnd = new Random(0);
        long[][][] table = new long[rows][cols][26];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                for (int k = 0; k < 26; k++) {
                    table[r][c][k] = rnd.nextLong();
                }
            }
        }
        return table;
    }
}
