package backend.util;

import backend.model.Board;

public interface Heuristic {
    /**
     * Menghitung estimasi jarak dari papan saat ini ke goal.
     *
     * @param board Papan yang sedang diperiksa
     * @return Estimasi jarak (heuristik)
     */
    int estimate(Board board);

    String getName();
    /**
     * Mendapatkan nama heuristik.
     * @return Nama heuristik
     */
} 
