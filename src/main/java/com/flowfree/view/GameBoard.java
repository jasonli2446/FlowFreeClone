package com.flowfree.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import java.util.function.BiConsumer;

import com.flowfree.config.GameConfig;
import com.flowfree.model.Grid;
import com.flowfree.model.Cell;
import com.flowfree.model.Position;

/**
 * Visual representation of the game board.
 */
public class GameBoard extends Pane {
  private Grid grid;
  private GridPane gridPane;
  private Rectangle[][] cellViews;

  private BiConsumer<Integer, Integer> cellClickHandler;
  private BiConsumer<Integer, Integer> cellDragHandler;
  private BiConsumer<Integer, Integer> cellReleaseHandler;

  // Track if mouse button is pressed for drag operations
  private boolean isMousePressed = false;

  // Track the current mouse position in grid coordinates
  private Position currentMousePosition = null;

  public GameBoard(Grid grid) {
    this.grid = grid;
    this.gridPane = new GridPane();
    this.cellViews = new Rectangle[grid.getRows()][grid.getCols()];

    initializeView();
    setupMouseTracking();
  }

  private void initializeView() {
    gridPane.setPadding(new Insets(10));
    gridPane.setHgap(GameConfig.CELL_PADDING);
    gridPane.setVgap(GameConfig.CELL_PADDING);

    int rows = grid.getRows();
    int cols = grid.getCols();

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Rectangle rect = createCellView(row, col);
        gridPane.add(rect, col, row);
        cellViews[row][col] = rect;
      }
    }

    this.getChildren().add(gridPane);
  }

  private Rectangle createCellView(int row, int col) {
    Rectangle rect = new Rectangle(GameConfig.CELL_SIZE, GameConfig.CELL_SIZE);
    updateCellAppearance(rect, grid.getCell(row, col));
    return rect;
  }

  private void updateCellAppearance(Rectangle rect, Cell cell) {
    // Default style for empty cells
    rect.setFill(javafx.scene.paint.Color.LIGHTGRAY);
    rect.setStroke(javafx.scene.paint.Color.GRAY);
    rect.setStrokeWidth(1);

    // If the cell has a color, set it
    if (cell.getColor() != null) {
      rect.setFill(cell.getColor().getPaint());

      // Make endpoints appear different (darker border)
      if (cell.isEndpoint()) {
        rect.setStroke(javafx.scene.paint.Color.BLACK);
        rect.setStrokeWidth(2);
      }
    }
  }

  private void setupMouseTracking() {
    this.setOnMouseMoved(this::updateMousePosition);
    this.setOnMouseDragged(event -> {
      if (isMousePressed) {
        updateMousePosition(event);
      }
    });

    this.setOnMousePressed(event -> {
      isMousePressed = true;
      updateMousePosition(event);
      notifyCellClick();
    });

    this.setOnMouseReleased(event -> {
      updateMousePosition(event);
      notifyCellRelease();
      isMousePressed = false;
    });
  }

  private void updateMousePosition(MouseEvent event) {
    double mouseX = event.getX() - gridPane.getLayoutX() - gridPane.getPadding().getLeft();
    double mouseY = event.getY() - gridPane.getLayoutY() - gridPane.getPadding().getTop();

    int col = (int) (mouseX / GameConfig.CELL_SIZE);
    int row = (int) (mouseY / GameConfig.CELL_SIZE);

    if (grid.isValidPosition(row, col)) {
      Position newPosition = new Position(row, col);

      if (currentMousePosition == null || !currentMousePosition.equals(newPosition)) {
        currentMousePosition = newPosition;

        if (isMousePressed) {
          notifyCellDrag();
        }
      }
    }
  }

  private void notifyCellClick() {
    if (cellClickHandler != null && currentMousePosition != null) {
      cellClickHandler.accept(
          currentMousePosition.getRow(),
          currentMousePosition.getCol());
    }
  }

  private void notifyCellDrag() {
    if (cellDragHandler != null && currentMousePosition != null) {
      cellDragHandler.accept(
          currentMousePosition.getRow(),
          currentMousePosition.getCol());
    }
  }

  private void notifyCellRelease() {
    if (cellReleaseHandler != null && currentMousePosition != null) {
      cellReleaseHandler.accept(
          currentMousePosition.getRow(),
          currentMousePosition.getCol());
    }
  }

  public void updateView() {
    for (int row = 0; row < grid.getRows(); row++) {
      for (int col = 0; col < grid.getCols(); col++) {
        updateCellAppearance(cellViews[row][col], grid.getCell(row, col));
      }
    }
  }

  public void updateCell(int row, int col) {
    if (grid.isValidPosition(row, col)) {
      updateCellAppearance(cellViews[row][col], grid.getCell(row, col));
    }
  }

  /**
   * Replaces the grid with a new grid of potentially different size.
   */
  public void replaceGrid(Grid newGrid) {
    this.grid = newGrid;

    // Remove existing grid view
    this.getChildren().remove(gridPane);

    // Create new grid with appropriate size
    int rows = grid.getRows();
    int cols = grid.getCols();
    this.cellViews = new Rectangle[rows][cols];

    // Clear and rebuild the gridPane
    gridPane.getChildren().clear();

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Rectangle rect = createCellView(row, col);
        gridPane.add(rect, col, row);
        cellViews[row][col] = rect;
      }
    }

    this.getChildren().add(gridPane);
    updateView();
  }

  // Event handler setters
  public void setOnCellClickHandler(BiConsumer<Integer, Integer> handler) {
    this.cellClickHandler = handler;
  }

  public void setOnCellDragHandler(BiConsumer<Integer, Integer> handler) {
    this.cellDragHandler = handler;
  }

  public void setOnCellReleaseHandler(BiConsumer<Integer, Integer> handler) {
    this.cellReleaseHandler = handler;
  }
}