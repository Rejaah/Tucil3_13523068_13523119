package driver.model;

import backend.model.Board;
// import backend.model.Car;
import backend.model.Parser;
import backend.exception.InvalidInputException;

import java.io.IOException;

public class TestDriver {
    public static void main(String[] args) {
        String path = "src/test/model/board.txt";  // Ubah path sesuai lokasi file Anda
        try {
            // 1) Parse input
            Board board = Parser.parse(path);

            // 2) Tampilkan papan awal
            System.out.println("Initial Board:");
            System.out.println(board);

            // 3) Tampilkan posisi exit
            System.out.printf("Exit at (%d,%d)%n%n",
                board.getExitRow(), board.getExitCol());

            // 4) Pindah satu langkah mobil 'X' (delta = -1)
            char targetId = 'I';
            int delta = -1;
            Board moved = board.applyMove(targetId, delta, board.getZobristTable());

            // 5) Tampilkan hasil setelah gerakan
            System.out.printf("After moving '%c' by %+d:%n", targetId, delta);
            System.out.println(moved);

            targetId = 'F';
            delta = 3;
            moved = moved.applyMove(targetId, delta, board.getZobristTable());

            // 5) Tampilkan hasil setelah gerakan
            System.out.printf("After moving '%c' by %+d:%n", targetId, delta);
            System.out.println(moved);

        } catch (IOException | InvalidInputException e) {
            System.err.println("Error during parsing or applying move:");
            e.printStackTrace();
        }
    }
}
