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
       long startTime = System.currentTimeMillis();
       visitedNodes = 0;
       
       PriorityQueue<ANode> openSet = new PriorityQueue<>(
           Comparator.comparingInt(node -> node.f));
       
       Set<Board> closedSet = new HashSet<>();
       
       Map<Board, Board> prevBoard = new HashMap<>();
       
       Map<Board, Integer> gScore = new HashMap<>();
       
       ANode startNode = new ANode(initBoard, 0, heuristic.estimate(initBoard));
       openSet.add(startNode);
       gScore.put(initBoard, 0);
       
       while (!openSet.isEmpty()) {
           ANode current = openSet.poll();
           visitedNodes++;
           
           if (isGoalState(current.board)) {
               execTime = System.currentTimeMillis() - startTime;
               return buildPath(prevBoard, current.board);
           }
           
           closedSet.add(current.board);
           
           List<Board> neighbors = current.board.generateNeighbors(current.board.getZobristTable());
           
           for (Board neighbor : neighbors) {
               if (closedSet.contains(neighbor)) {
                   continue;
               }
               
               int tentG = gScore.get(current.board) + 1; 
               
               if (!gScore.containsKey(neighbor) || tentG < gScore.get(neighbor)) {
                   prevBoard.put(neighbor, current.board);
                   gScore.put(neighbor, tentG);
                   
                   openSet.add(new ANode(neighbor, tentG, heuristic.estimate(neighbor)));
               }
           }
       }
       
       execTime = System.currentTimeMillis() - startTime;
       return new ArrayList<>();
   }
   
   private boolean isGoalState(Board board) {
       return board.isGoal();
   }

   private List<Board> buildPath(Map<Board, Board> prevBoard, Board current) {
       List<Board> path = new ArrayList<>();
       path.add(current);
       
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
       int g;     
       int h;      
       int f;       
       
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