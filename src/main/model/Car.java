package main.model;

public class Car {
    private final char id;
    private final boolean isHorizontal;
    private final int length;
    private int row;
    private int col;

    public Car(char id, boolean isHorizontal, int length, int row, int col) {
        this.id = id;
        this.isHorizontal = isHorizontal;
        this.length = length;
        this.row = row;
        this.col = col;
    }

    public char getId() {
        return id;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public int getLength() {
        return length;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void move(int delta) {
        if (isHorizontal) {
            col += delta;
        } else {
            row += delta;
        }
    }

    public Car copy() {
        return new Car(id, isHorizontal, length, row, col);
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", isHorizontal=" + isHorizontal +
                ", length=" + length +
                ", row=" + row +
                ", col=" + col +
                '}';
    }
}