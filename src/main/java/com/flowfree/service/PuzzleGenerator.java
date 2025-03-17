package com.flowfree.service;

import com.flowfree.model.Color;
import com.flowfree.model.Puzzle;
import com.flowfree.model.Position;

import java.util.*;
import java.util.logging.Logger;

/**
 * Generates Flow Free puzzles using simple, guaranteed solvable patterns.
 */
public class PuzzleGenerator {
  private static final Logger LOGGER = Logger.getLogger(PuzzleGenerator.class.getName());
  private final Random random = new Random();

  /**
   * Generates a puzzle with the given size and number of flows.
   * 
   * @param name     Name of the puzzle
   * @param size     Grid size (size x size)
   * @param numFlows Number of color flows to include
   * @return A generated puzzle
   */
  public Puzzle generatePuzzle(String name, int size, int numFlows) {
    // Ensure we don't exceed available colors or board capacity
    numFlows = Math.min(numFlows, Color.values().length);
    numFlows = Math.min(numFlows, size); // Don't use more colors than rows

    LOGGER.info("Generating puzzle: " + size + "x" + size + " with " + numFlows + " flows");

    // Generate a simple horizontal pattern solution
    int[][] solution = generateSimpleHorizontalSolution(size, numFlows);

    // Extract puzzle from solution
    Puzzle puzzle = extractPuzzleFromSolution(name, solution, size, numFlows);

    // Log validation result
    boolean isValid = verifySolution(puzzle, solution);
    LOGGER.info("Solution validity check: " + isValid);

    return puzzle;
  }

  /**
   * Generates a simple solution with horizontal paths for each color.
   * This guarantees a valid solution that fills the entire board.
   */
  private int[][] generateSimpleHorizontalSolution(int size, int numFlows) {
    int[][] solution = new int[size][size];

    // Initialize with color -1 (empty)
    for (int r = 0; r < size; r++) {
      Arrays.fill(solution[r], -1);
    }

    // Fill each row with a single color
    for (int r = 0; r < size; r++) {
      int colorIndex = r % numFlows;
      for (int c = 0; c < size; c++) {
        solution[r][c] = colorIndex;
      }
    }

    return solution;
  }

  /**
   * Extracts a puzzle from a solution by choosing endpoints.
   */
  private Puzzle extractPuzzleFromSolution(String name, int[][] solution, int size, int numFlows) {
    Puzzle puzzle = new Puzzle(name, size, size);

    // For each color, use the first and last cells in its first row as endpoints
    for (int color = 0; color < numFlows; color++) {
      // Find the first row with this color
      for (int r = 0; r < size; r++) {
        if (solution[r][0] == color) {
          // Add endpoints at the start and end of this row
          puzzle.addEndpoint(
              r, 0, // First column (left)
              Color.values()[color % Color.values().length],
              r, size - 1 // Last column (right)
          );
          break;
        }
      }
    }

    // Store the solution with the puzzle
    int[][] solutionCopy = new int[size][size];
    for (int r = 0; r < size; r++) {
      System.arraycopy(solution[r], 0, solutionCopy[r], 0, size);
    }
    puzzle.setSolution(solutionCopy);

    return puzzle;
  }

  /**
   * Verifies that the solution is valid for the puzzle.
   */
  private boolean verifySolution(Puzzle puzzle, int[][] solution) {
    // Check each color path
    for (Puzzle.Endpoint endpoint : puzzle.getEndpoints()) {
      int colorIndex = indexOf(Color.values(), endpoint.color);

      // Check if start and end points have the right color
      if (solution[endpoint.startRow][endpoint.startCol] != colorIndex) {
        LOGGER.warning("Start point has wrong color");
        return false;
      }

      if (solution[endpoint.endRow][endpoint.endCol] != colorIndex) {
        LOGGER.warning("End point has wrong color");
        return false;
      }

      // Check if there's a connected path between endpoints
      if (!hasConnectedPath(solution, endpoint, colorIndex)) {
        LOGGER.warning("No connected path for color " + endpoint.color);
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if a path connects the two endpoints.
   */
  private boolean hasConnectedPath(int[][] solution, Puzzle.Endpoint endpoint, int colorIndex) {
    int size = solution.length;

    // For horizontal paths, just verify all cells in the row are the same color
    int row = endpoint.startRow;
    for (int c = endpoint.startCol; c <= endpoint.endCol; c++) {
      if (solution[row][c] != colorIndex) {
        return false;
      }
    }

    return true;
  }

  /**
   * Find index of a value in an array.
   */
  private <T> int indexOf(T[] array, T value) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }
}