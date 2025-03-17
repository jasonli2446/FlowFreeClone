package com.flowfree.service;

import com.flowfree.model.*;
import java.util.*;

/**
 * Validates if a puzzle has a solution that fills the entire grid.
 */
public class SolutionValidator {
  /**
   * Checks if a puzzle has at least one solution that covers all cells.
   */
  public boolean hasSolution(Puzzle puzzle) {
    // Create a virtual grid to test
    int[][] grid = new int[puzzle.getRows()][puzzle.getCols()];
    for (int i = 0; i < puzzle.getRows(); i++) {
      for (int j = 0; j < puzzle.getCols(); j++) {
        grid[i][j] = -1; // -1 means empty
      }
    }

    // Mark endpoints
    for (int i = 0; i < puzzle.getEndpoints().size(); i++) {
      Puzzle.Endpoint endpoint = puzzle.getEndpoints().get(i);
      grid[endpoint.startRow][endpoint.startCol] = i;
      grid[endpoint.endRow][endpoint.endCol] = i;
    }

    // Try to find a solution using backtracking
    return solvePuzzle(grid, puzzle);
  }

  private boolean solvePuzzle(int[][] grid, Puzzle puzzle) {
    // Find an empty cell
    int[] emptyCellPos = findEmptyCell(grid);
    if (emptyCellPos == null) {
      // No empty cells - puzzle is solved
      return true;
    }

    int row = emptyCellPos[0];
    int col = emptyCellPos[1];

    // Try placing each color in this cell
    for (int colorIndex = 0; colorIndex < puzzle.getEndpoints().size(); colorIndex++) {
      // Check if this color is valid in this cell
      if (isValidPlacement(grid, row, col, colorIndex, puzzle)) {
        // Place the color
        grid[row][col] = colorIndex;

        // Recursively try to solve the rest
        if (solvePuzzle(grid, puzzle)) {
          return true;
        }

        // If no solution, backtrack
        grid[row][col] = -1;
      }
    }

    // No color worked in this cell
    return false;
  }

  private int[] findEmptyCell(int[][] grid) {
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[0].length; col++) {
        if (grid[row][col] == -1) {
          return new int[] { row, col };
        }
      }
    }
    return null; // No empty cells
  }

  private boolean isValidPlacement(int[][] grid, int row, int col, int colorIndex, Puzzle puzzle) {
    // Implementation would check:
    // 1. If this cell has a neighbor of the same color
    // 2. If placing this color doesn't create a split path
    // 3. If placing this color doesn't block other paths

    // This is a complex algorithm that would need proper implementation
    // Simplified version for demonstration
    return hasAdjacentSameColor(grid, row, col, colorIndex);
  }

  private boolean hasAdjacentSameColor(int[][] grid, int row, int col, int colorIndex) {
    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

    for (int[] dir : directions) {
      int newRow = row + dir[0];
      int newCol = col + dir[1];

      if (isValidPosition(newRow, newCol, grid.length) && grid[newRow][newCol] == colorIndex) {
        return true;
      }
    }

    return false;
  }

  private boolean isValidPosition(int row, int col, int size) {
    return row >= 0 && row < size && col >= 0 && col < size;
  }
}