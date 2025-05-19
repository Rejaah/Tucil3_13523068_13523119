package backend.util;

import backend.model.Board;
import backend.model.Car;

import java.io.*;
import java.util.List;

public class SolutionExporter {

    public static boolean exportSolution(List<Board> boardSequence, String algorithmName, String heuristicName,
                                         int totalVisited, long durationMs, File destination) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
            writer.write("Algoritma: " + algorithmName + "\n");
            writer.write("Heuristik: " + heuristicName + "\n");
            writer.write("Nodes dikunjungi: " + totalVisited + "\n");
            writer.write("Waktu eksekusi: " + durationMs + " ms\n\n");

            // Write initial board
            writer.write("Papan Awal\n");
            writer.write(boardSequence.get(0).toString() + "\n");

            // Write step-by-step moves
            for (int step = 1; step < boardSequence.size(); step++) {
                Board before = boardSequence.get(step - 1);
                Board after = boardSequence.get(step);

                String moveDescription = describeMove(before, after);
                writer.write("Langkah " + step + ": " + moveDescription + "\n");
                writer.write(after.toString() + "\n");
            }

            return true;
        }
    }

    private static String describeMove(Board before, Board after) {
        for (Car oldCar : before.getCars()) {
            Car newCar = findCarById(after, oldCar.getId());

            if (newCar != null) {
                if (oldCar.getRow() != newCar.getRow() || oldCar.getCol() != newCar.getCol()) {
                    String direction;
                    if (oldCar.isHorizontal()) {
                        direction = newCar.getCol() > oldCar.getCol() ? "kanan" : "kiri";
                    } else {
                        direction = newCar.getRow() > oldCar.getRow() ? "bawah" : "atas";
                    }
                    return oldCar.getId() + "-" + direction;
                }
            }
        }

        return "tidak diketahui";
    }

    private static Car findCarById(Board board, char carId) {
        for (Car car : board.getCars()) {
            if (car.getId() == carId) {
                return car;
            }
        }
        return null;
    }
}
