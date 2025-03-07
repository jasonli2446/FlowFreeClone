package com.flowfree.controller;

import com.flowfree.model.Grid;
import com.flowfree.model.Cell;
import com.flowfree.model.Color;
import com.flowfree.model.Position;
import com.flowfree.view.GameBoard;
import com.flowfree.service.PathFinderService;
import com.flowfree.service.PuzzleService;

import java.util.logging.Logger;

/**
 * Controller handling game logic and user interactions.
 */
public class GameController {
  private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());

  private final Grid grid;
  private final GameBoard gameBoard;
  private final PathFinderService pathFinderService;
  private final PuzzleService puzzleService;

  // Active drawing state
  private Color currentColor;
  private Cell lastCellInPath;
  private boolean isDrawing;
  private Position currentPosition;

  public GameController(Grid grid, GameBoard gameBoard) {
    this.grid = grid;
    this.gameBoard = gameBoard;
    this.pathFinderService = new PathFinderService(grid);
    this.puzzleService = new PuzzleService();
    this.isDrawing = false;

    setupEventHandlers();
    loadInitialPuzzle();
  }

  private void setupEventHandlers() {
    gameBoard.setOnCellClickHandler(this::handleCellClick);
    gameBoard.setOnCellDragHandler(this::handleCellDrag);
    gameBoard.setOnCellReleaseHandler(this::handleCellRelease);
  }

  private void loadInitialPuzzle() {
    // In a production app, this would load from PuzzleService
    // For now, we create a test puzzle
    createTestPuzzle();
    gameBoard.updateView();
  }

  private void createTestPuzzle() {
    grid.addEndpoint(0, 0, Color.RED);
    grid.addEndpoint(4, 4, Color.RED);

    grid.addEndpoint(0, 4, Color.BLUE);
    grid.addEndpoint(4, 0, Color.BLUE);

    grid.addEndpoint(2, 0, Color.GREEN);
    grid.addEndpoint(2, 4, Color.GREEN);
  }

  public void handleCellClick(int row, int col) {
    Cell cell = grid.getCell(row, col);
    if (cell == null)
      return;

    LOGGER.fine("Cell clicked: " + row + "," + col);

    // Handle endpoint click - start a new path
    if (cell.isEndpoint()) {
      startNewPath(cell);
      return;
    }

    // Handle path end click - continue a path
    if (cell.isPartOfPath() && !cell.isEndpoint()) {
      continuePathFromCell(cell);
    }
  }

  private void startNewPath(Cell cell) {
    // Clear any existing path of this color
    grid.clearPath(cell.getColor());

    // Start a new path
    currentColor = cell.getColor();
    lastCellInPath = cell;
    isDrawing = true;
    currentPosition = cell.getPosition();

    LOGGER.fine("Starting path from endpoint: " + cell);

    // Update the view
    gameBoard.updateView();
  }

  private void continuePathFromCell(Cell cell) {
    // Find if this cell is at the end of a path
    Cell pathEndCell = grid.findLastCellInPath(cell.getColor());

    // If this is indeed the end of a path, continue from here
    if (pathEndCell != null &&
        pathEndCell.getRow() == cell.getRow() &&
        pathEndCell.getCol() == cell.getCol()) {

      currentColor = cell.getColor();
      lastCellInPath = cell;
      isDrawing = true;
      currentPosition = cell.getPosition();

      LOGGER.fine("Continuing path from: " + cell);
    } else {
      LOGGER.fine("Clicked on middle of path (not the end)");
    }
  }

  public void handleCellDrag(int row, int col) {
    if (!isDrawing)
      return;

    Position newPosition = new Position(row, col);

    // Skip if position hasn't changed
    if (currentPosition != null && currentPosition.equals(newPosition)) {
      return;
    }

    LOGGER.fine("Drag detected at: " + row + "," + col);

    Cell targetCell = grid.getCell(row, col);
    if (targetCell == null || lastCellInPath == null)
      return;

    // Check if this is a valid move
    if (isValidNextCell(lastCellInPath, targetCell)) {
      processCellDrag(targetCell, row, col);
    }
  }

  private void processCellDrag(Cell targetCell, int row, int col) {
    // If the target cell is an endpoint of the same color, complete the path
    if (targetCell.isEndpoint() && targetCell.getColor() == currentColor) {
      completeCurrentPath(targetCell);
    } else if (targetCell.isEmpty()) {
      // Set the color of the target cell
      targetCell.setColor(currentColor);
      targetCell.setPartOfPath(true);
      lastCellInPath = targetCell;
      LOGGER.fine("Added cell to path: " + row + "," + col);
    }

    // Update tracking position
    currentPosition = new Position(row, col);

    // Update the view
    gameBoard.updateCell(row, col);
  }

  private void completeCurrentPath(Cell targetCell) {
    targetCell.setPartOfPath(true);
    isDrawing = false;
    lastCellInPath = null;
    LOGGER.fine("Path completed!");

    // Check if the puzzle is complete
    if (grid.isComplete()) {
      LOGGER.info("Puzzle solved!");
      // Additional win condition handling can go here
    }
  }

  public void handleCellRelease(int row, int col) {
    // End the current path drawing
    isDrawing = false;
    lastCellInPath = null;
    currentPosition = null;
    LOGGER.fine("Mouse released at: " + row + "," + col);
  }

  private boolean isValidNextCell(Cell current, Cell next) {
    if (current == null || next == null) {
      return false;
    }

    // Check if cells are adjacent
    boolean isAdjacent = current.getPosition().isAdjacent(next.getPosition());

    // The next cell should be empty or an endpoint of the same color
    boolean isValidTarget = next.isEmpty() ||
        (next.isEndpoint() && next.getColor() == currentColor);

    return isAdjacent && isValidTarget;
  }
}