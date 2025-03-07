package com.flowfree.controller;

import com.flowfree.model.Grid;
import com.flowfree.model.Cell;
import com.flowfree.view.GameView;
import java.util.*;

public class GameController {
  private Grid grid;
  private GameView view;
  private String currentColor;
  private Cell lastCellInPath;
  private boolean isDrawing;

  // Track the current cell during dragging
  private int currentPathRow = -1;
  private int currentPathCol = -1;

  public GameController(Grid grid, GameView view) {
    this.grid = grid;
    this.view = view;
    this.isDrawing = false;

    // Set up event handlers in the view
    setupEventHandlers();

    // Initialize a simple level for testing
    initializeTestLevel();
  }

  private void setupEventHandlers() {
    view.setOnCellClickHandler((row, col) -> handleCellClick(row, col));
    view.setOnCellDragHandler((row, col) -> handleCellDrag(row, col));
    view.setOnCellReleaseHandler((row, col) -> handleCellRelease(row, col));
  }

  private void initializeTestLevel() {
    // Add some endpoints for testing
    grid.addEndpoint(0, 0, "RED");
    grid.addEndpoint(4, 4, "RED");

    grid.addEndpoint(0, 4, "BLUE");
    grid.addEndpoint(4, 0, "BLUE");

    grid.addEndpoint(2, 0, "GREEN");
    grid.addEndpoint(2, 4, "GREEN");

    // Update the view
    view.updateView();
  }

  public void handleCellClick(int row, int col) {
    Cell cell = grid.getCell(row, col);

    System.out.println("Cell clicked: " + row + "," + col);

    // If cell is an endpoint, start drawing a path
    if (cell != null && cell.isEndpoint()) {
      // Clear any existing path of this color
      grid.clearPath(cell.getColor());

      // Start a new path
      currentColor = cell.getColor();
      lastCellInPath = cell;
      isDrawing = true;

      // Initialize drag position tracking
      currentPathRow = row;
      currentPathCol = col;

      System.out.println("Starting path from endpoint: " + cell);

      // Update the view
      view.updateView();
    }
    // Check if cell is the end of a path
    else if (cell != null && cell.isPartOfPath() && !cell.isEndpoint()) {
      // Find the path end cells for this color
      Cell endCell = findLastCellInPath(cell.getColor());

      // If this is indeed the end of a path, continue from here
      if (endCell != null && endCell.getX() == row && endCell.getY() == col) {
        currentColor = cell.getColor();
        lastCellInPath = cell;
        isDrawing = true;

        // Initialize drag position tracking
        currentPathRow = row;
        currentPathCol = col;

        System.out.println("Continuing path from: " + cell);
      } else {
        System.out.println("Clicked on middle of path (not the end)");
      }
    } else {
      System.out.println("Clicked on non-endpoint cell");
    }
  }

  private Cell findLastCellInPath(String color) {
    List<Cell> pathCells = new ArrayList<>();

    // Get all cells in the path (non-endpoints)
    for (int r = 0; r < grid.getRows(); r++) {
      for (int c = 0; c < grid.getCols(); c++) {
        Cell cell = grid.getCell(r, c);
        if (cell.isPartOfPath() && !cell.isEndpoint() &&
            color.equals(cell.getColor())) {
          pathCells.add(cell);
        }
      }
    }

    if (pathCells.isEmpty()) {
      return null;
    }

    // Find cells with only one adjacent cell of the same color (path end)
    for (Cell cell : pathCells) {
      int adjacentSameColorCells = 0;
      for (Cell adj : grid.getAdjacentCells(cell.getX(), cell.getY())) {
        if (adj.getColor() != null && adj.getColor().equals(color)) {
          adjacentSameColorCells++;
        }
      }

      // A cell at the end of a path will have exactly one adjacent cell
      // of the same color (or an endpoint of the same color)
      if (adjacentSameColorCells == 1) {
        return cell;
      }
    }

    return null;
  }

  public void handleCellDrag(int row, int col) {
    if (!isDrawing)
      return;

    System.out.println("Drag detected at: " + row + "," + col);

    if (row == currentPathRow && col == currentPathCol) {
      return;
    }

    // Check if the new cell is adjacent to the last cell in the path
    Cell targetCell = grid.getCell(row, col);

    if (targetCell == null || lastCellInPath == null) {
      return;
    }

    // Check if this is a valid move
    boolean isValid = isValidNextCell(lastCellInPath, targetCell);
    System.out.println("Valid move: " + isValid);

    if (isValid) {
      // If the target cell is an endpoint of the same color, complete the path
      if (targetCell.isEndpoint() && targetCell.getColor().equals(currentColor)) {
        targetCell.setPartOfPath(true);
        isDrawing = false;
        lastCellInPath = null;
        System.out.println("Path completed!");

        // Check if the puzzle is complete
        if (grid.isComplete()) {
          System.out.println("Puzzle solved!");
        }
      } else if (targetCell.isEmpty()) {
        // Set the color of the target cell
        targetCell.setColor(currentColor);
        targetCell.setPartOfPath(true);
        lastCellInPath = targetCell;
        System.out.println("Added cell to path: " + row + "," + col);
      }

      // Update tracking position only on valid moves
      currentPathRow = row;
      currentPathCol = col;

      // Update the view
      view.updateCell(row, col);
    }
  }

  public void handleCellRelease(int row, int col) {
    // End the current path drawing
    isDrawing = false;
    lastCellInPath = null;
    currentPathRow = -1;
    currentPathCol = -1;
    System.out.println("Mouse released at: " + row + "," + col);
  }

  private boolean isValidNextCell(Cell current, Cell next) {
    if (current == null || next == null) {
      return false;
    }

    // Check if cells are adjacent
    int rowDiff = Math.abs(current.getX() - next.getX());
    int colDiff = Math.abs(current.getY() - next.getY());

    // Valid if exactly one step in any direction (not diagonal)
    boolean isAdjacent = (rowDiff + colDiff == 1);

    // The next cell should be empty or an endpoint of the same color
    boolean isValidTarget = next.isEmpty() ||
        (next.isEndpoint() && next.getColor().equals(currentColor));

    return isAdjacent && isValidTarget;
  }

  public void debugPath() {
    System.out.println("----- PATH DEBUG INFO -----");
    System.out.println("Current color: " + currentColor);
    System.out.println("Is drawing: " + isDrawing);
    System.out.println("Last cell: " +
        (lastCellInPath != null ? lastCellInPath.getX() + "," + lastCellInPath.getY() : "null"));
    System.out.println("Current path position: " + currentPathRow + "," + currentPathCol);
    System.out.println("--------------------------");
  }
}