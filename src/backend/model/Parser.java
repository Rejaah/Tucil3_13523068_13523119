package backend.model;

import backend.exception.InvalidInputException;
import backend.exception.ParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {

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

            validateCarConnections(posMap);

            // 8) Bangun daftar Car
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
            
            if (cars.size() - (posMap.containsKey('P') ? 1 : 0) != declaredCars) {
                int act = cars.size()-1;
                throw new InvalidInputException(
                    "Jumlah mobil terdeteksi (" + (cars.size() - 1) +
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

    private static void validateCarConnections(Map<Character, List<int[]>> posMap) 
        throws InvalidInputException {
        
        for (Map.Entry<Character, List<int[]>> entry : posMap.entrySet()) {
            char id = entry.getKey();
            List<int[]> positions = entry.getValue();
            
            List<Set<int[]>> components = new ArrayList<>();
            for (int[] pos : positions) {
                boolean connected = false;
                
                for (Set<int[]> component : components) {
                    for (int[] existing : component) {
                        if (isAdjacent(pos, existing)) {
                            component.add(pos);
                            connected = true;
                            break;
                        }
                    }
                    if (connected) break;
                }
                
                if (!connected) {
                    Set<int[]> newComponent = new HashSet<>();
                    newComponent.add(pos);
                    components.add(newComponent);
                }
            }
            
            if (components.size() > 1) {
                throw new InvalidInputException(
                    "Mobil " + id + " terdeteksi di " + components.size() + 
                    " lokasi terpisah.");
            }
            
            validateCarShape(id, positions);
        }
    }


    private static boolean isAdjacent(int[] pos1, int[] pos2) {
        return (Math.abs(pos1[0] - pos2[0]) == 1 && pos1[1] == pos2[1]) ||  
            (Math.abs(pos1[1] - pos2[1]) == 1 && pos1[0] == pos2[0]);
    }

    private static void validateCarShape(char id, List<int[]> positions) 
        throws InvalidInputException {
        
        if (positions.size() == 1) {
            throw new InvalidInputException(
                "Mobil " + id + " berukuran 1x1 tidak diperbolehkan");
        }
        
        boolean allRowsSame = positions.stream().mapToInt(p -> p[0]).distinct().count() == 1;
        boolean allColsSame = positions.stream().mapToInt(p -> p[1]).distinct().count() == 1;
        
        if (!allRowsSame && !allColsSame) {
            throw new InvalidInputException(
                "Mobil " + id + " tidak membentuk garis lurus.");
        }
        
        if (allRowsSame) {
            List<int[]> sorted = positions.stream()
                .sorted(Comparator.comparingInt(p -> p[1]))
                .toList();
                
            for (int i = 1; i < sorted.size(); i++) {
                if (sorted.get(i)[1] != sorted.get(i-1)[1] + 1) {
                    throw new InvalidInputException(
                        "Mobil " + id + " terputus");
                }
            }
        }
        else {
            List<int[]> sorted = positions.stream()
                .sorted(Comparator.comparingInt(p -> p[0]))
                .toList();
                
            for (int i = 1; i < sorted.size(); i++) {
                if (sorted.get(i)[0] != sorted.get(i-1)[0] + 1) {
                    throw new InvalidInputException(
                        "Mobil " + id + " terputus");
                }
            }
        }
    }

    public static Board parseFile(String filePath) throws backend.exception.ParserException {
        try {
            return parse(filePath);
        } catch (IOException | InvalidInputException e) {
            throw new ParserException("Parsing file: " + e.getMessage(), e);
        }
    }
}