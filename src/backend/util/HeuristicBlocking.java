package backend.util;

import backend.model.Board;
import backend.model.Car;

public class HeuristicBlocking implements Heuristic {
    @Override
    public int estimate(Board board) {
        // Dapatkan posisi mobil utama 'P'
        Car player = board.getCar('P');
        return countBlocking(board, player);  // Menghitung jumlah mobil yang menghalangi
    }

    private int countBlocking(Board board, Car player) {
        int count = 0;
        int primRow = player.getRow();
        int primEndCol = player.getCol() + player.getLength() - 1;
        int exitCol = board.getExitCol();

        // Cek mobil-mobil yang menghalangi jalur horizontal ke exit
        for (Car car : board.getCars()) {
            if (car.isPrimary()) continue;

            // Cek apakah mobil menghalangi jalur horizontal ke exit
            if (car.isVertical() && car.getRow() <= primRow && 
                car.getRow() + car.getLength() - 1 >= primRow) {
                if (exitCol > primEndCol && car.getCol() > primEndCol && car.getCol() <= exitCol) {
                    count++;
                } else if (exitCol < player.getCol() && car.getCol() < player.getCol() && car.getCol() >= exitCol) {
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public String getName() {
        return "Blocking Heuristic";
    }
}
