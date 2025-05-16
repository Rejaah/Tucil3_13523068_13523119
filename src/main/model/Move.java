package main.model;

public class Move {
    private final char carId;
    private final int delta;

    public Move(char carId, int delta) {
        this.carId = carId;
        this.delta = delta;
    }

    public char getCarId() {
        return carId;
    }

    public int getDelta() {
        return delta;
    }

    public int getCost() {
        return 1;
    }

    @Override
    public String toString() {
        return "Move{" +
                "carId=" + carId +
                ", delta=" + delta +
                '}';
    }
}
