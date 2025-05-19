package gui;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.util.*;

import backend.model.*;
import backend.util.*;
import backend.exception.*;
import backend.algorithm.*;

public class RushHourGUI extends Application {
    private BoardView boardView;
    private File selectedFile;
    private Board board;
    private Label fileNameLabel;
    private Button runButton;
    private Button saveButton;
    private ComboBox<String> algorithmCombo;
    private ComboBox<String> heuristicCombo;
    private Label statsLabel;
    private HBox animationControls;
    private List<Board> solution;
    private PathfindingAlgorithm lastAlgorithm;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        VBox controlPanel = createControlPanel(primaryStage);
        root.setLeft(controlPanel);

        boardView = new BoardView();
        boardView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(boardView);
        scrollPane.setFitToWidth(true);  
        scrollPane.setFitToHeight(true); 

        scrollPane.setPannable(true);
        StackPane centerWrapper = new StackPane(scrollPane);
        centerWrapper.setPadding(new Insets(20));

        animationControls = createAnimationControls();

        VBox centerPanel = new VBox(10);
        centerPanel.getChildren().addAll(centerWrapper, animationControls);
        centerPanel.setAlignment(Pos.CENTER);
        root.setCenter(centerPanel);

        Scene scene = new Scene(root, 800, 600);
        scene.getRoot().setStyle("-fx-font-family: 'Arial'; -fx-background-color: #f5f5f5;");

        primaryStage.setTitle("Rush Hour Puzzle Solver");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createControlPanel(Stage stage) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #e0e0e0; -fx-min-width: 250px;");

        Label titleLabel = new Label("Rush Hour Puzzle Solver");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button fileButton = new Button("Select Puzzle File");
        fileButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");
        fileNameLabel = new Label("No file selected");
        fileNameLabel.setWrapText(true);

        Label algoLabel = new Label("Select Algorithm:");
        algorithmCombo = new ComboBox<>();
        algorithmCombo.getItems().addAll(
            "Greedy Best First Search",
            "Uniform Cost Search (UCS)",
            "A* Search"
        );
        algorithmCombo.setValue("A* Search");
        algorithmCombo.setPrefWidth(200);

        Label heuristicLabel = new Label("Select Heuristic:");
        heuristicCombo = new ComboBox<>();
        heuristicCombo.getItems().addAll(
            "Manhattan Distance", 
            "Blocking Cars"
        );
        heuristicCombo.setValue("Manhattan Distance");
        heuristicCombo.setPrefWidth(200);

        algorithmCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Uniform Cost Search (UCS)".equals(newVal)) {
                heuristicCombo.setDisable(true);
            } else {
                heuristicCombo.setDisable(false);
            }
        });

        runButton = new Button("Run Solver");
        runButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");
        runButton.setPrefWidth(200);
        runButton.setDisable(true);

        saveButton = new Button("Save Solution");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setPrefWidth(200);
        saveButton.setDisable(true);

        statsLabel = new Label("No data");
        statsLabel.setWrapText(true);

        VBox statsBox = new VBox(5);
        statsBox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #cccccc;");
        statsBox.getChildren().addAll(new Label("Statistics:"), statsLabel);

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
            saveButton,
            new Separator(),
            statsBox
        );

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
                    board = Parser.parseFile(selectedFile.getPath());
                    boardView.initializeBoard(board);
                    runButton.setDisable(false);
                    saveButton.setDisable(true);
                    statsLabel.setText("Ready to solve");
                } catch (ParserException ex) {
                    fileNameLabel.setText("Error: " + ex.getMessage());
                    runButton.setDisable(true);
                } catch (Exception ex) {
                    fileNameLabel.setText("Error: " + ex.getMessage());
                    runButton.setDisable(true);
                }
            }
        });

        runButton.setOnAction(e -> {
            if (board == null) return;
            String algorithmName = algorithmCombo.getValue();
            Heuristic heuristic = null;
            if (!"Uniform Cost Search (UCS)".equals(algorithmName)) {
                String heuristicName = heuristicCombo.getValue();
                heuristic = getHeuristicByName(heuristicName);
            }

            PathfindingAlgorithm algorithm = getAlgorithmByName(algorithmName, heuristic);

            if (algorithm != null) {
                try {
                    statsLabel.setText("Running algorithm...");
                    solution = algorithm.solve(board, heuristic);
                    lastAlgorithm = algorithm;

                    statsLabel.setText(String.format(
                        "Algorithm: %s\nHeuristic: %s\nNodes visited: %d\nExecution time: %d ms\nSolution steps: %d",
                        algorithm.getName(),
                        algorithm.getHeuristicName(),
                        algorithm.getNodesVisited(),
                        algorithm.getExecutionTime(),
                        solution.size() - 1
                    ));

                    enableAnimationControls();
                    setupSlider(solution.size());
                    boardView.setSolution(solution);
                    saveButton.setDisable(false);
                } catch (Exception ex) {
                    statsLabel.setText("Error: " + ex.getMessage());
                    showError("Algorithm Error", "Error running algorithm", ex.getMessage());
                }
            } else {
                showError("Algorithm Error", "Invalid algorithm selected", "Please select a valid algorithm");
            }
        });

        saveButton.setOnAction(e -> {
            if (board == null || solution == null || solution.isEmpty()) {
                showError("Save Error", "Nothing to save", "You need to find a solution first before saving.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Solution");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            fileChooser.setInitialFileName("rush_hour_solution.txt");

            File saveFile = fileChooser.showSaveDialog(stage);

            if (saveFile != null) {
                try {
                    String algorithmName = algorithmCombo.getValue();
                    String heuristicName = "None";
                    if (!"Uniform Cost Search (UCS)".equals(algorithmName)) {
                        heuristicName = heuristicCombo.getValue();
                    }

                    int nodesVisited = lastAlgorithm != null ? lastAlgorithm.getNodesVisited() : 0;
                    long executionTime = lastAlgorithm != null ? lastAlgorithm.getExecutionTime() : 0;

                    boolean success = exportSolution(
                        solution, 
                        algorithmName, 
                        heuristicName, 
                        nodesVisited, 
                        executionTime, 
                        saveFile
                    );

                    if (success) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Save Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Solution saved successfully to: " + saveFile.getPath());
                        alert.showAndWait();
                    }
                } catch (Exception ex) {
                    showError("Save Error", "Error saving solution", ex.getMessage());
                }
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

        String buttonStyle = "-fx-background-color: #90caf9; -fx-text-fill: black;";
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        playButton.setStyle(buttonStyle);
        pauseButton.setStyle(buttonStyle);

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

    private boolean exportSolution(List<Board> solution, String algorithm, String heuristic, 
                                  int nodesVisited, long executionTime, File outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("Algoritma: " + algorithm + "\n");
            writer.write("Heuristik: " + heuristic + "\n");
            writer.write("Nodes dikunjungi: " + nodesVisited + "\n");
            writer.write("Waktu eksekusi: " + executionTime + " ms\n\n");

            writer.write("Papan Awal\n");
            writer.write(boardToString(solution.get(0)) + "\n");

            for (int i = 1; i < solution.size(); i++) {
                Board prevBoard = solution.get(i-1);
                Board currBoard = solution.get(i);
                String moveInfo = identifyMove(prevBoard, currBoard);
                writer.write("Gerakan " + i + ": " + moveInfo + "\n");
                writer.write(boardToString(currBoard) + "\n");
            }

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String boardToString(Board board) {
        return board.toString();
    }

    private String identifyMove(Board prevBoard, Board currBoard) {
        for (Car car1 : prevBoard.getCars()) {
            Car car2 = findCarById(currBoard, car1.getId());
            if (car2 != null) {
                if (car1.getRow() != car2.getRow() || car1.getCol() != car2.getCol()) {
                    String direction;
                    if (car1.isHorizontal()) {
                        direction = car2.getCol() > car1.getCol() ? "kanan" : "kiri";
                    } else {
                        direction = car2.getRow() > car1.getRow() ? "bawah" : "atas";
                    }
                    return car1.getId() + "-" + direction;
                }
            }
        }

        return "unknown-unknown";
    }

    private Car findCarById(Board board, char id) {
        for (Car car : board.getCars()) {
            if (car.getId() == id) {
                return car;
            }
        }
        return null;
    }

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

    private void enableAnimationControls() {
        Button prevButton = findNodeById(animationControls, "prevButton");
        Button nextButton = findNodeById(animationControls, "nextButton");
        Button playButton = findNodeById(animationControls, "playButton");
        Button pauseButton = findNodeById(animationControls, "pauseButton");
        Slider slider = findNodeById(animationControls, "stepSlider");

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

    private void setupSlider(int solutionSize) {
        Slider slider = findNodeById(animationControls, "stepSlider");

        if (slider != null) {
            slider.setMin(0);
            slider.setMax(solutionSize - 1);
            slider.setValue(0);
            slider.setBlockIncrement(1);
            slider.setMajorTickUnit(Math.max(1, solutionSize / 10));
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);

            slider.valueProperty().addListener((obs, oldVal, newVal) -> 
                boardView.goToStep(newVal.intValue())
            );

            boardView.currentStepProperty().addListener((obs, oldVal, newVal) -> 
                slider.setValue(newVal.intValue())
            );
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> T findNodeById(Parent parent, String id) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (id.equals(node.getId())) {
                return (T) node;
            }
        }
        return null;
    }

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