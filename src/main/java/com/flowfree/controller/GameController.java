package com.flowfree.controller;

import com.flowfree.model.Grid;
import com.flowfree.model.Cell;
import com.flowfree.model.Color;
import com.flowfree.model.Position;
import com.flowfree.model.Puzzle;
import com.flowfree.view.GameBoard;
import com.flowfree.service.PathFinderService;
import com.flowfree.service.PuzzleService;
import com.flowfree.service.GameStateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Controller handling game logic and user interactions.
 */
public class GameController {
  private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());

  private Grid grid;
  private final GameBoard gameBoard;
  private PathFinderService pathFinderService;
  private final PuzzleService puzzleService;
  private final GameStateService gameStateService;

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
    this.gameStateService = new GameStateService(grid);
    this.isDrawing = false;

    // Store reference to this controller in the game board
    gameBoard.getProperties().put("controller", this);

    setupEventHandlers();
  }

  private void setupEventHandlers() {
    gameBoard.setOnCellClickHandler(this::handleCellClick);
    gameBoard.setOnCellDragHandler(this::handleCellDrag);
    gameBoard.setOnCellReleaseHandler(this::handleCellRelease);
  }

  /**
   * Loads the initial puzzle when the game starts
   */
  public void loadInitialPuzzle() {
    Puzzle puzzle = puzzleService.getCurrentPuzzle();
    if (puzzle == null) {
      LOGGER.warning("No puzzles available!");
      return;
    }

    // Check if we need to update the grid size
    if (grid.getRows() != puzzle.getRows() || grid.getCols() != puzzle.getCols()) {
      updateGridSize(puzzle.getRows(), puzzle.getCols());
    }

    if (!puzzleService.loadPuzzle(grid, 0)) {
      LOGGER.warning("Failed to load initial puzzle!");
    }
    gameBoard.updateView();
    gameStateService.resetStats();
  }

  /**
   * Updates the grid to a new size and reinitializes components
   */
  private void updateGridSize(int rows, int cols) {
    LOGGER.info("Updating grid size to " + rows + "x" + cols);

    // Create a new grid with the required size
    this.grid = new Grid(rows, cols);

    // Update dependent components
    this.pathFinderService = new PathFinderService(grid);
    this.gameStateService.setGrid(grid);

    // Update the game board with the new grid
    gameBoard.replaceGrid(grid);
  }

  /**
   * Resets the current puzzle to its initial state
   */
  public void resetPuzzle() {
    int currentIndex = puzzleService.getCurrentPuzzleIndex();
    LOGGER.info("Resetting puzzle at index: " + currentIndex);

    Puzzle currentPuzzle = puzzleService.getCurrentPuzzle();
    if (currentPuzzle == null) {
      LOGGER.warning("No current puzzle to reset!");
      return;
    }

    // First, clear all non-endpoint cells
    for (int row = 0; row < grid.getRows(); row++) {
      for (int col = 0; col < grid.getCols(); col++) {
        Cell cell = grid.getCell(row, col);
        if (cell != null && !cell.isEndpoint()) {
          cell.clear();
        }
      }
    }

    // Now make sure all endpoints are properly marked as not part of a path
    for (Puzzle.Endpoint endpoint : currentPuzzle.getEndpoints()) {
      Cell startCell = grid.getCell(endpoint.startRow, endpoint.startCol);
      Cell endCell = grid.getCell(endpoint.endRow, endpoint.endCol);

      if (startCell != null) {
        startCell.setPartOfPath(false);
      }

      if (endCell != null) {
        endCell.setPartOfPath(false);
      }
    }

    // Reset game state
    gameBoard.updateView();
    gameStateService.resetStats();
    isDrawing = false;
    lastCellInPath = null;
    currentPosition = null;
    currentColor = null;
    LOGGER.info("Puzzle reset successfully");
  }

  /**
   * Advances to the next puzzle
   */
  public void nextPuzzle() {
    LOGGER.info("Moving to next puzzle");

    int nextIndex = puzzleService.getCurrentPuzzleIndex() + 1;
    if (nextIndex < puzzleService.getPuzzleCount()) {
      Puzzle nextPuzzle = puzzleService.getPuzzle(nextIndex);

      // Check if we need to update the grid size
      if (grid.getRows() != nextPuzzle.getRows() || grid.getCols() != nextPuzzle.getCols()) {
        updateGridSize(nextPuzzle.getRows(), nextPuzzle.getCols());
      }

      if (puzzleService.loadPuzzle(grid, nextIndex)) {
        gameBoard.updateView();
        gameStateService.resetStats();
        LOGGER.info("Advanced to next puzzle");
      }
    } else {
      LOGGER.info("No more puzzles available");
    }
  }

  /**
   * Returns to the previous puzzle
   */
  public void previousPuzzle() {
    LOGGER.info("Moving to previous puzzle");

    int prevIndex = puzzleService.getCurrentPuzzleIndex() - 1;
    if (prevIndex >= 0) {
      Puzzle prevPuzzle = puzzleService.getPuzzle(prevIndex);

      // Check if we need to update the grid size
      if (grid.getRows() != prevPuzzle.getRows() || grid.getCols() != prevPuzzle.getCols()) {
        updateGridSize(prevPuzzle.getRows(), prevPuzzle.getCols());
      }

      if (puzzleService.loadPuzzle(grid, prevIndex)) {
        gameBoard.updateView();
        gameStateService.resetStats();
        LOGGER.info("Returned to previous puzzle");
      }
    } else {
      LOGGER.info("Already at first puzzle");
    }
  }

  public void handleCellClick(int row, int col) {
    Cell cell = grid.getCell(row, col);
    if (cell == null)
      return;

    LOGGER.fine("Cell clicked: " + row + "," + col);

    // Handle endpoint click - start a new path
    if (cell.isEndpoint()) {
      startNewPath(cell);
      gameStateService.incrementMoves(); // Count starting a new path as a move
      return;
    }

    // Handle path end click - continue a path
    if (cell.isPartOfPath() && !cell.isEndpoint()) {
      continuePathFromCell(cell);
      gameStateService.incrementMoves(); // Count continuing a path as a move
    }
  }

  /**
   * Shows the solution for the current puzzle.
   */
  public void showSolution() {
    Puzzle currentPuzzle = puzzleService.getCurrentPuzzle();
    if (currentPuzzle == null) {
      LOGGER.warning("No puzzle available to solve!");
      return;
    }

    LOGGER.info("Showing solution");

    // Clear all non-endpoint cells first
    for (int r = 0; r < grid.getRows(); r++) {
      for (int c = 0; c < grid.getCols(); c++) {
        Cell cell = grid.getCell(r, c);
        if (!cell.isEndpoint()) {
          cell.clear();
        }
      }
    }

    // For each color, find a path between its endpoints
    for (Puzzle.Endpoint endpoint : currentPuzzle.getEndpoints()) {
      Color color = endpoint.color;
      Position start = new Position(endpoint.startRow, endpoint.startCol);
      Position end = new Position(endpoint.endRow, endpoint.endCol);

      // Find a path between the endpoints
      List<Position> path = findPath(start, end, grid.getRows(), grid.getCols(), grid);

      // Apply the path to the grid
      if (!path.isEmpty()) {
        for (Position pos : path) {
          Cell cell = grid.getCell(pos);
          if (cell != null && !cell.isEndpoint()) {
            cell.setColor(color);
            cell.setPartOfPath(true);
          }
        }
      } else {
        LOGGER.warning("Couldn't find path for color " + color);
      }
    }

    // Update the view to show the solution
    gameBoard.updateView();
  }

  /**
   * Finds a path between two positions.
   */
  private List<Position> findPath(Position start, Position end, int rows, int cols, Grid grid) {
    // Use BFS to find the shortest path
    Queue<Position> queue = new LinkedList<>();
    Map<Position, Position> parentMap = new HashMap<>();
    boolean[][] visited = new boolean[rows][cols];

    queue.add(start);
    visited[start.getRow()][start.getCol()] = true;

    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } }; // Up, Right, Down, Left

    while (!queue.isEmpty()) {
      Position current = queue.poll();

      if (current.equals(end)) {
        // Path found, reconstruct it
        List<Position> path = new ArrayList<>();
        Position pos = current;

        while (!pos.equals(start)) {
          path.add(pos);
          pos = parentMap.get(pos);
        }

        Collections.reverse(path);
        return path;
      }

      // Try all four directions
      for (int[] dir : directions) {
        int newRow = current.getRow() + dir[0];
        int newCol = current.getCol() + dir[1];
        Position next = new Position(newRow, newCol);

        // Check if the position is valid and not visited
        if (isValidPosition(newRow, newCol, rows, cols) && !visited[newRow][newCol]) {
          Cell cell = grid.getCell(newRow, newCol);

          // Only consider empty cells or the destination endpoint
          if (cell.isEmpty() || next.equals(end)) {
            queue.add(next);
            visited[newRow][newCol] = true;
            parentMap.put(next, current);
          }
        }
      }
    }

    // No path found
    return Collections.emptyList();
  }

  /**
   * Checks if a position is valid.
   */
  private boolean isValidPosition(int row, int col, int rows, int cols) {
    return row >= 0 && row < rows && col >= 0 && col < cols;
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
      gameStateService.setPuzzleCompleted(true);
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

  /**
   * Gets the current grid being managed by this controller.
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Gets the game board view.
   */
  public GameBoard getGameBoard() {
    return gameBoard;
  }

  public PuzzleService getPuzzleService() {
    return puzzleService;
  }
}