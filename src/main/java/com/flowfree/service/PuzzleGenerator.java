package com.flowfree.service;

import com.flowfree.model.Color;
import com.flowfree.model.Puzzle;
import com.flowfree.model.Position;

import java.util.*;
import java.util.logging.Logger;

/**
 * Generates Flow Free puzzles using a solution-first approach.
 */
public class PuzzleGenerator {
  private static final Logger LOGGER = Logger.getLogger(PuzzleGenerator.class.getName());
  private final Random random = new Random();

  // Directions for navigation: Up, Right, Down, Left
  private static final int[][] DIRECTIONS = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };
  private static final int REARRANGEMENT_ITERATIONS = 50; // Number of iterations for rearrangement

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
    numFlows = Math.min(numFlows, size); // Conservative limit

    LOGGER.info("Generating puzzle: " + size + "x" + size + " with " + numFlows + " flows");

    // Create a complete solution first
    int[][] solution = createSimpleSolution(size, numFlows);

    // Rearrange the flows to make the puzzle more complex
    solution = rearrangeFlows(solution, size, numFlows);

    // Extract endpoints to create the puzzle
    return extractPuzzleFromSolution(name, solution, size, numFlows);
  }

  /**
   * Creates a simple complete solution with straight-line flows.
   */
  private int[][] createSimpleSolution(int size, int numFlows) {
    int[][] solution = new int[size][size];

    // Initialize with -1 (empty)
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        solution[r][c] = -1;
      }
    }

    // Create straight line flows
    for (int color = 0; color < numFlows; color++) {
      // Decide to make flow vertical or horizontal
      boolean vertical = color % 2 == 0;

      if (vertical) {
        // Vertical flow
        int col = color / 2;
        for (int r = 0; r < size; r++) {
          if (col < size) {
            solution[r][col] = color;
          }
        }
      } else {
        // Horizontal flow
        int row = color / 2;
        for (int c = 0; c < size; c++) {
          if (row < size) {
            solution[row][c] = color;
          }
        }
      }
    }

    // Fill any remaining empty cells with first color
    // This ensures the solution covers the entire grid
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] == -1) {
          solution[r][c] = 0;
        }
      }
    }

    return solution;
  }

  /**
   * Rearranges flows to make the puzzle more complex.
   */
  private int[][] rearrangeFlows(int[][] solution, int size, int numFlows) {
    // For iterations, try to rearrange flows
    for (int iteration = 0; iteration < REARRANGEMENT_ITERATIONS; iteration++) {
      // Choose a random flow
      int flowColor = random.nextInt(numFlows);

      // Get all cells of this flow
      List<Position> flowCells = getFlowCells(solution, size, flowColor);
      if (flowCells.size() <= 3) {
        continue; // Skip if flow is too short
      }

      // Find neighbor flows that we can interact with
      List<NeighborInfo> neighbors = findNeighboringFlows(solution, size, flowCells, flowColor);
      if (neighbors.isEmpty()) {
        continue;
      }

      // Pick a random neighbor
      NeighborInfo neighbor = neighbors.get(random.nextInt(neighbors.size()));

      // Attempt to swap territory
      swapTerritory(solution, size, flowColor, neighbor);
    }

    return solution;
  }

  /**
   * Gets all cells belonging to a specific flow.
   */
  private List<Position> getFlowCells(int[][] solution, int size, int flowColor) {
    List<Position> flowCells = new ArrayList<>();

    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] == flowColor) {
          flowCells.add(new Position(r, c));
        }
      }
    }

    return flowCells;
  }

  /**
   * Finds neighboring flows that we can interact with.
   */
  private List<NeighborInfo> findNeighboringFlows(int[][] solution, int size,
      List<Position> flowCells, int flowColor) {
    List<NeighborInfo> neighbors = new ArrayList<>();

    for (Position cell : flowCells) {
      // Check all directions
      for (int[] dir : DIRECTIONS) {
        int newRow = cell.getRow() + dir[0];
        int newCol = cell.getCol() + dir[1];

        if (isValidPosition(newRow, newCol, size)) {
          int neighborColor = solution[newRow][newCol];

          if (neighborColor != flowColor && neighborColor != -1) {
            NeighborInfo info = new NeighborInfo();
            info.flowCell = cell;
            info.neighborCell = new Position(newRow, newCol);
            info.neighborColor = neighborColor;
            neighbors.add(info);
          }
        }
      }
    }

    return neighbors;
  }

  /**
   * Swaps territory between two flows.
   */
  private void swapTerritory(int[][] solution, int size, int flowColor, NeighborInfo neighbor) {
    int r1 = neighbor.flowCell.getRow();
    int c1 = neighbor.flowCell.getCol();
    int r2 = neighbor.neighborCell.getRow();
    int c2 = neighbor.neighborCell.getCol();

    // Swap colors
    solution[r1][c1] = neighbor.neighborColor;
    solution[r2][c2] = flowColor;

    // Find a nearby cell of the neighbor's color to connect to
    boolean found = false;
    for (int[] dir : DIRECTIONS) {
      int r3 = r1 + dir[0];
      int c3 = c1 + dir[1];

      if (isValidPosition(r3, c3, size) &&
          solution[r3][c3] == neighbor.neighborColor &&
          !(r3 == r2 && c3 == c2)) {
        found = true;
        break;
      }
    }

    // If we couldn't connect, revert the swap
    if (!found) {
      solution[r1][c1] = flowColor;
      solution[r2][c2] = neighbor.neighborColor;
    }

    // Ensure the solution is still valid (all cells are filled)
    ensureSolutionValid(solution, size);
  }

  /**
   * Check if solution has any empty cells, and fill them if needed.
   */
  private void ensureSolutionValid(int[][] solution, int size) {
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] == -1) {
          // Find a neighboring color to use
          for (int[] dir : DIRECTIONS) {
            int newRow = r + dir[0];
            int newCol = c + dir[1];

            if (isValidPosition(newRow, newCol, size) && solution[newRow][newCol] != -1) {
              solution[r][c] = solution[newRow][newCol];
              break;
            }
          }

          // If still empty, use color 0
          if (solution[r][c] == -1) {
            solution[r][c] = 0;
          }
        }
      }
    }
  }

  /**
   * Extracts puzzle endpoints from a complete solution.
   */
  private Puzzle extractPuzzleFromSolution(String name, int[][] solution, int size, int numFlows) {
    Puzzle puzzle = new Puzzle(name, size, size);

    // For each color, find two good endpoints
    for (int color = 0; color < numFlows; color++) {
      List<Position> flowCells = getFlowCells(solution, size, color);

      if (flowCells.size() < 2) {
        LOGGER.warning("Flow " + color + " has fewer than 2 cells!");
        continue;
      }

      // Find endpoints with maximum distance
      Position[] endpoints = findBestEndpoints(flowCells);

      puzzle.addEndpoint(
          endpoints[0].getRow(), endpoints[0].getCol(),
          Color.values()[color % Color.values().length],
          endpoints[1].getRow(), endpoints[1].getCol());
    }

    // Store the complete solution with the puzzle
    int[][] solutionCopy = new int[size][size];
    for (int r = 0; r < size; r++) {
      System.arraycopy(solution[r], 0, solutionCopy[r], 0, size);
    }
    puzzle.setSolution(solutionCopy);

    return puzzle;
  }

  /**
   * Finds the best pair of endpoints for a flow (maximizing distance).
   */
  private Position[] findBestEndpoints(List<Position> flowCells) {
    Position[] result = new Position[2];
    int maxDistance = 0;

    for (int i = 0; i < flowCells.size(); i++) {
      for (int j = i + 1; j < flowCells.size(); j++) {
        Position p1 = flowCells.get(i);
        Position p2 = flowCells.get(j);

        int distance = manhattanDistance(p1, p2);
        if (distance > maxDistance) {
          maxDistance = distance;
          result[0] = p1;
          result[1] = p2;
        }
      }
    }

    return result;
  }

  /**
   * Calculate Manhattan distance between two positions
   */
  private int manhattanDistance(Position p1, Position p2) {
    return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
  }

  /**
   * Check if a position is valid
   */
  private boolean isValidPosition(int row, int col, int size) {
    return row >= 0 && row < size && col >= 0 && col < size;
  }

  /**
   * Helper class to store neighbor flow information.
   */
  private static class NeighborInfo {
    Position flowCell;
    Position neighborCell;
    int neighborColor;
  }
}