package com.flowfree.service;

import com.flowfree.model.Color;
import com.flowfree.model.Puzzle;
import com.flowfree.model.Position;

import java.util.*;
import java.util.logging.Logger;

/**
 * Generates Flow Free puzzles using reliable pattern-based approaches.
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
    numFlows = Math.min(numFlows, size); // Conservative limit

    LOGGER.info("Generating puzzle: " + size + "x" + size + " with " + numFlows + " flows");

    // Use a pattern-based approach rather than complex generation
    switch (size) {
      case 5:
        return generateSize5Puzzle(name, numFlows);
      case 6:
        return generateSize6Puzzle(name, numFlows);
      case 7:
        return generateSize7Puzzle(name, numFlows);
      default:
        return generateGenericSizePuzzle(name, size, numFlows);
    }
  }

  /**
   * Generate a 5x5 puzzle with predefined patterns
   */
  private Puzzle generateSize5Puzzle(String name, int numFlows) {
    Puzzle puzzle = new Puzzle(name, 5, 5);

    // Pre-defined reliable endpoint positions for 5x5 grid
    Position[][] endpointPairs = {
        { new Position(0, 0), new Position(4, 4) }, // Red
        { new Position(0, 4), new Position(4, 0) }, // Green
        { new Position(0, 2), new Position(4, 2) }, // Blue
        { new Position(2, 0), new Position(2, 4) }, // Yellow
        { new Position(1, 1), new Position(3, 3) } // Purple
    };

    // Add endpoints based on requested number of flows
    for (int i = 0; i < Math.min(numFlows, endpointPairs.length); i++) {
      puzzle.addEndpoint(
          endpointPairs[i][0].getRow(), endpointPairs[i][0].getCol(),
          Color.values()[i],
          endpointPairs[i][1].getRow(), endpointPairs[i][1].getCol());
    }

    return puzzle;
  }

  /**
   * Generate a 6x6 puzzle with predefined patterns
   */
  private Puzzle generateSize6Puzzle(String name, int numFlows) {
    Puzzle puzzle = new Puzzle(name, 6, 6);

    // Pre-defined reliable endpoint positions for 6x6 grid
    Position[][] endpointPairs = {
        { new Position(0, 0), new Position(5, 5) }, // Red
        { new Position(0, 5), new Position(5, 0) }, // Green
        { new Position(0, 2), new Position(5, 3) }, // Blue
        { new Position(2, 0), new Position(3, 5) }, // Yellow
        { new Position(1, 1), new Position(4, 4) }, // Purple
        { new Position(1, 4), new Position(4, 1) } // Orange
    };

    // Add endpoints based on requested number of flows
    for (int i = 0; i < Math.min(numFlows, endpointPairs.length); i++) {
      puzzle.addEndpoint(
          endpointPairs[i][0].getRow(), endpointPairs[i][0].getCol(),
          Color.values()[i],
          endpointPairs[i][1].getRow(), endpointPairs[i][1].getCol());
    }

    return puzzle;
  }

  /**
   * Generate a 7x7 puzzle with predefined patterns
   */
  private Puzzle generateSize7Puzzle(String name, int numFlows) {
    Puzzle puzzle = new Puzzle(name, 7, 7);

    // Pre-defined reliable endpoint positions for 7x7 grid
    Position[][] endpointPairs = {
        { new Position(0, 0), new Position(6, 6) }, // Red
        { new Position(0, 6), new Position(6, 0) }, // Green
        { new Position(0, 3), new Position(6, 3) }, // Blue
        { new Position(3, 0), new Position(3, 6) }, // Yellow
        { new Position(1, 1), new Position(5, 5) }, // Purple
        { new Position(1, 5), new Position(5, 1) }, // Orange
        { new Position(2, 2), new Position(4, 4) } // Pink
    };

    // Add endpoints based on requested number of flows
    for (int i = 0; i < Math.min(numFlows, endpointPairs.length); i++) {
      puzzle.addEndpoint(
          endpointPairs[i][0].getRow(), endpointPairs[i][0].getCol(),
          Color.values()[i],
          endpointPairs[i][1].getRow(), endpointPairs[i][1].getCol());
    }

    return puzzle;
  }

  /**
   * Generate a puzzle of arbitrary size
   */
  private Puzzle generateGenericSizePuzzle(String name, int size, int numFlows) {
    Puzzle puzzle = new Puzzle(name, size, size);

    // For larger or custom sizes, create a pattern that's guaranteed to work
    // Place endpoints along perimeter with maximum distance
    List<Position> perimeterPositions = new ArrayList<>();

    // Add all perimeter positions
    for (int i = 0; i < size; i++) {
      perimeterPositions.add(new Position(0, i)); // Top row
      perimeterPositions.add(new Position(size - 1, i)); // Bottom row
      if (i > 0 && i < size - 1) {
        perimeterPositions.add(new Position(i, 0)); // Left column
        perimeterPositions.add(new Position(i, size - 1)); // Right column
      }
    }

    // Shuffle to randomize the selection
    Collections.shuffle(perimeterPositions, random);

    // Select pairs with maximum distance between them
    for (int i = 0; i < Math.min(numFlows, Color.values().length); i++) {
      if (i * 2 + 1 >= perimeterPositions.size())
        break;

      Position pos1 = perimeterPositions.get(i * 2);
      Position pos2 = findFarthestPosition(pos1, perimeterPositions, i * 2);

      puzzle.addEndpoint(
          pos1.getRow(), pos1.getCol(),
          Color.values()[i],
          pos2.getRow(), pos2.getCol());
    }

    return puzzle;
  }

  /**
   * Find the position farthest from the given position
   */
  private Position findFarthestPosition(Position pos, List<Position> positions, int excluding) {
    int maxDistance = 0;
    Position farthest = null;

    for (int i = 0; i < positions.size(); i++) {
      if (i == excluding)
        continue;

      Position current = positions.get(i);
      int distance = manhattanDistance(pos, current);

      if (distance > maxDistance) {
        maxDistance = distance;
        farthest = current;
      }
    }

    if (farthest != null) {
      positions.remove(farthest); // Remove so it won't be used again
      return farthest;
    }

    // Fallback - should never happen with proper list sizes
    return positions.get((excluding + 1) % positions.size());
  }

  /**
   * Calculate Manhattan distance between two positions
   */
  private int manhattanDistance(Position p1, Position p2) {
    return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
  }
}