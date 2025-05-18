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

        // Jika mobil utama bergerak horizontal
        if (player.isHorizontal()) {
            int rightmostCol = player.getCol() + player.getLength() - 1;
            if (exitCol > rightmostCol) {
                return Math.abs(player.getRow() - exitRow) + Math.abs(rightmostCol - exitCol);
            }
            else {
                return Math.abs(player.getRow() - exitRow) + Math.abs(player.getCol() - exitCol);
            }
        }
        // Jika mobil utama bergerak vertikal 
        else {
            int bottomRow = player.getRow() + player.getLength() - 1;
            if (exitRow > bottomRow) {
                return Math.abs(player.getCol() - exitCol) + Math.abs(bottomRow - exitRow); 
            }
            else {
                return Math.abs(player.getCol() - exitCol) + Math.abs(player.getRow() - exitRow); 
            }
        }
    }

    @Override
    public String getName() {
        return "Manhattan Heuristic";
    }
}
