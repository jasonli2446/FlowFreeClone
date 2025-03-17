package com.flowfree;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.flowfree.model.Grid;
import com.flowfree.view.GameBoard;
import com.flowfree.controller.GameController;
import com.flowfree.config.GameConfig;

/**
 * Main application entry point for the Flow Free clone game.
 */
public class FlowFreeApplication extends Application {
  @Override
  public void start(Stage primaryStage) {
    // Create the main layout container
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));

    // Create grid and game board
    Grid grid = new Grid(GameConfig.DEFAULT_GRID_SIZE, GameConfig.DEFAULT_GRID_SIZE);
    GameBoard gameBoard = new GameBoard(grid);
    GameController controller = new GameController(grid, gameBoard);

    // Add the game board to the center of the BorderPane
    root.setCenter(gameBoard);

    // Create label for level information
    Label levelLabel = new Label("Level: 1");
    levelLabel.getStyleClass().add("level-info");

    // Create button controls
    HBox controlsBox = new HBox(10);
    controlsBox.setAlignment(Pos.CENTER);
    controlsBox.setPadding(new Insets(10));

    Button prevButton = new Button("Previous Level");
    Button resetButton = new Button("Reset");
    Button nextButton = new Button("Next Level");

    // Set button actions
    prevButton.setOnAction(e -> {
      controller.previousPuzzle();
      levelLabel.setText("Level: " + (controller.getPuzzleService().getCurrentPuzzleIndex() + 1));
    });

    resetButton.setOnAction(e -> {
      controller.resetPuzzle();
    });

    nextButton.setOnAction(e -> {
      controller.nextPuzzle();
      levelLabel.setText("Level: " + (controller.getPuzzleService().getCurrentPuzzleIndex() + 1));
    });

    controlsBox.getChildren().addAll(prevButton, resetButton, nextButton);

    // Create top bar with level info
    HBox topBar = new HBox(levelLabel);
    topBar.setAlignment(Pos.CENTER);
    topBar.setPadding(new Insets(0, 0, 10, 0));

    // Create VBox for top and bottom controls
    VBox controlsContainer = new VBox(10);
    controlsContainer.getChildren().addAll(topBar, controlsBox);

    // Add controls to the bottom of the BorderPane
    root.setBottom(controlsContainer);

    // Load the first puzzle
    controller.loadInitialPuzzle();

    Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

    primaryStage.setTitle(GameConfig.APPLICATION_TITLE);
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}