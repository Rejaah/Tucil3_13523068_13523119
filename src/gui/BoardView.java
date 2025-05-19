package gui;

import backend.model.*;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.*;

import java.util.*;

public class BoardView extends Pane {
    private double cellSize = 30.0;
    private final Map<Character, Rectangle> carRectangles = new HashMap<>();
    private Board currentBoard;
    private List<Board> solution;
    private final IntegerProperty currentStep = new SimpleIntegerProperty(0);
    private Timeline animation;
    private Rectangle exitMarker;

    public void initializeBoard(Board board) {
        this.currentBoard = board;
        this.getChildren().clear();
        carRectangles.clear();

        int rows = board.getRows();
        int cols = board.getCols();

        cellSize = (rows > 10 || cols > 10) ? Math.min(40.0, 600.0 / Math.max(rows, cols)) : 60.0;

        double width = cols * cellSize;
        double height = rows * cellSize;

        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);

        drawGrid(rows, cols);
        drawExitMarker(board);

        for (Car car : board.getCars()) {
            Rectangle rect = createCarRectangle(car);
            carRectangles.put(car.getId(), rect);
            this.getChildren().add(rect);
        }
    }

    public void setSolution(List<Board> solution) {
        this.solution = solution;
        currentStep.set(0);
        updateBoardState(solution.get(0));
    }

    public void nextStep() {
        if (solution != null && currentStep.get() < solution.size() - 1) {
            currentStep.set(currentStep.get() + 1);
            updateBoardState(solution.get(currentStep.get()));
        }
    }

    public void previousStep() {
        if (solution != null && currentStep.get() > 0) {
            currentStep.set(currentStep.get() - 1);
            updateBoardState(solution.get(currentStep.get()));
        }
    }

    public void goToStep(int step) {
        if (solution != null && step >= 0 && step < solution.size() && step != currentStep.get()) {
            currentStep.set(step);
            updateBoardState(solution.get(step));
        }
    }

    public void playAnimation() {
        if (solution == null || solution.size() <= 1) return;
        if (animation != null) animation.stop();
        animation = new Timeline(new KeyFrame(Duration.seconds(0.8), e -> nextStep()));
        animation.setCycleCount(solution.size() - currentStep.get() - 1);
        animation.play();
    }

    public void pauseAnimation() {
        if (animation != null) animation.stop();
    }

    public IntegerProperty currentStepProperty() {
        return currentStep;
    }

    private void drawGrid(int rows, int cols) {
        for (int i = 0; i <= rows; i++) {
            Line hLine = new Line(0, i * cellSize, cols * cellSize, i * cellSize);
            hLine.setStroke(Color.LIGHTGRAY);
            this.getChildren().add(hLine);
        }
        for (int j = 0; j <= cols; j++) {
            Line vLine = new Line(j * cellSize, 0, j * cellSize, rows * cellSize);
            vLine.setStroke(Color.LIGHTGRAY);
            this.getChildren().add(vLine);
        }
    }

    private void drawExitMarker(Board board) {
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        Rectangle marker;

        if (exitRow < 0) {
            marker = new Rectangle(exitCol * cellSize, -5, cellSize, 10);
        } else if (exitRow >= board.getRows()) {
            marker = new Rectangle(exitCol * cellSize, board.getRows() * cellSize - 5, cellSize, 10);
        } else if (exitCol < 0) {
            marker = new Rectangle(-5, exitRow * cellSize, 10, cellSize);
        } else if (exitCol >= board.getCols()) {
            marker = new Rectangle(board.getCols() * cellSize - 5, exitRow * cellSize, 10, cellSize);
        } else {
            marker = new Rectangle(exitCol * cellSize, exitRow * cellSize, cellSize, cellSize);
        }

        marker.setFill(Color.LIGHTGREEN);
        marker.setOpacity(0.7);
        this.exitMarker = marker;
        this.getChildren().add(exitMarker);

        Text exitLabel = new Text("EXIT");
        exitLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        if (exitRow < 0) {
            exitLabel.setX(exitCol * cellSize + cellSize / 4);
            exitLabel.setY(-15);
        } else if (exitRow >= board.getRows()) {
            exitLabel.setX(exitCol * cellSize + cellSize / 4);
            exitLabel.setY(board.getRows() * cellSize + 15);
        } else if (exitCol < 0) {
            exitLabel.setX(-25);
            exitLabel.setY(exitRow * cellSize + cellSize / 2);
        } else if (exitCol >= board.getCols()) {
            exitLabel.setX(board.getCols() * cellSize + 10);
            exitLabel.setY(exitRow * cellSize + cellSize / 2);
        }

        this.getChildren().add(exitLabel);
    }

    private Rectangle createCarRectangle(Car car) {
        double width = car.isVertical() ? cellSize - 4 : car.getLength() * cellSize - 4;
        double height = car.isVertical() ? car.getLength() * cellSize - 4 : cellSize - 4;
        Rectangle rect = new Rectangle(car.getCol() * cellSize + 2, car.getRow() * cellSize + 2, width, height);

        if (car.isPrimary()) {
            rect.setFill(Color.RED);
            rect.setStroke(Color.DARKRED);
        } else {
            rect.setFill(generateColorForCar(car.getId()));
            rect.setStroke(Color.BLACK);
        }

        rect.setArcWidth(10);
        rect.setArcHeight(10);
        rect.setStrokeWidth(1.5);

        Text idText = new Text(String.valueOf(car.getId()));
        idText.setFill(Color.WHITE);
        idText.setFont(Font.font("Arial", FontWeight.BOLD, cellSize / 3));

        if (car.isVertical()) {
            idText.setX(car.getCol() * cellSize + cellSize / 2 - 5);
            idText.setY(car.getRow() * cellSize + height / 2 + 5);
        } else {
            idText.setX(car.getCol() * cellSize + width / 2 - 5);
            idText.setY(car.getRow() * cellSize + cellSize / 2 + 5);
        }

        this.getChildren().add(idText);
        return rect;
    }

    private Color generateColorForCar(char id) {
        int hash = id * 91;
        return Color.rgb(
            (hash * 123) % 200 + 57,
            (hash * 47) % 200 + 59,
            (hash * 29) % 200 + 53
        );
    }

    private void updateBoardState(Board board) {
        this.getChildren().clear();
        carRectangles.clear();
        this.currentBoard = board;

        int rows = board.getRows();
        int cols = board.getCols();

        drawGrid(rows, cols);
        drawExitMarker(board);

        for (Car car : board.getCars()) {
            Rectangle rect = createCarRectangle(car);
            carRectangles.put(car.getId(), rect);
            this.getChildren().add(rect);
        }
    }
}
