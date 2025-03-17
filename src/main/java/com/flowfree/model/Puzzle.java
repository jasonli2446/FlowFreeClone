package com.flowfree.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a puzzle configuration with endpoints.
 */
public class Puzzle {
  private final String name;
  private final int rows;
  private final int cols;
  private final List<Endpoint> endpoints = new ArrayList<>();
  private int[][] solution; // Complete solution grid

  public Puzzle(String name, int rows, int cols) {
    this.name = name;
    this.rows = rows;
    this.cols = cols;
  }

  public String getName() {
    return name;
  }

  public int getRows() {
    return rows;
  }

  public int getCols() {
    return cols;
  }

  /**
   * Adds a pair of endpoints (start and end) for a color.
   */
  public void addEndpoint(int startRow, int startCol, Color color, int endRow, int endCol) {
    endpoints.add(new Endpoint(startRow, startCol, endRow, endCol, color));
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  /**
   * Sets the complete solution grid.
   */
  public void setSolution(int[][] solution) {
    this.solution = solution;
  }

  /**
   * Gets the complete solution grid.
   */
  public int[][] getSolution() {
    return solution;
  }

  /**
   * Gets color paths from solution matrix.
   */
  public Map<Color, List<Position>> getSolutionPaths() {
    Map<Color, List<Position>> paths = new HashMap<>();

    if (solution != null) {
      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          int colorIndex = solution[r][c];
          if (colorIndex >= 0 && colorIndex < Color.values().length) {
            Color color = Color.values()[colorIndex % Color.values().length];
            paths.computeIfAbsent(color, k -> new ArrayList<>())
                .add(new Position(r, c));
          }
        }
      }
    }

    return paths;
  }

  /**
   * Inner class representing a pair of endpoints for a color.
   */
  public static class Endpoint {
    public final int startRow;
    public final int startCol;
    public final int endRow;
    public final int endCol;
    public final Color color;

    public Endpoint(int startRow, int startCol, int endRow, int endCol, Color color) {
      this.startRow = startRow;
      this.startCol = startCol;
      this.endRow = endRow;
      this.endCol = endCol;
      this.color = color;
    }
  }
}