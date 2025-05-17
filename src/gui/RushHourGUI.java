package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import backend.model.Board;
import backend.exception.ParserException;
import backend.model.Parser;

/**
 * Main JavaFX application for Rush Hour Puzzle Solver.
 */
public class RushHourGUI extends Application {
    private BoardView boardView;
    private File selectedFile;
    private Board board;
    private Label fileNameLabel;
    private Button runButton;
    private ComboBox<String> algorithmCombo;
    private Label statsLabel;
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Left panel with controls
        VBox controlPanel = createControlPanel(primaryStage);
        root.setLeft(controlPanel);
        
        // Center panel with board
        boardView = new BoardView();
        boardView.setPrefSize(500, 500);
        boardView.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2px;");
        
        // Add some padding around the board
        StackPane centerWrapper = new StackPane(boardView);
        centerWrapper.setPadding(new Insets(20));
        root.setCenter(centerWrapper);
        
        // Add controls below the board
        HBox animationControls = createAnimationControls();
        
        VBox centerPanel = new VBox(10);
        centerPanel.getChildren().addAll(centerWrapper, animationControls);
        root.setCenter(centerPanel);
        
        Scene scene = new Scene(root, 800, 600);
        
        // Add basic styling
        scene.getRoot().setStyle("-fx-font-family: 'Arial'; -fx-background-color: #f5f5f5;");
        
        primaryStage.setTitle("Rush Hour Puzzle Solver");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Show a sample board initially
        // boardView.createSampleBoard();
    }
    
    private VBox createControlPanel(Stage stage) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e0e0e0; -fx-min-width: 250px;");
        
        // Title
        Label titleLabel = new Label("Rush Hour Puzzle Solver");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // File input
        Button fileButton = new Button("Select Puzzle File");
        fileButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");
        fileNameLabel = new Label("No file selected");
        fileNameLabel.setWrapText(true);
        
        // Algorithm selection
        Label algoLabel = new Label("Select Algorithm:");
        algorithmCombo = new ComboBox<>();
        algorithmCombo.getItems().addAll(
            "Greedy Best First Search",
            "Uniform Cost Search (UCS)",
            "A* Search"
        );
        algorithmCombo.setValue("A* Search");
        algorithmCombo.setPrefWidth(200);
        
        // Run button
        runButton = new Button("Run Solver");
        runButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");
        runButton.setPrefWidth(200);
        runButton.setDisable(true);
        
        // Statistics section
        statsLabel = new Label("No data");
        statsLabel.setWrapText(true);
        
        VBox statsBox = new VBox(5);
        statsBox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #cccccc;");
        statsBox.getChildren().addAll(new Label("Statistics:"), statsLabel);
        
        // Add all to panel
        panel.getChildren().addAll(
            titleLabel,
            new Separator(),
            fileButton,
            fileNameLabel,
            new Separator(),
            algoLabel,
            algorithmCombo,
            runButton,
            new Separator(),
            statsBox
        );
        
        // Add file selection functionality
        fileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Rush Hour Puzzle File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            
            selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile != null) {
                fileNameLabel.setText(selectedFile.getName());
                
                try {
                    // Parse the selected file
                    board = Parser.parseFile(selectedFile.getPath());
                    
                    // Initialize board view
                    boardView.initializeBoard(board);
                    
                    // Enable run button
                    runButton.setDisable(false);
                    
                    // Reset statistics
                    statsLabel.setText("Ready to solve");
                } catch (ParserException ex) {
                    // Show error message
                    fileNameLabel.setText("Error: " + ex.getMessage());
                    runButton.setDisable(true);
                } catch (Exception ex) {
                    // Show general error
                    fileNameLabel.setText("Error: " + ex.getMessage());
                    runButton.setDisable(true);
                }
            }
        });
        
        // Run button currently doesn't do anything (algorithms not implemented)
        runButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Not Implemented");
            alert.setHeaderText("Algorithm execution not implemented yet");
            alert.setContentText("This functionality will be available when algorithms are implemented.");
            alert.showAndWait();
        });
        
        return panel;
    }
    
    private HBox createAnimationControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button prevButton = new Button("Previous");
        Button nextButton = new Button("Next");
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Slider slider = new Slider(0, 1, 0);
        slider.setPrefWidth(200);
        
        // Style buttons
        String buttonStyle = "-fx-background-color: #90caf9; -fx-text-fill: black;";
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        playButton.setStyle(buttonStyle);
        pauseButton.setStyle(buttonStyle);
        
        // Disable controls initially (no solution loaded)
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        playButton.setDisable(true);
        pauseButton.setDisable(true);
        slider.setDisable(true);
        
        controls.getChildren().addAll(
            prevButton, 
            playButton, 
            pauseButton, 
            nextButton, 
            slider
        );
        
        return controls;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}