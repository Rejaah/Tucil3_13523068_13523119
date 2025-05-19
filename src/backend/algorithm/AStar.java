package backend.algorithm;

import backend.model.Board;
import backend.util.Heuristic;

import java.util.*;

public class AStar implements PathfindingAlgorithm {
   private Heuristic heuristic; 
   private int visitedNodes = 0; 
   private long execTime = 0; 
   
   public AStar(Heuristic heuristic) {
       this.heuristic = heuristic;
   }
   
   @Override
   public List<Board> solve(Board initBoard, Heuristic heuristic) {
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
       ANode startNode = new ANode(initBoard, 0, heuristic.estimate(initBoard));
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
                   openSet.add(new ANode(neighbor, tentG, heuristic.estimate(neighbor)));
               }
           }
       }
       
       // 18. Tidak ada solusi yang ditemukan
       execTime = System.currentTimeMillis() - startTime;
       return new ArrayList<>();
   }
   
   private boolean isGoalState(Board board) {
       return board.isGoal();
   }

   private List<Board> buildPath(Map<Board, Board> prevBoard, Board current) {
       // 34. Inisialisasi path dengan goal
       List<Board> path = new ArrayList<>();
       path.add(current);
       
       // 35. Rekonstruksi jalur dari goal ke start
       while (prevBoard.containsKey(current)) {
           current = prevBoard.get(current);
           path.add(0, current); 
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

   @Override
    public String getHeuristicName() {
         return heuristic.getName();
    }

   private static class ANode implements SearchNode{
       Board board; 
       int g;       // Biaya dari start ke node ini
       int h;       // Perkiraan heuristik dari node ini ke goal
       int f;       // f(n) = g(n) + h(n)
       
       public ANode(Board board, int g, int h) {
           this.board = board;
           this.g = g;
           this.h = h;
           this.f = g + h;
       }

        @Override
        public Board getBoard() {
            return board;
        }

        @Override
        public SearchNode getParent() {
            return null;
        }

        @Override
        public int getCost() {
            return g;
        }

        @Override
        public int getHeuristic() {
            return h;
        }

        @Override
        public int getPriority() {
            return f;
        }

   }
}