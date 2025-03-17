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
  private final PuzzleGenerator generator = new PuzzleGenerator();

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
    // Generate base puzzles
    Puzzle puzzle1 = generator.generatePuzzle("Easy Level 1", 5, 3);
    puzzles.add(generator.rearrangeFlows(puzzle1, 5));

    Puzzle puzzle2 = generator.generatePuzzle("Easy Level 2", 5, 4);
    puzzles.add(generator.rearrangeFlows(puzzle2, 6));

    Puzzle puzzle3 = generator.generatePuzzle("Medium Level 1", 5, 5);
    puzzles.add(generator.rearrangeFlows(puzzle3, 8));

    Puzzle puzzle4 = generator.generatePuzzle("Medium Level 2", 6, 5);
    puzzles.add(generator.rearrangeFlows(puzzle4, 10));

    Puzzle puzzle5 = generator.generatePuzzle("Hard Level 1", 6, 6);
    puzzles.add(generator.rearrangeFlows(puzzle5, 12));

    Puzzle puzzle6 = generator.generatePuzzle("Hard Level 2", 7, 7);
    puzzles.add(generator.rearrangeFlows(puzzle6, 15));
  }

  /**
   * Generates a new random puzzle with the specified parameters.
   *
   * @param name     Name for the puzzle
   * @param size     Grid size (size x size)
   * @param numFlows Number of colored flows
   * @return The newly generated puzzle
   */
  public Puzzle generateRandomPuzzle(String name, int size, int numFlows) {
    return generator.generatePuzzle(name, size, numFlows);
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

    // Create a new grid with the correct size if needed
    if (grid.getRows() != puzzle.getRows() || grid.getCols() != puzzle.getCols()) {
      // We need to create a new grid with the correct size
      // But we don't have direct access to change the grid here
      // This should be handled by the controller
      return false;
    }

    // Reset the grid
    for (int row = 0; row < grid.getRows(); row++) {
      for (int col = 0; col < grid.getCols(); col++) {
        Cell cell = grid.getCell(row, col);
        if (cell != null) {
          cell.setColor(null);
          cell.setEndpoint(false);
          cell.setPartOfPath(false);
        }
      }
    }

    // Set endpoints only
    puzzle.getEndpoints().forEach(endpoint -> {
      // Add first endpoint
      Cell startCell = grid.getCell(endpoint.startRow, endpoint.startCol);
      if (startCell != null) {
        startCell.setColor(endpoint.color);
        startCell.setEndpoint(true);
        startCell.setPartOfPath(false);
      }

      // Add second endpoint
      Cell endCell = grid.getCell(endpoint.endRow, endpoint.endCol);
      if (endCell != null) {
        endCell.setColor(endpoint.color);
        endCell.setEndpoint(true);
        endCell.setPartOfPath(false);
      }
    });

    return true;
  }

  /**
   * Adds a newly generated puzzle to the puzzle list.
   *
   * @param puzzle The puzzle to add
   * @return The index of the added puzzle
   */
  public int addPuzzle(Puzzle puzzle) {
    puzzles.add(puzzle);
    return puzzles.size() - 1;
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

  /**
   * Gets the total number of puzzles.
   */
  public int getPuzzleCount() {
    return puzzles.size();
  }

  /**
   * Gets a puzzle by index.
   */
  public Puzzle getPuzzle(int index) {
    if (index >= 0 && index < puzzles.size()) {
      return puzzles.get(index);
    }
    return null;
  }
}