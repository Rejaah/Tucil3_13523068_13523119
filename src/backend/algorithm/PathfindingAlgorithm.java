package backend.algorithm;

import backend.model.Board;
import backend.util.Heuristic;

import java.util.List;

public interface PathfindingAlgorithm {
    /**
     * Menyelesaikan puzzle Rush Hour dengan algoritma ini.
     * @param initialBoard Konfigurasi papan awal
     * @param heuristic Heuristik yang digunakan
     * @return Daftar state papan dari awal hingga goal
     */

    List<Board> solve(Board initialBoard, Heuristic heuristic);
    
    /**
     * Mendapatkan nama algoritma.
     * @return Nama algoritma
     */

    String getName();
    
    /**
     * Mendapatkan jumlah node yang dikunjungi.
     * @return Jumlah node yang dikunjungi
     */

    int getNodesVisited();
    
    /**
     * Mendapatkan waktu eksekusi dalam milidetik.
     * @return Waktu eksekusi
     */

    long getExecutionTime();

    /**
     * Mendapatkan nama heuristik yang digunakan.
     * @return Nama heuristik
     */
    
    String getHeuristicName();

}
