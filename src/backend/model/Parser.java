package backend.model;

import backend.exception.InvalidInputException;
import backend.exception.ParserException;

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
     *                 (opsional) baris exit atas (mengandung satu 'K' dan spasi/dot, length ≤ COLS)
     *                 ROWS baris grid (length=COLS or COLS+1 jika exit samping)
     *                 (opsional) baris exit bawah (mengandung satu 'K' dan spasi/dot, length ≤ COLS)
     *
     * @param filePath path ke file input
     * @return Board terbangun
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

            // 2) Validasi jumlah Car
            br.mark(1024);
            String countLine = br.readLine();
            if (countLine == null) throw new InvalidInputException("Tidak ada baris jumlah mobil");
            countLine = countLine.trim();
            if (!countLine.matches("\\d+")) {
                throw new InvalidInputException("Baris kedua harus angka jumlah mobil");
            }
            int declaredCars = Integer.parseInt(countLine);
            if (declaredCars < 1 || declaredCars > 24) {
                throw new InvalidInputException(
                    "Jumlah mobil harus antara 1 dan 24, ditemukan: " + declaredCars);
            }

            // 3) Cek exit di atas (baris dengan ≤cols char, tepat satu 'K')
            br.mark(1024);
            String firstRaw = br.readLine();
            int exitRow = Integer.MIN_VALUE, exitCol = -1;
            boolean exitAbove = false;
            if (firstRaw != null) {
                long countK = firstRaw.chars().filter(ch -> ch == 'K').count();
                boolean validChars = firstRaw.chars()
                    .allMatch(ch -> ch == 'K' || ch == '.' || Character.isWhitespace(ch));
                if (countK == 1 && validChars && firstRaw.length() <= cols) {
                    exitRow = -1;
                    exitCol = firstRaw.indexOf('K');
                    exitAbove = true;
                }
            }
            if (!exitAbove) br.reset();

            // 4) Baca rows baris grid (dengan kemungkinan exit samping)
            List<String> rawGrid = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
                String line = br.readLine();
                if (line == null)
                    throw new InvalidInputException("Grid kurang dari " + rows + " baris");
                
                // hilangkan whitespace internal
                String compact = line.replaceAll("\\s+", "");
                
                if (compact.length() == cols) {
                    // Check for internal exit 'K'
                    if (compact.contains("K")) {
                        if (compact.chars().filter(ch -> ch == 'K').count() != 1) {
                            throw new InvalidInputException("Grid harus mengandung tepat satu 'K'");
                        }
                        exitRow = r;
                        exitCol = compact.indexOf('K');
                        compact = compact.replace('K', '.');  // Replace K with empty
                    }
                    rawGrid.add(compact);
                } 
                else if (compact.length() == cols + 1) {
                    // exit samping: satu K di tepi
                    long countK = compact.chars().filter(ch -> ch == 'K').count();
                    if (countK != 1)
                        throw new InvalidInputException(
                            "Baris grid ke-" + (r+1) + " ekstra harus tepat satu 'K'");
                    int kIdx = compact.indexOf('K');
                    if (kIdx == 0) {
                        exitRow = r;
                        exitCol = -1;
                        rawGrid.add(compact.substring(1));
                    } else if (kIdx == cols) {
                        exitRow = r;
                        exitCol = cols;
                        rawGrid.add(compact.substring(0, cols));
                    } else {
                        throw new InvalidInputException(
                            "Baris grid ke-" + (r+1) + " ekstra 'K' harus di tepi");
                    }
                } 
                else {
                    throw new InvalidInputException(
                        "Panjang baris grid ke-" + (r+1) +
                        " harus " + cols + " atau " + (cols+1));
                }
            }

            // 5) Cek exit di bawah (baris dengan ≤cols char, tepat satu 'K')
            br.mark(1024);
            String lastRaw = br.readLine();
            boolean exitBelow = false;
            if (lastRaw != null) {
                long countK = lastRaw.chars().filter(ch -> ch == 'K').count();
                boolean validChars = lastRaw.chars()
                    .allMatch(ch -> ch == 'K' || ch == '.' || Character.isWhitespace(ch));
                if (countK == 1 && validChars && lastRaw.length() <= cols) {
                    exitRow = rows;
                    exitCol = lastRaw.indexOf('K');
                    exitBelow = true;
                }
            }
            if (!exitBelow) br.reset();

            // 6) Validasi exit ditemukan
            if (exitRow == Integer.MIN_VALUE) {
                throw new InvalidInputException(
                    "Tidak ditemukan exit 'K' di atas, samping, atau bawah");
            }

            // 7) Kumpulkan posisi tiap mobil
            Map<Character, List<int[]>> posMap = new HashMap<>();
            char[][] grid = new char[rows][cols];
            for (int r = 0; r < rows; r++) {
                String rowStr = rawGrid.get(r);
                grid[r] = rowStr.toCharArray();
                for (int c = 0; c < cols; c++) {
                    char ch = grid[r][c];
                    if (ch == '.' || ch == 'K') continue;
                    posMap.computeIfAbsent(ch, k -> new ArrayList<>())
                          .add(new int[]{r, c});
                }
            }

            // 8) Bangun daftar Car
            List<Car> cars = new ArrayList<>();
            for (var e : posMap.entrySet()) {
                char id = e.getKey();
                List<int[]> coords = e.getValue();
                int length = coords.size();
                
                // 1x1 mobil tidak valid karena tidak memiliki arah
                if (length == 1) {
                    throw new InvalidInputException("Mobil dengan ukuran 1x1 tidak diperbolehkan.");
                }

                int minR = coords.stream().mapToInt(p -> p[0]).min().getAsInt();
                int minC = coords.stream().mapToInt(p -> p[1]).min().getAsInt();
                boolean horiz = coords.stream().allMatch(p -> p[0] == minR);
                
                cars.add(new Car(id, horiz, length, minR, minC));
            }
            
            if (cars.size() - (posMap.containsKey('P') ? 1 : 0) != declaredCars) {
                throw new InvalidInputException(
                    "Jumlah mobil terdeteksi (" + cars.size() +
                    ") tidak sesuai deklarasi (" + declaredCars + ")");
            }
            
            // 8.5) Validasi exit sejajar dengan mobil player (P)
            Car playerCar = cars.stream()
                .filter(car -> car.getId() == 'P')
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("Mobil pemain (P) tidak ditemukan"));

            boolean exitAligned = false;
            if (playerCar.isHorizontal()) {
                int playerRow = playerCar.getRow();
                exitAligned = (exitRow == playerRow);
            } else {
                int playerCol = playerCar.getCol();
                exitAligned = (exitCol == playerCol);
            }

            if (!exitAligned) {
                throw new InvalidInputException(
                    "Pintu keluar (K) harus sejajar dengan mobil pemain (P): " +
                    (playerCar.isHorizontal() ? "horizontal" : "vertikal"));
            }

            // 9) Generate Zobrist & kembalikan Board
            long[][][] zTable = generateZobristTable(rows, cols);
            return new Board(rows, cols, cars, zTable, exitRow, exitCol);
        }
    }

    /** Zobrist table [rows][cols][A–Z] */
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

    /**
     * Parses a file and returns a Board.
     * Wrapper method for compatibility with GUI.
     * 
     * @param filePath Path to the input file
     * @return A Board object
     * @throws ParserException if parsing fails
     */
    public static Board parseFile(String filePath) throws backend.exception.ParserException {
        try {
            return parse(filePath);
        } catch (IOException | InvalidInputException e) {
            throw new ParserException("Error parsing file: " + e.getMessage(), e);
        }
    }
}