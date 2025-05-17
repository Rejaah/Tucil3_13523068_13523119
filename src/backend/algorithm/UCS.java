package backend.algorithm;

import java.nio.file.Path;
import java.util.*;
import backend.model.Board;

public class UCS implements PathfindingAlgorithm {
    private int nodesVisited;
    private long executionTime;

    @Override
    public List<Board> solve(Board initialBoard) {
        nodesVisited = 0;
        long startTime = System.currentTimeMillis();

        Queue<UCSNode> queue = new LinkedList<>();
        Set<Board> visited = new HashSet<>();

        queue.add(new UCSNode(initialBoard, null, 0));
        visited.add(initialBoard);

        while (!queue.isEmpty()) {
            UCSNode currentNode = queue.poll();
            nodesVisited++;

            // Tampilkan state board yang sedang diperiksa
            System.out.printf("Visited node #%d, cost = %d%n", nodesVisited, currentNode.cost);
            System.out.println(currentNode.board);  // panggil toString() di Board

            if (currentNode.getBoard().isGoal()) {
                executionTime = System.currentTimeMillis() - startTime;
                return reconstructPath(currentNode);
            }

            for (Board neighbor : currentNode.getBoard().generateNeighbors(currentNode.getBoard().getZobristTable())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new UCSNode(neighbor, currentNode, currentNode.getCost() + 1));
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return Collections.emptyList(); // Tidak ada solusi ditemukan
    }

    private static class UCSNode implements SearchNode {
        private final Board board;
        private final SearchNode parent;
        private final int cost;

        public UCSNode(Board board, SearchNode parent, int cost) {
            this.board = board;
            this.parent = parent;
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
        public int getCost() {
            return cost;
        }

        @Override
        public int getHeuristic() {
            return 0;
        }

        @Override
        public int getPriority() {
            return cost;
        }
    }

    private List<Board> reconstructPath(UCSNode node) {
        LinkedList<Board> path = new LinkedList<>();
        while (node != null) {
            path.add(node.getBoard());
            node = (UCSNode) node.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public String getName() {
        return "Uniform Cost Search (UCS)";
    }

    @Override
    public int getNodesVisited() {
        return nodesVisited;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }
}
