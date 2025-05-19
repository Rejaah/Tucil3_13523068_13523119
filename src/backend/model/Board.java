package backend.model;

import java.util.*;

public class Board {
    private final int rows;
    private final int cols;
    private final Map<Character, Car> cars;
    private final char[][] grid;
    private final long[][][] zobristTable;
    private final int exitRow, exitCol;
    private final long zobristKey;

    public Board(int rows, int cols, List<Car> cars, long[][][] zobristTable, int exitRow, int exitCol) {
        this.rows = rows;
        this.cols = cols;
        this.zobristTable = zobristTable;
        this.cars = new HashMap<>();
        this.grid = new char[rows][cols];

        for (char[] row : grid) {
            Arrays.fill(row, '.');
        }

        for (Car c : cars) {
            this.cars.put(c.getId(), c);
            placeOnGrid(c);
        }

        this.zobristKey = computeZobrist(zobristTable);
        this.exitRow = exitRow;
        this.exitCol = exitCol;
    }

    private void placeOnGrid(Car c) {
        int r = c.getRow(), c0 = c.getCol();
        for (int i = 0; i < c.getLength(); i++) {
            grid[r + (c.isHorizontal() ? 0 : i)]
                [c0 + (c.isHorizontal() ? i : 0)] = c.getId();
        }
    }

    public List<Board> generateNeighbors(long[][][] zobristTable) {
        List<Board> neighbors = new ArrayList<>();
        for (Car c : cars.values()) {
            // Gerak mundur hingga mentok
            int delta = -1;
            int lastValidDelta = 0;
            while (canMove(c, delta)) {
                lastValidDelta = delta;
                delta--;
            }
            if (lastValidDelta != 0) {
                neighbors.add(applyMove(c.getId(), lastValidDelta, zobristTable));
            }

            // Gerak maju
            delta = 1;
            lastValidDelta = 0;
            while (canMove(c,delta)) {
                lastValidDelta = delta;
                delta++;
            }
            if (lastValidDelta != 0) {
                neighbors.add(applyMove(c.getId(), lastValidDelta, zobristTable));
            }
        }
        return neighbors;
    }

    private boolean canMove(Car c, int delta) {
        int newR = c.getRow() + (c.isHorizontal() ? 0 : delta);
        int newC = c.getCol() + (c.isHorizontal() ? delta : 0);
        int endR = newR + (c.isHorizontal() ? 0 : c.getLength() - 1);
        int endC = newC + (c.isHorizontal() ? c.getLength() - 1 : 0);

        if (newR < 0 || newC < 0 || endR >= rows || endC >= cols) {
            return false;
        }

        // Cek tabrakan dengan mobil lain
        // Buat set posisi yang ditempati mobil c sebelum pindah
        Set<String> oldPositions = new HashSet<>();
        int r0 = c.getRow();
        int c0 = c.getCol();
        for (int i = 0; i < c.getLength(); i++) {
            int rr = r0 + (c.isHorizontal() ? 0 : i);
            int cc = c0 + (c.isHorizontal() ? i : 0);
            oldPositions.add(rr + "," + cc);
        }

        // Periksa semua posisi baru yang akan ditempati mobil c
        for (int i = 0; i < c.getLength(); i++) {
            int rr = newR + (c.isHorizontal() ? 0 : i);
            int cc = newC + (c.isHorizontal() ? i : 0);
            // Jika sel ini sudah ditempati oleh mobil lain, return false
            char cell = grid[rr][cc];
            if (cell != '.' && !oldPositions.contains(rr + "," + cc)) {
                return false;
            }
        }

        return true;
    }

    public Board applyMove(char carId, int delta, long[][][] zobristTable) {
        List<Car> newCars = new ArrayList<>();
        for (Car c : cars.values()) newCars.add(c.copy());

        for (Car c : newCars) {
            if (c.getId() == carId) {
                c.move(delta);
                break;
            }
        }
        return new Board(rows, cols, newCars, zobristTable, exitRow, exitCol);
    }

    private long computeZobrist(long [][][] zobristTable) {
        long h = 0L;
        for (Car c : cars.values()) {
            int r = c.getRow();
            int c0 = c.getCol();
            int len = c.getLength();
            int idx = c.getId() - 'A';
            
            for (int i = 0; i < len; i++) {
                int rr = r + (c.isHorizontal() ? 0 : i);
                int cc = c0 + (c.isHorizontal() ? i : 0);
                h ^= zobristTable[rr][cc][idx];
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board)) return false;
        Board board = (Board) o;
        return this.zobristKey == board.zobristKey;
    }

    public boolean isGoal() {
        Car player = cars.get('P');
        if (player == null) return false;

        if (player.isHorizontal()) {
            if (getExitCol() < 0) {
                int leftEndCol = player.getCol() - 1;
                return (player.getRow() == exitRow) && (leftEndCol == exitCol);
            } else {
                int rightEndCol = player.getCol() + player.getLength();
                return (player.getRow() == exitRow) && (rightEndCol == exitCol);
            }
        } else {
            if (getExitRow() < 0) {
                int topEndRow = player.getRow() - 1;
                return (topEndRow == exitRow) && (player.getCol() == exitCol);
            } else {
                int bottomEndRow = player.getRow() + player.getLength();
                return (bottomEndRow == exitRow) && (player.getCol() == exitCol);        
            }
        }
    }

    public void printDebugInfo() {
        System.out.println("\n[DEBUG BOARD STATE]");
        System.out.println("Exit position: (" + exitRow + "," + exitCol + ")");
        System.out.println("Zobrist key: " + zobristKey);
        System.out.println("Cars positions:");
        
        cars.forEach((id, car) -> {
            System.out.printf("  %s: %s\n", id, car.toString());
        });
        
        System.out.println("\nCurrent grid:");
        System.out.println(this.toString());
    }

    public int hashCode() {
        return Long.hashCode(zobristKey);
    }

    public Car getCar(char id) {
        return cars.get(id).copy();
    }

    public int getExitRow() {
        return exitRow;
    }

    public int getExitCol() {
        return exitCol;
    }

    public long[][][] getZobristTable() {
        return zobristTable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            for (char cell : row) {
                sb.append(cell).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Gets all cars in the board.
     * @return List of all cars
     */
    public List<Car> getCars() {
        return new ArrayList<>(cars.values());
    }

    /**
     * Gets the number of rows in the board.
     * @return Number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the board.
     * @return Number of columns
     */
    public int getCols() {
        return cols;
    }
}