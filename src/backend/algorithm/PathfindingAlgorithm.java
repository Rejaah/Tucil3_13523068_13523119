package backend.algorithm;

import backend.model.Board;
import java.util.List;

/**
 * Interface for pathfinding algorithms.
 * This is a dummy implementation for GUI development.
 */
//=======
public interface PathfindingAlgorithm {
    /**
     * Menyelesaikan puzzle Rush Hour dengan algoritma ini.
     * @param initialBoard Konfigurasi papan awal
     * @return Daftar state papan dari awal hingga goal
     */
    List<Board> solve(Board initialBoard);
    
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
}
//=======