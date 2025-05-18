package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import backend.model.Board;
import backend.exception.ParserException;
import backend.model.Parser;
import backend.util.Heuristic;
import backend.algorithm.PathfindingAlgorithm;

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
    private HBox animationControls;
    
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
        
        // Add controls below the board
        animationControls = createAnimationControls();
        
        VBox centerPanel = new VBox(10);
        centerPanel.getChildren().addAll(centerWrapper, animationControls);
        centerPanel.setAlignment(Pos.CENTER);
        root.setCenter(centerPanel);
        
        Scene scene = new Scene(root, 800, 600);
        
        // Add basic styling
        scene.getRoot().setStyle("-fx-font-family: 'Arial'; -fx-background-color: #f5f5f5;");
        
        primaryStage.setTitle("Rush Hour Puzzle Solver");
        primaryStage.setScene(scene);
        primaryStage.show();
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

        // Heuristic selection
        Label heuristicLabel = new Label("Select Heuristic:");
        ComboBox<String> heuristicCombo = new ComboBox<>();
        heuristicCombo.getItems().addAll(
            "Manhattan Distance", 
            "Blocking Cars"
        );
        heuristicCombo.setValue("Manhattan Distance");
        heuristicCombo.setPrefWidth(200);

        // Disable heuristic ComboBox if UCS is selected
        algorithmCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Uniform Cost Search (UCS)".equals(newVal)) {
                heuristicCombo.setDisable(true); // Disable heuristic selection
            } else {
                heuristicCombo.setDisable(false); // Enable heuristic selection
            }
        });

        
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
            new Separator(),
            heuristicLabel,
            heuristicCombo,
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
        
        // Run button event handler with algorithm integration
        runButton.setOnAction(e -> {
            if (board == null) return;
            
            // Get selected algorithm
            String algorithmName = algorithmCombo.getValue();
            // Get selected heuristic if applicable
            Heuristic heuristic = null;
            if (!"Uniform Cost Search (UCS)".equals(algorithmName)) {
                String heuristicName = heuristicCombo.getValue();
                heuristic = getHeuristicByName(heuristicName);
            }
            
            // Initialize the selected algorithm with the chosen heuristic
            PathfindingAlgorithm algorithm = getAlgorithmByName(algorithmName, heuristic);
            
            if (algorithm != null) {
                try {
                    // Show loading message
                    statsLabel.setText("Running algorithm...");
                    
                    // Run the algorithm
                    List<Board> solution = algorithm.solve(board, heuristic);

                    
                    // Update statistics
                    statsLabel.setText(String.format(
                        "Algorithm: %s\nHeuristic: %s\nNodes visited: %d\nExecution time: %d ms\nSolution steps: %d",
                        algorithm.getName(),
                        algorithm.getHeuristicName(),
                        algorithm.getNodesVisited(),
                        algorithm.getExecutionTime(),
                        solution.size() - 1
                    ));
                    
                    // Enable animation controls
                    enableAnimationControls();
                    
                    // Configure slider
                    setupSlider(solution.size());
                    
                    // Visualize solution
                    boardView.setSolution(solution);
                } catch (Exception ex) {
                    // Show error
                    statsLabel.setText("Error: " + ex.getMessage());
                    showError("Algorithm Error", "Error running algorithm", ex.getMessage());
                }
            } else {
                showError("Algorithm Error", "Invalid algorithm selected", "Please select a valid algorithm");
            }
        });
        
        return panel;
    }
    
    private HBox createAnimationControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);
        
        Button prevButton = new Button("Previous");
        prevButton.setId("prevButton");
        
        Button nextButton = new Button("Next");
        nextButton.setId("nextButton");
        
        Button playButton = new Button("Play");
        playButton.setId("playButton");
        
        Button pauseButton = new Button("Pause");
        pauseButton.setId("pauseButton");
        
        Slider slider = new Slider(0, 1, 0);
        slider.setId("stepSlider");
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
    
    /**
     * Gets the appropriate algorithm implementation based on name.
     * @param name Algorithm name
     * @return PathfindingAlgorithm implementation
     */
    private PathfindingAlgorithm getAlgorithmByName(String name, Heuristic heuristic) {
        switch (name) {
            case "Greedy Best First Search":
                return new backend.algorithm.GBFS(heuristic);
            case "Uniform Cost Search (UCS)":
                return new backend.algorithm.UCS(); 
            case "A* Search":
                return new backend.algorithm.AStar(heuristic);
            default:
                return null;
        }
    }

    private Heuristic getHeuristicByName(String name) {
        switch (name) {
            case "Manhattan Distance":
                return new backend.util.HeuristicManhattan();
            case "Blocking Cars":
                return new backend.util.HeuristicBlocking();
            default:
                return null;
        }
    }
    
    /**
     * Enables animation controls after a solution is found.
     */
    private void enableAnimationControls() {
        // Find controls by ID
        Button prevButton = findNodeById(animationControls, "prevButton");
        Button nextButton = findNodeById(animationControls, "nextButton");
        Button playButton = findNodeById(animationControls, "playButton");
        Button pauseButton = findNodeById(animationControls, "pauseButton");
        Slider slider = findNodeById(animationControls, "stepSlider");
        
        // Enable controls
        if (prevButton != null) {
            prevButton.setDisable(false);
            prevButton.setOnAction(e -> boardView.previousStep());
        }
        
        if (nextButton != null) {
            nextButton.setDisable(false);
            nextButton.setOnAction(e -> boardView.nextStep());
        }
        
        if (playButton != null) {
            playButton.setDisable(false);
            playButton.setOnAction(e -> boardView.playAnimation());
        }
        
        if (pauseButton != null) {
            pauseButton.setDisable(false);
            pauseButton.setOnAction(e -> boardView.pauseAnimation());
        }
        
        if (slider != null) {
            slider.setDisable(false);
        }
    }
    
    /**
     * Configures the slider for navigating through solution steps.
     * @param solutionSize Number of states in the solution
     */
    private void setupSlider(int solutionSize) {
        Slider slider = findNodeById(animationControls, "stepSlider");
        
        if (slider != null) {
            // Configure slider properties
            slider.setMin(0);
            slider.setMax(solutionSize - 1);
            slider.setValue(0);
            slider.setBlockIncrement(1);
            slider.setMajorTickUnit(Math.max(1, solutionSize / 10));
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
            
            // Bind slider to board view
            slider.valueProperty().addListener((obs, oldVal, newVal) -> 
                boardView.goToStep(newVal.intValue())
            );
            
            // Bind board view to slider
            boardView.currentStepProperty().addListener((obs, oldVal, newVal) -> 
                slider.setValue(newVal.intValue())
            );
        }
    }
    
    /**
     * Helper method to find a node by ID in a parent.
     * @param parent Parent node
     * @param id Node ID to find
     * @return The node if found, null otherwise
     */
    @SuppressWarnings("unchecked")
    private <T extends Node> T findNodeById(Parent parent, String id) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (id.equals(node.getId())) {
                return (T) node;
            }
        }
        return null;
    }
    
    /**
     * Shows an error dialog.
     * @param title Dialog title
     * @param header Header text
     * @param content Content text
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}