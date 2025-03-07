package com.flowfree;

import javafx.application.Application;
import javafx.scene.Scene;
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
    Grid grid = new Grid(GameConfig.DEFAULT_GRID_SIZE, GameConfig.DEFAULT_GRID_SIZE);
    GameBoard gameBoard = new GameBoard(grid);
    GameController controller = new GameController(grid, gameBoard);

    Scene scene = new Scene(gameBoard, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
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