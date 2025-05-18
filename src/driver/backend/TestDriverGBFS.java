package test.backend;

import java.io.IOException;
import java.util.List;

import backend.algorithm.PathfindingAlgorithm;
import backend.algorithm.GBFS;
import backend.exception.InvalidInputException;
import backend.model.Board;
import backend.model.Parser;

public class TestDriverGBFS {
    public static void main(String[] args) {
        String filePath = "src/test/model/board.txt"; // sesuaikan path file input Anda

        try {
            // Parse board awal
            Board initialBoard = Parser.parse(filePath);

            System.out.println("Initial Board:");
            System.out.println(initialBoard);

            // Buat instance GBFS
            PathfindingAlgorithm GBFS = new GBFS(new backend.util.HeuristicManhattan());

            // Jalankan solve
            List<Board> solutionPath = GBFS.solve(initialBoard, new backend.util.HeuristicManhattan());

            // Tampilkan hasil
            if (solutionPath.isEmpty()) {
                System.out.println("No solution found.");
            } else {
                System.out.printf("Solution found in %d steps. Nodes visited: %d. Execution time: %d ms.%n",
                        solutionPath.size() - 1, // langkah perpindahan
                        GBFS.getNodesVisited(),
                        GBFS.getExecutionTime());

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
