package backend.algorithm;

import backend.model.Board;
import backend.model.Car;
import backend.model.Move;
import java.util.*;

public class AStar implements PathfindingAlgorithm {
   private int visitedNodes = 0; // Jumlah node yang dikunjungi
   private long execTime = 0; // Waktu eksekusi algoritma (ms)
   
   @Override
   public List<Board> solve(Board initBoard) {
       // 1. Inisialisasi waktu mulai untuk pengukuran performa
       long startTime = System.currentTimeMillis();
       visitedNodes = 0;
       
       // 2. Priority queue untuk open set, berdasarkan nilai f(n) = g(n) + h(n)
       PriorityQueue<ANode> openSet = new PriorityQueue<>(
           Comparator.comparingInt(node -> node.f));
       
       // 3. Simpan state yang sudah dieksplorasi (closed set)
       Set<Board> closedSet = new HashSet<>();
       
       // 4. Mapping untuk rekonstruksi jalur
       Map<Board, Board> prevBoard = new HashMap<>();
       
       // 5. Mapping untuk g score (biaya dari start ke node)
       Map<Board, Integer> gScore = new HashMap<>();
       
       // 6. Inisialisasi node awal dengan g=0 dan h=heuristik
       ANode startNode = new ANode(initBoard, 0, heuristic(initBoard));
       openSet.add(startNode);
       gScore.put(initBoard, 0);
       
       // 7. Loop utama algoritma A*
       while (!openSet.isEmpty()) {
           // 8. Ambil node dengan f(n) terkecil
           ANode current = openSet.poll();
           visitedNodes++;
           
           // 9. Cek apakah sudah mencapai goal state
           if (isGoalState(current.board)) {
               execTime = System.currentTimeMillis() - startTime;
               return buildPath(prevBoard, current.board);
           }
           
           // 10. Tambahkan ke closed set
           closedSet.add(current.board);
           
           // 11. Generate semua tetangga (neighbor) state
           List<Board> neighbors = current.board.generateNeighbors(current.board.getZobristTable());
           
           // 12. Proses setiap tetangga
           for (Board neighbor : neighbors) {
               // 13. Lewati jika sudah dieksplorasi
               if (closedSet.contains(neighbor)) {
                   continue;
               }
               
               // 14. Hitung g score sementara (biaya dari start ke tetangga melalui current)
               int tentG = gScore.get(current.board) + 1; // 1 adalah cost untuk satu gerakan
               
               // 15. Jika ini node baru atau kita menemukan jalur yang lebih baik
               if (!gScore.containsKey(neighbor) || tentG < gScore.get(neighbor)) {
                   // 16. Perbarui jalur
                   prevBoard.put(neighbor, current.board);
                   gScore.put(neighbor, tentG);
                   
                   // 17. Tambahkan ke open set dengan f = g + h
                   openSet.add(new ANode(neighbor, tentG, heuristic(neighbor)));
               }
           }
       }
       
       // 18. Tidak ada solusi yang ditemukan
       execTime = System.currentTimeMillis() - startTime;
       return new ArrayList<>();
   }
   
   private int heuristic(Board board) {
       // 19. Cari primary car (mobil utama)
       Car primCar = null;
       for (Car car : board.getCars()) {
           if (car.isPrimary()) {
               primCar = car;
               break;
           }
       }
       
       if (primCar == null) {
           return Integer.MAX_VALUE; // State tidak valid
       }
       
       // 20. Hitung jarak horizontal ke exit
       int distToExit;
       if (board.getExitCol() > primCar.getCol() + primCar.getLength() - 1) {
           // Exit ada di kanan
           distToExit = board.getExitCol() - (primCar.getCol() + primCar.getLength());
       } else if (board.getExitCol() < primCar.getCol()) {
           // Exit ada di kiri
           distToExit = primCar.getCol() - board.getExitCol();
       } else {
           // Mobil utama sudah sejajar dengan exit
           return 0;
       }
       
       // 21. Hitung jumlah mobil yang menghalangi
       int blockingCars = countBlocking(board, primCar);
       
       // 22. Heuristik gabungan: jarak + (2 × jumlah penghalang)
       return distToExit + (2 * blockingCars);
   }

   private int countBlocking(Board board, Car primCar) {
       int count = 0;
       int primRow = primCar.getRow();
       int primEndCol = primCar.getCol() + primCar.getLength() - 1;
       int exitCol = board.getExitCol();
       
       // 23. Cek mobil-mobil yang menghalangi jalur horizontal ke exit
       for (Car car : board.getCars()) {
           if (car.isPrimary()) continue;
           
           // 24. Cek apakah mobil menghalangi jalur horizontal ke exit
           if (car.isVertical() && car.getRow() <= primRow && 
               car.getRow() + car.getLength() - 1 >= primRow) {
               
               // 25. Mobil berada di baris yang sama dengan mobil utama
               if (exitCol > primEndCol && car.getCol() > primEndCol && car.getCol() <= exitCol) {
                   // 26. Mobil menghalangi jalur ke exit di kanan
                   count++;
               } else if (exitCol < primCar.getCol() && car.getCol() < primCar.getCol() && car.getCol() >= exitCol) {
                   // 27. Mobil menghalangi jalur ke exit di kiri
                   count++;
               }
           }
       }
       
       return count;
   }
   
   private boolean isGoalState(Board board) {
       // 28. Cari mobil utama
       Car primCar = null;
       for (Car car : board.getCars()) {
           if (car.isPrimary()) {
               primCar = car;
               break;
           }
       }
       
       if (primCar == null) {
           return false;
       }
       
       // 29. Cek apakah mobil utama bersebelahan dengan exit
       if (primCar.isHorizontal()) {
           // 30. Mobil horizontal bisa keluar dari samping
           int exitCol = board.getExitCol();
           int carEndCol = primCar.getCol() + primCar.getLength() - 1;
           
           // 31. Mobil berada di baris exit dan bisa bergerak ke exit
           return primCar.getRow() == board.getExitRow() && 
                 (primCar.getCol() == exitCol + 1 || carEndCol == exitCol - 1);
       } else {
           // 32. Mobil vertikal bisa keluar dari atas/bawah
           int exitRow = board.getExitRow();
           int carEndRow = primCar.getRow() + primCar.getLength() - 1;
           
           // 33. Mobil berada di kolom exit dan bisa bergerak ke exit
           return primCar.getCol() == board.getExitCol() && 
                 (primCar.getRow() == exitRow + 1 || carEndRow == exitRow - 1);
       }
   }

   private List<Board> buildPath(Map<Board, Board> prevBoard, Board current) {
       // 34. Inisialisasi path dengan goal
       List<Board> path = new ArrayList<>();
       path.add(current);
       
       // 35. Rekonstruksi jalur dari goal ke start
       while (prevBoard.containsKey(current)) {
           current = prevBoard.get(current);
           path.add(0, current); // Tambahkan di awal list
       }
       
       return path;
   }
   
   @Override
   public String getName() {
       return "A* Search";
   }
   
   @Override
   public int getNodesVisited() {
       return visitedNodes;
   }
   
   @Override
   public long getExecutionTime() {
       return execTime;
   }
   
   private static class ANode {
       Board board; // State board
       int g;       // Biaya dari start ke node ini
       int h;       // Perkiraan heuristik dari node ini ke goal
       int f;       // f(n) = g(n) + h(n)
       
       ANode(Board board, int g, int h) {
           this.board = board;
           this.g = g;
           this.h = h;
           this.f = g + h;
       }
   }
}