package com.flowfree;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.flowfree.model.Grid;
import com.flowfree.view.GameView;
import com.flowfree.controller.GameController;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    Grid grid = new Grid(7, 7);
    GameView view = new GameView(grid);
    GameController controller = new GameController(grid, view);

    Scene scene = new Scene(view, 400, 400);

    primaryStage.setTitle("Flow Free Clone");
    primaryStage.setScene(scene);
    primaryStage.show();

    controller.debugPath();
  }

  public static void main(String[] args) {
    launch(args);
  }
}