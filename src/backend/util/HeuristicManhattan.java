package backend.util;

import backend.model.Board;
import backend.model.Car;

public class HeuristicManhattan implements Heuristic {
    @Override
    public int estimate(Board board) {
        // Dapatkan posisi mobil utama 'P'
        Car player = board.getCar('P');
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        // Jika mobil utama horizontal
        if (player.isHorizontal()) {
            int rightmostCol = player.getCol() + player.getLength() - 1;
            return Math.abs(player.getRow() - exitRow) + Math.abs(rightmostCol - exitCol);
        } else {
            // Jika mobil utama vertikal
            int bottomRow = player.getRow() + player.getLength() - 1;
            return Math.abs(player.getCol() - exitCol) + Math.abs(bottomRow - exitRow);
        }
    }

    @Override
    public String getName() {
        return "Manhattan Heuristic";
    }
}
