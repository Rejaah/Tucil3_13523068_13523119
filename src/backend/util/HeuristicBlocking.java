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
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        // Jika mobil utama bergerak horizontal (exit horizontal)
        if (player.isHorizontal()) {
            for (Car car : board.getCars()) {
                if (car.isPrimary()) continue;

                if (car.isVertical() && car.getRow() <= primRow &&
                        car.getRow() + car.getLength() - 1 >= primRow) {
                    if (exitCol > primEndCol && car.getCol() > primEndCol && car.getCol() <= exitCol) {
                        count++;
                    } else if (exitCol < player.getCol() && car.getCol() < player.getCol() && car.getCol() >= exitCol) {
                        count++;  
                    }
                }
            }
        }
        // Jika mobil utama bergerak vertikal (exit vertikal)
        else {
            for (Car car : board.getCars()) {
                if (car.isPrimary()) continue;

                if (!car.isVertical() && car.getCol() <= player.getCol() + player.getLength() - 1 &&
                        car.getCol() >= player.getCol()) {
                    if (exitRow > primRow && car.getRow() > primRow && car.getRow() <= exitRow) {
                        count++;
                    } else if (exitRow < player.getRow() && car.getRow() < player.getRow() && car.getRow() >= exitRow) {
                        count++;
                    }
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
