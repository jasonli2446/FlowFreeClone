package com.flowfree.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import java.util.function.BiConsumer;

import com.flowfree.model.Grid;
import com.flowfree.model.Cell;

public class GameView extends Pane {
  private Grid grid;
  private GridPane gridPane;
  private Rectangle[][] cellViews;
  private static final int CELL_SIZE = 40;
  // Remove gaps between cells
  private static final int CELL_PADDING = 0;

  private BiConsumer<Integer, Integer> cellClickHandler;
  private BiConsumer<Integer, Integer> cellDragHandler;
  private BiConsumer<Integer, Integer> cellReleaseHandler;
  private BiConsumer<Integer, Integer> cellEnterHandler;

  // Track if mouse button is pressed for drag operations
  private boolean isMousePressed = false;

  // Track the current mouse position in grid coordinates
  private int currentMouseRow = -1;
  private int currentMouseCol = -1;

  public GameView(Grid grid) {
    this.grid = grid;
    initializeView();

    // Add mouse tracking to the entire pane
    setupMouseTracking();
  }

  private void initializeView() {
    gridPane = new GridPane();
    gridPane.setPadding(new Insets(10));
    gridPane.setHgap(CELL_PADDING);
    gridPane.setVgap(CELL_PADDING);

    int rows = grid.getRows();
    int cols = grid.getCols();
    cellViews = new Rectangle[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        Rectangle rect = createCell(i, j);
        gridPane.add(rect, j, i);
        cellViews[i][j] = rect;
      }
    }

    this.getChildren().add(gridPane);
  }

  // Add a new method to track mouse position across the entire grid
  private void setupMouseTracking() {
    // Track mouse movement across the entire pane
    this.setOnMouseMoved(event -> {
      updateMousePosition(event);
    });

    this.setOnMouseDragged(event -> {
      if (isMousePressed) {
        updateMousePosition(event);
      }
    });

    this.setOnMousePressed(event -> {
      isMousePressed = true;
      updateMousePosition(event);
      // Trigger click on the cell at mouse position
      if (cellClickHandler != null && currentMouseRow >= 0 && currentMouseCol >= 0) {
        cellClickHandler.accept(currentMouseRow, currentMouseCol);
      }
    });

    this.setOnMouseReleased(event -> {
      updateMousePosition(event);
      // Trigger release on the cell at mouse position
      if (cellReleaseHandler != null && currentMouseRow >= 0 && currentMouseCol >= 0) {
        cellReleaseHandler.accept(currentMouseRow, currentMouseCol);
      }
      isMousePressed = false;
    });
  }

  // Convert mouse coordinates to grid cell positions
  private void updateMousePosition(MouseEvent event) {
    // Get position relative to grid pane
    double mouseX = event.getX() - gridPane.getLayoutX() - gridPane.getPadding().getLeft();
    double mouseY = event.getY() - gridPane.getLayoutY() - gridPane.getPadding().getTop();

    // Calculate grid cell position
    int col = (int) (mouseX / CELL_SIZE);
    int row = (int) (mouseY / CELL_SIZE);

    // Check if we're within grid bounds
    if (row >= 0 && row < grid.getRows() && col >= 0 && col < grid.getCols()) {
      // Only process if the position has changed
      if (row != currentMouseRow || col != currentMouseCol) {
        int oldRow = currentMouseRow;
        int oldCol = currentMouseCol;

        // Update the current position
        currentMouseRow = row;
        currentMouseCol = col;

        // If we're dragging, trigger the drag handler
        if (isMousePressed) {
          System.out.println("Mouse moved to cell: " + row + "," + col);

          if (cellDragHandler != null) {
            cellDragHandler.accept(row, col);
          }
        }
      }
    }
  }

  private Rectangle createCell(int row, int col) {
    Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
    Cell cell = grid.getCell(row, col);

    // Set cell color based on the model
    updateCellAppearance(rect, cell);

    // We'll now use the main pane's mouse tracking instead of individual cell
    // events
    // This prevents issues with gaps and ensures continuous tracking

    return rect;
  }

  private void updateCellAppearance(Rectangle rect, Cell cell) {
    // Default style for empty cells
    rect.setFill(Color.LIGHTGRAY);
    rect.setStroke(Color.GRAY);
    rect.setStrokeWidth(1);

    // If the cell has a color, set it
    if (cell.getColor() != null) {
      try {
        Color cellColor = Color.valueOf(cell.getColor().toUpperCase());
        rect.setFill(cellColor);

        // Make endpoints appear different (darker border)
        if (cell.isEndpoint()) {
          rect.setStroke(Color.BLACK);
          rect.setStrokeWidth(2);
        }
      } catch (Exception e) {
        // Fallback to default if color name is invalid
        rect.setFill(Color.LIGHTGRAY);
      }
    }
  }

  public void updateView() {
    int rows = grid.getRows();
    int cols = grid.getCols();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        updateCellAppearance(cellViews[i][j], grid.getCell(i, j));
      }
    }
  }

  public void updateCell(int row, int col) {
    updateCellAppearance(cellViews[row][col], grid.getCell(row, col));
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

  public void setOnCellEnterHandler(BiConsumer<Integer, Integer> handler) {
    this.cellEnterHandler = handler;
  }
}