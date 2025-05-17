package backend.algorithm;

import backend.model.Board;

public interface SearchNode extends Comparable<SearchNode> {
    Board getBoard();
    SearchNode getParent();
    int getCost();
    int getHeuristic();
    int getPriority();

    @Override
    default int compareTo(SearchNode other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
}
