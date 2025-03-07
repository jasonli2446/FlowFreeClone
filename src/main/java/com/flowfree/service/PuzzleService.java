package com.flowfree.service;

import com.flowfree.model.Grid;
import com.flowfree.model.Cell;
import com.flowfree.model.Color;
import com.flowfree.model.Puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing puzzle loading and progression.
 */
public class PuzzleService {
  private final List<Puzzle> puzzles = new ArrayList<>();
  private int currentPuzzleIndex = 0;

  public PuzzleService() {
    loadPuzzles();
  }

  /**
   * Loads puzzle definitions.
   * In a production environment, this would load from a file or database.
   */
  private void loadPuzzles() {
    // Add some sample puzzles (5x5 grids)
    createSamplePuzzles();
  }

  private void createSamplePuzzles() {
    // Sample puzzle 1 (5x5)
    Puzzle puzzle1 = new Puzzle("Easy Level 1", 5, 5);
    puzzle1.addEndpoint(0, 0, Color.RED, 4, 4);
    puzzle1.addEndpoint(0, 4, Color.BLUE, 4, 0);
    puzzle1.addEndpoint(2, 0, Color.GREEN, 2, 4);
    puzzles.add(puzzle1);

    // Sample puzzle 2 (5x5)
    Puzzle puzzle2 = new Puzzle("Easy Level 2", 5, 5);
    puzzle2.addEndpoint(0, 0, Color.RED, 4, 4);
    puzzle2.addEndpoint(0, 4, Color.BLUE, 4, 0);
    puzzle2.addEndpoint(1, 2, Color.GREEN, 3, 2);
    puzzle2.addEndpoint(2, 1, Color.YELLOW, 2, 3);
    puzzles.add(puzzle2);

    // Sample puzzle 3 (5x5)
    Puzzle puzzle3 = new Puzzle("Medium Level 1", 5, 5);
    puzzle3.addEndpoint(0, 0, Color.RED, 4, 4);
    puzzle3.addEndpoint(0, 4, Color.BLUE, 4, 0);
    puzzle3.addEndpoint(2, 0, Color.GREEN, 2, 4);
    puzzle3.addEndpoint(1, 2, Color.YELLOW, 3, 2);
    puzzle3.addEndpoint(0, 2, Color.PURPLE, 4, 2);
    puzzles.add(puzzle3);
  }

  /**
   * Gets the current puzzle.
   *
   * @return The current puzzle
   */
  public Puzzle getCurrentPuzzle() {
    if (currentPuzzleIndex < puzzles.size()) {
      return puzzles.get(currentPuzzleIndex);
    }
    return null;
  }

  /**
   * Gets the index of the current puzzle.
   *
   * @return The current puzzle index
   */
  public int getCurrentPuzzleIndex() {
    return currentPuzzleIndex;
  }

  /**
   * Loads a puzzle into the provided grid.
   *
   * @param grid        The grid to load the puzzle into
   * @param puzzleIndex Index of the puzzle to load
   * @return True if puzzle was loaded successfully
   */
  public boolean loadPuzzle(Grid grid, int puzzleIndex) {
    if (puzzleIndex < 0 || puzzleIndex >= puzzles.size()) {
      return false;
    }

    Puzzle puzzle = puzzles.get(puzzleIndex);
    currentPuzzleIndex = puzzleIndex;

    // Reset the grid
    for (int row = 0; row < grid.getRows(); row++) {
      for (int col = 0; col < grid.getCols(); col++) {
        Cell cell = grid.getCell(row, col);
        cell.clear();
        cell.setEndpoint(false);
      }
    }

    // Set endpoints
    puzzle.getEndpoints().forEach(endpoint -> {
      grid.addEndpoint(endpoint.startRow, endpoint.startCol, endpoint.color);
      grid.addEndpoint(endpoint.endRow, endpoint.endCol, endpoint.color);
    });

    return true;
  }

  /**
   * Advances to the next puzzle if available.
   *
   * @param grid The grid to load the next puzzle into
   * @return True if advanced to a new puzzle, false if no more puzzles
   */
  public boolean nextPuzzle(Grid grid) {
    if (currentPuzzleIndex + 1 < puzzles.size()) {
      return loadPuzzle(grid, currentPuzzleIndex + 1);
    }
    return false;
  }

  /**
   * Returns to the previous puzzle if available.
   *
   * @param grid The grid to load the previous puzzle into
   * @return True if returned to a previous puzzle, false if at first puzzle
   */
  public boolean previousPuzzle(Grid grid) {
    if (currentPuzzleIndex > 0) {
      return loadPuzzle(grid, currentPuzzleIndex - 1);
    }
    return false;
  }
}