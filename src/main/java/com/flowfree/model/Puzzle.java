package com.flowfree.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a puzzle configuration with endpoints.
 */
public class Puzzle {
  private final String name;
  private final int rows;
  private final int cols;
  private final List<Endpoint> endpoints = new ArrayList<>();

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