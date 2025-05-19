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
        algorithmCombo.getItems().addAll("Greedy Best First Search", "Uniform Cost Search (UCS)", "A* Search");
        algorithmCombo.setValue("A* Search");
        algorithmCombo.setPrefWidth(200);

        Label heuristicLabel = new Label("Select Heuristic:");
        heuristicCombo = new ComboBox<>();
        heuristicCombo.getItems().addAll("Manhattan Distance", "Blocking Cars");
        heuristicCombo.setValue("Manhattan Distance");
        heuristicCombo.setPrefWidth(200);

        algorithmCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            heuristicCombo.setDisable("Uniform Cost Search (UCS)".equals(newVal));
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
            titleLabel, new Separator(),
            fileButton, fileNameLabel, new Separator(),
            algoLabel, algorithmCombo, new Separator(),
            heuristicLabel, heuristicCombo,
            runButton, saveButton, new Separator(), statsBox
        );

        fileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Rush Hour Puzzle File");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            selectedFile = chooser.showOpenDialog(stage);

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
                }
            }
        });

        runButton.setOnAction(e -> {
            if (board == null) return;
            String algoName = algorithmCombo.getValue();
            Heuristic heuristic = !"Uniform Cost Search (UCS)".equals(algoName)
                ? getHeuristicByName(heuristicCombo.getValue()) : null;

            PathfindingAlgorithm algorithm = getAlgorithmByName(algoName, heuristic);
            if (algorithm != null) {
                try {
                    statsLabel.setText("Running algorithm...");
                    solution = algorithm.solve(board, heuristic);
                    lastAlgorithm = algorithm;

                    if (solution == null || solution.isEmpty()) {
                        statsLabel.setText(String.format(
                            "Algorithm: %s\nHeuristic: %s\nNodes visited: %d\nExecution time: %d ms\nNo solution found.",
                            algorithm.getName(),
                            algorithm.getHeuristicName(),
                            algorithm.getNodesVisited(),
                            algorithm.getExecutionTime()
                        ));
                        showError("No Solution", "No solution could be found", "The puzzle has no valid solution path.");
                        saveButton.setDisable(true);
                        return;
                    }

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
            }
        });

        saveButton.setOnAction(e -> {
            if (board == null || solution == null || solution.isEmpty()) {
                showError("Save Error", "Nothing to save", "You need to find a solution first before saving.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Solution");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            chooser.setInitialFileName("rush_hour_solution.txt");
            File saveFile = chooser.showSaveDialog(stage);

            if (saveFile != null) {
                try {
                    String algo = algorithmCombo.getValue();
                    String heur = "Uniform Cost Search (UCS)".equals(algo) ? "None" : heuristicCombo.getValue();
                    boolean success = exportSolution(
                        solution, algo, heur,
                        lastAlgorithm.getNodesVisited(),
                        lastAlgorithm.getExecutionTime(),
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
        Button nextButton = new Button("Next");
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Slider slider = new Slider(0, 1, 0);

        prevButton.setId("prevButton");
        nextButton.setId("nextButton");
        playButton.setId("playButton");
        pauseButton.setId("pauseButton");
        slider.setId("stepSlider");
        slider.setPrefWidth(200);

        String style = "-fx-background-color: #90caf9; -fx-text-fill: black;";
        prevButton.setStyle(style);
        nextButton.setStyle(style);
        playButton.setStyle(style);
        pauseButton.setStyle(style);

        prevButton.setDisable(true);
        nextButton.setDisable(true);
        playButton.setDisable(true);
        pauseButton.setDisable(true);
        slider.setDisable(true);

        controls.getChildren().addAll(prevButton, playButton, pauseButton, nextButton, slider);
        return controls;
    }

    private boolean exportSolution(List<Board> solution, String algo, String heur, int nodesVisited, long time, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Algorithm: " + algo + "\n");
            writer.write("Heuristic: " + heur + "\n");
            writer.write("Nodes visited: " + nodesVisited + "\n");
            writer.write("Execution time: " + time + " ms\n\n");

            writer.write("Papan Awal:\n");
            writer.write(solution.get(0).toString() + "\n");

            for (int i = 1; i < solution.size(); i++) {
                writer.write("Gerakan " + i + ": " + identifyMove(solution.get(i - 1), solution.get(i)) + "\n");
                writer.write(solution.get(i).toString() + "\n");
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String identifyMove(Board prev, Board curr) {
        for (Car car1 : prev.getCars()) {
            Car car2 = findCarById(curr, car1.getId());
            if (car2 != null && (car1.getRow() != car2.getRow() || car1.getCol() != car2.getCol())) {
                String dir = car1.isHorizontal() ?
                    (car2.getCol() > car1.getCol() ? "Kanan" : "Kiri") :
                    (car2.getRow() > car1.getRow() ? "Bawah" : "Atas");
                return car1.getId() + " - " + dir;
            }
        }
        return "unknown";
    }

    private Car findCarById(Board board, char id) {
        return board.getCars().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private PathfindingAlgorithm getAlgorithmByName(String name, Heuristic heuristic) {
        return switch (name) {
            case "Greedy Best First Search" -> new GBFS(heuristic);
            case "Uniform Cost Search (UCS)" -> new UCS();
            case "A* Search" -> new AStar(heuristic);
            default -> null;
        };
    }

    private Heuristic getHeuristicByName(String name) {
        return switch (name) {
            case "Manhattan Distance" -> new HeuristicManhattan();
            case "Blocking Cars" -> new HeuristicBlocking();
            default -> null;
        };
    }

    private void enableAnimationControls() {
        Button prev = findNodeById(animationControls, "prevButton");
        Button next = findNodeById(animationControls, "nextButton");
        Button play = findNodeById(animationControls, "playButton");
        Button pause = findNodeById(animationControls, "pauseButton");
        Slider slider = findNodeById(animationControls, "stepSlider");

        if (prev != null) {
            prev.setDisable(false);
            prev.setOnAction(e -> boardView.previousStep());
        }

        if (next != null) {
            next.setDisable(false);
            next.setOnAction(e -> boardView.nextStep());
        }

        if (play != null) {
            play.setDisable(false);
            play.setOnAction(e -> boardView.playAnimation());
        }

        if (pause != null) {
            pause.setDisable(false);
            pause.setOnAction(e -> boardView.pauseAnimation());
        }

        if (slider != null) {
            slider.setDisable(false);
        }
    }

    private void setupSlider(int size) {
        Slider slider = findNodeById(animationControls, "stepSlider");
        if (slider != null) {
            slider.setMin(0);
            slider.setMax(size - 1);
            slider.setValue(0);
            slider.setBlockIncrement(1);
            slider.setMajorTickUnit(Math.max(1, size / 10));
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);

            slider.valueProperty().addListener((obs, oldVal, newVal) ->
                boardView.goToStep(newVal.intValue()));

            boardView.currentStepProperty().addListener((obs, oldVal, newVal) ->
                slider.setValue(newVal.intValue()));
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
