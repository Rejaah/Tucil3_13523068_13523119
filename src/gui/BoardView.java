package gui;

import backend.model.Board;
import backend.model.Car;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * View component for rendering the Rush Hour board.
 */
public class BoardView extends Pane {
    private double cellSize = 60.0;
    private Map<Character, Rectangle> carRectangles = new HashMap<>();
    private Board currentBoard;
    private List<Board> solution;
    private IntegerProperty currentStep = new SimpleIntegerProperty(0);
    private Timeline animation;
    private Rectangle exitMarker;
    
    /**
     * Initialize the board view with initial board state.
     * @param board The initial board configuration
     */
    public void initializeBoard(Board board) {
        this.currentBoard = board;
        this.getChildren().clear();
        carRectangles.clear();
        
        // Set view size based on board dimensions
        int rows = board.getRows();
        int cols = board.getCols();
        setPrefSize(cols * cellSize, rows * cellSize);
        
        // Draw board grid
        drawGrid(rows, cols);
        
        // Draw exit marker
        drawExitMarker(board);
        
        // Draw cars
        for (Car car : board.getCars()) {
            Rectangle rect = createCarRectangle(car);
            carRectangles.put(car.getId(), rect);
            this.getChildren().add(rect);
        }
    }
    
    /**
     * Set the solution path for animation.
     * @param solution List of board states from initial to goal
     */
    public void setSolution(List<Board> solution) {
        this.solution = solution;
        currentStep.set(0);
        updateBoardState(solution.get(0));
    }
    
    /**
     * Move to the next step in the solution.
     */
    public void nextStep() {
        if (solution != null && currentStep.get() < solution.size() - 1) {
            currentStep.set(currentStep.get() + 1);
            animateMove(solution.get(currentStep.get() - 1), solution.get(currentStep.get()));
        }
    }
    
    /**
     * Move to the previous step in the solution.
     */
    public void previousStep() {
        if (solution != null && currentStep.get() > 0) {
            currentStep.set(currentStep.get() - 1);
            animateMove(solution.get(currentStep.get() + 1), solution.get(currentStep.get()));
        }
    }
    
    /**
     * Jump to a specific step in the solution.
     * @param step The step index to jump to
     */
    public void goToStep(int step) {
        if (solution != null && step >= 0 && step < solution.size() && step != currentStep.get()) {
            int oldStep = currentStep.get();
            currentStep.set(step);
            
            // If the difference is 1, animate the transition
            if (Math.abs(oldStep - step) == 1) {
                animateMove(solution.get(oldStep), solution.get(step));
            } else {
                // Otherwise, just update the board state directly
                updateBoardState(solution.get(step));
            }
        }
    }
    
    /**
     * Play the solution animation automatically.
     */
    public void playAnimation() {
        if (solution == null || solution.size() <= 1) return;
        
        if (animation != null) {
            animation.stop();
        }
        
        animation = new Timeline(
            new KeyFrame(Duration.seconds(0.8), e -> nextStep())
        );
        animation.setCycleCount(solution.size() - currentStep.get() - 1);
        animation.play();
    }
    
    /**
     * Pause the animation.
     */
    public void pauseAnimation() {
        if (animation != null) {
            animation.stop();
        }
    }
    
    /**
     * Get the current step property for binding.
     */
    public IntegerProperty currentStepProperty() {
        return currentStep;
    }
    
    // Private helper methods
    
    private void drawGrid(int rows, int cols) {
        for (int i = 0; i <= rows; i++) {
            javafx.scene.shape.Line horizontalLine = new javafx.scene.shape.Line(
                0, i * cellSize, cols * cellSize, i * cellSize
            );
            horizontalLine.setStroke(Color.LIGHTGRAY);
            this.getChildren().add(horizontalLine);
        }
        
        for (int j = 0; j <= cols; j++) {
            javafx.scene.shape.Line verticalLine = new javafx.scene.shape.Line(
                j * cellSize, 0, j * cellSize, rows * cellSize
            );
            verticalLine.setStroke(Color.LIGHTGRAY);
            this.getChildren().add(verticalLine);
        }
    }
    
    private void drawExitMarker(Board board) {
        // Get exit position from the board
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();
        
        // Determine exit position and draw exit marker
        Rectangle marker;
        
        if (exitRow < 0) {
            // Exit is above the board
            marker = new Rectangle(
                exitCol * cellSize, 
                -5,  // Slightly above the top edge
                cellSize, 
                10
            );
        } else if (exitRow >= board.getRows()) {
            // Exit is below the board
            marker = new Rectangle(
                exitCol * cellSize, 
                board.getRows() * cellSize - 5,  // Slightly below the bottom edge
                cellSize, 
                10
            );
        } else if (exitCol < 0) {
            // Exit is to the left of the board
            marker = new Rectangle(
                -5,  // Slightly to the left of the left edge
                exitRow * cellSize, 
                10, 
                cellSize
            );
        } else if (exitCol >= board.getCols()) {
            // Exit is to the right of the board
            marker = new Rectangle(
                board.getCols() * cellSize - 5,  // Slightly to the right of the right edge
                exitRow * cellSize, 
                10, 
                cellSize
            );
        } else {
            // This shouldn't happen, but just in case exit is inside the board
            marker = new Rectangle(
                exitCol * cellSize, 
                exitRow * cellSize, 
                cellSize, 
                cellSize
            );
        }
        
        // Add visual styling to the exit marker
        marker.setFill(Color.LIGHTGREEN);
        marker.setOpacity(0.7);
        
        // Store reference to exit marker and add it to the view
        this.exitMarker = marker;
        this.getChildren().add(exitMarker);
        
        // Add an exit label for clarity
        javafx.scene.text.Text exitLabel = new javafx.scene.text.Text("EXIT");
        exitLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 10));
        
        // Position the label near the exit marker
        if (exitRow < 0) {
            exitLabel.setX(exitCol * cellSize + cellSize/4);
            exitLabel.setY(-15);
        } else if (exitRow >= board.getRows()) {
            exitLabel.setX(exitCol * cellSize + cellSize/4);
            exitLabel.setY(board.getRows() * cellSize + 15);
        } else if (exitCol < 0) {
            exitLabel.setX(-25);
            exitLabel.setY(exitRow * cellSize + cellSize/2);
        } else if (exitCol >= board.getCols()) {
            exitLabel.setX(board.getCols() * cellSize + 10);
            exitLabel.setY(exitRow * cellSize + cellSize/2);
        }
        
        this.getChildren().add(exitLabel);
    }
    
    private Rectangle createCarRectangle(Car car) {
        double width, height;
        
        // Determine car size and orientation
        boolean isVertical = car.isVertical();
        if (isVertical) {
            width = cellSize - 4;
            height = car.getLength() * cellSize - 4;
        } else {
            width = car.getLength() * cellSize - 4;
            height = cellSize - 4;
        }
        
        Rectangle rect = new Rectangle(
            car.getCol() * cellSize + 2,
            car.getRow() * cellSize + 2,
            width,
            height
        );
        
        // Style based on car type
        if (car.isPrimary()) {
            rect.setFill(Color.RED);
            rect.setStroke(Color.DARKRED);
        } else {
            rect.setFill(generateColorForCar(car.getId()));
            rect.setStroke(Color.BLACK);
        }
        
        rect.setArcWidth(10);  // Rounded corners
        rect.setArcHeight(10);
        rect.setStrokeWidth(1.5);
        
        return rect;
    }
    
    private Color generateColorForCar(char id) {
        // Generate a unique color for each car based on its ID
        int hash = id * 83;
        
        return Color.rgb(
            (hash * 123) % 200 + 55,  // R
            (hash * 47) % 200 + 55,   // G
            (hash * 29) % 200 + 55    // B
        );
    }
    
    private void animateMove(Board fromBoard, Board toBoard) {
        // Find the car that moved
        Car movedCar = findMovedCar(fromBoard, toBoard);
        
        if (movedCar != null) {
            // Get the rectangle for this car
            Rectangle carRect = carRectangles.get(movedCar.getId());
            
            // Calculate the difference in position
            Car carInNewState = findCarById(toBoard, movedCar.getId());
            
            if (carRect != null && carInNewState != null) {
                double fromX = movedCar.getCol() * cellSize + 2;
                double fromY = movedCar.getRow() * cellSize + 2;
                double toX = carInNewState.getCol() * cellSize + 2;
                double toY = carInNewState.getRow() * cellSize + 2;
                
                // Create animation
                TranslateTransition transition = new TranslateTransition(Duration.millis(300), carRect);
                transition.setFromX(carRect.getTranslateX());
                transition.setFromY(carRect.getTranslateY());
                transition.setToX(toX - fromX);
                transition.setToY(toY - fromY);
                
                // Play animation
                transition.play();
            }
        } else {
            // If we couldn't determine which car moved, just update board state
            updateBoardState(toBoard);
        }
    }
    
    private Car findMovedCar(Board board1, Board board2) {
        // This is a placeholder - implement based on your Board/Car classes
        // Should return the car that changed position between board1 and board2
        return null;
    }
    
    private Car findCarById(Board board, char id) {
        // This is a placeholder - implement based on your Board/Car classes
        // Should return the car with the given ID from the board
        return null;
    }
    
    private void updateBoardState(Board board) {
        // Update all car positions based on the new board state
        for (Car car : board.getCars()) {
            Rectangle rect = carRectangles.get(car.getId());
            
            if (rect != null) {
                rect.setTranslateX(0);
                rect.setTranslateY(0);
                rect.setX(car.getCol() * cellSize + 2);
                rect.setY(car.getRow() * cellSize + 2);
            }
        }
    }
    
    /**
     * Create a sample board for demonstration purposes.
     * Used when no file is loaded yet.
     */
    // public void createSampleBoard() {
    //     this.getChildren().clear();
        
    //     // Sample board dimensions
    //     int rows = 6;
    //     int cols = 6;
        
    //     // Set view size
    //     setPrefSize(cols * cellSize, rows * cellSize);
        
    //     // Draw grid
    //     drawGrid(rows, cols);
        
    //     // Draw an exit marker at the right edge, third row (for demonstration)
    //     Rectangle exit = new Rectangle(
    //         cols * cellSize - 5,
    //         2 * cellSize,
    //         10,
    //         cellSize
    //     );
    //     exit.setFill(Color.LIGHTGREEN);
    //     exit.setOpacity(0.7);
    //     this.getChildren().add(exit);
        
    //     // Add exit label
    //     javafx.scene.text.Text exitLabel = new javafx.scene.text.Text("EXIT");
    //     exitLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 10));
    //     exitLabel.setX(cols * cellSize + 10);
    //     exitLabel.setY(2 * cellSize + cellSize/2);
    //     this.getChildren().add(exitLabel);
        
    //     // Draw some sample cars
        
    //     // Primary car (horizontal at position 2,2 with length 2)
    //     Rectangle primaryCar = new Rectangle(
    //         2 * cellSize + 2,
    //         2 * cellSize + 2,
    //         2 * cellSize - 4,
    //         cellSize - 4
    //     );
    //     primaryCar.setFill(Color.RED);
    //     primaryCar.setStroke(Color.DARKRED);
    //     primaryCar.setStrokeWidth(1.5);
    //     primaryCar.setArcWidth(10);
    //     primaryCar.setArcHeight(10);
    //     this.getChildren().add(primaryCar);
        
    //     // Add a few more sample cars with different colors
    //     // Vertical car (at position 0,0 with length 3)
    //     Rectangle verticalCar = new Rectangle(
    //         0 * cellSize + 2,
    //         0 * cellSize + 2,
    //         cellSize - 4,
    //         3 * cellSize - 4
    //     );
    //     verticalCar.setFill(Color.BLUE);
    //     verticalCar.setStroke(Color.BLACK);
    //     verticalCar.setStrokeWidth(1.5);
    //     verticalCar.setArcWidth(10);
    //     verticalCar.setArcHeight(10);
    //     this.getChildren().add(verticalCar);
        
    //     // Horizontal car (at position 0,3 with length 2)
    //     Rectangle horizontalCar = new Rectangle(
    //         0 * cellSize + 2,
    //         3 * cellSize + 2,
    //         2 * cellSize - 4,
    //         cellSize - 4
    //     );
    //     horizontalCar.setFill(Color.GREEN);
    //     horizontalCar.setStroke(Color.BLACK);
    //     horizontalCar.setStrokeWidth(1.5);
    //     horizontalCar.setArcWidth(10);
    //     horizontalCar.setArcHeight(10);
    //     this.getChildren().add(horizontalCar);
    // }
}