package backend.algorithm;

import java.util.*;
import backend.model.Board;
import backend.util.Heuristic;

public class GBFS implements PathfindingAlgorithm {
    private int nodesVisited;
    private long executionTime;
    private Heuristic heuristic;

    public GBFS(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    @Override
    public List<Board> solve(Board initialBoard, Heuristic heuristic) {
        nodesVisited = 0;
        long startTime = System.currentTimeMillis();

        PriorityQueue<GBFSNode> openSet = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.heuristicValue));

        Map<Board, Integer> bestHeuristics = new HashMap<>();

        openSet.add(new GBFSNode(initialBoard, null, heuristic.estimate(initialBoard), 0));
        bestHeuristics.put(initialBoard, heuristic.estimate(initialBoard));

        while (!openSet.isEmpty()) {
            GBFSNode currentNode = openSet.poll();
            nodesVisited++;

            if (bestHeuristics.get(currentNode.getBoard()) < currentNode.heuristicValue) {
                continue;
            }

            if (currentNode.getBoard().isGoal()) {
                executionTime = System.currentTimeMillis() - startTime;
                return reconstructPath(currentNode);
            }

            for (Board neighbor : currentNode.getBoard().generateNeighbors(
                    currentNode.getBoard().getZobristTable())) {
                
                int newHeuristic = heuristic.estimate(neighbor);
                
                if (!bestHeuristics.containsKey(neighbor) || 
                    newHeuristic < bestHeuristics.get(neighbor)) {
                    
                    bestHeuristics.put(neighbor, newHeuristic);
                    openSet.add(new GBFSNode(neighbor, currentNode, newHeuristic, currentNode.cost + 1));
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return Collections.emptyList();
    }

    private List<Board> reconstructPath(GBFSNode node) {
        List<Board> path = new ArrayList<>();
        while (node != null) {
            path.add(node.getBoard());
            node = (GBFSNode) node.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public String getName() {
        return "Greedy Best-First Search (GBFS)";
    }

    @Override
    public int getNodesVisited() {
        return nodesVisited;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public String getHeuristicName() {
        return heuristic.getName();
    }

    private static class GBFSNode implements SearchNode {
        private final Board board;
        private final SearchNode parent;
        int heuristicValue;
        int cost;

        public GBFSNode(Board board, SearchNode parent, int heuristicValue, int cost) {
            this.board = board;
            this.parent = parent;
            this.heuristicValue = heuristicValue;
            this.cost = cost;
        }

        @Override
        public Board getBoard() {
            return board;
        }

        @Override
        public SearchNode getParent() {
            return parent;
        }

        @Override
        public int getHeuristic() {
            return heuristicValue;
        }

        @Override
        public int getCost() {
            return cost;
        }

        @Override
        public int getPriority() {
            return heuristicValue;
        }
    }
}
