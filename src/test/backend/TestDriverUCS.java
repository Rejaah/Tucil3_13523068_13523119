package test.backend;

import backend.algorithm.UCS;
import backend.algorithm.PathfindingAlgorithm;
import backend.model.Board;
import backend.model.Parser;
import backend.exception.InvalidInputException;

import java.io.IOException;
import java.util.List;

public class TestDriverUCS {
    public static void main(String[] args) {
        String filePath = "src/test/model/board.txt"; // sesuaikan path file input Anda

        try {
            // Parse board awal
            Board initialBoard = Parser.parse(filePath);

            System.out.println("Initial Board:");
            System.out.println(initialBoard);

            // Buat instance UCS
            PathfindingAlgorithm ucs = new UCS();

            // Jalankan solve
            List<Board> solutionPath = ucs.solve(initialBoard, new backend.util.HeuristicManhattan());

            // Tampilkan hasil
            if (solutionPath.isEmpty()) {
                System.out.println("No solution found.");
            } else {
                System.out.printf("Solution found in %d steps. Nodes visited: %d. Execution time: %d ms.%n",
                        solutionPath.size() - 1, // langkah perpindahan
                        ucs.getNodesVisited(),
                        ucs.getExecutionTime());

                System.out.println("Path:");
                int step = 0;
                for (Board b : solutionPath) {
                    System.out.printf("Step %d:%n%s%n", step++, b);
                }
            }

        } catch (IOException | InvalidInputException e) {
            System.err.println("Error during parsing or solving:");
            e.printStackTrace();
        }
    }
}
