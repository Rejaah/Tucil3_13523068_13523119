package backend.exception;

public class InvalidInputException extends Exception {
    /**
     * Buat exception tanpa pesan khusus.
     */
    public InvalidInputException() {
        super();
    }

    /**
     * Buat exception dengan pesan kesalahan.
     * @param message deskripsi kesalahan
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Buat exception dengan pesan dan penyebab.
     * @param message deskripsi kesalahan
     * @param cause penyebab asli (throwable)
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Buat exception dengan penyebab saja.
     * @param cause penyebab asli (throwable)
     */
    public InvalidInputException(Throwable cause) {
        super(cause);
    }
}
