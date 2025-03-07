package com.flowfree.model;

import java.util.*;

public class Grid {
  private Cell[][] cells;
  private Map<String, List<Cell>> flowEndpoints;

  public Grid(int rows, int cols) {
    cells = new Cell[rows][cols];
    flowEndpoints = new HashMap<>();

    // Initialize cells
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        cells[i][j] = new Cell(i, j, null);
      }
    }
  }

  // Getters and setters
  public Cell getCell(int x, int y) {
    if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length) {
      return null;
    }
    return cells[x][y];
  }

  public void setCell(int x, int y, Cell cell) {
    cells[x][y] = cell;
  }

  public Cell[][] getCells() {
    return cells;
  }

  public int getRows() {
    return cells.length;
  }

  public int getCols() {
    return cells[0].length;
  }

  // Add an endpoint for a flow
  public void addEndpoint(int x, int y, String color) {
    Cell cell = getCell(x, y);
    if (cell != null) {
      cell.setColor(color);
      cell.setEndpoint(true);

      if (!flowEndpoints.containsKey(color)) {
        flowEndpoints.put(color, new ArrayList<>());
      }
      flowEndpoints.get(color).add(cell);
    }
  }

  // Check if a move to the given cell is valid for the current color
  public boolean isValidMove(int x, int y, String color) {
    Cell cell = getCell(x, y);

    // Cell must exist and be either empty or an endpoint of the same color
    if (cell == null) {
      return false;
    }

    if (cell.isEmpty() || (cell.isEndpoint() && color.equals(cell.getColor()))) {
      return true;
    }

    return false;
  }

  // Check if all cells are filled (puzzle complete)
  public boolean isComplete() {
    for (int i = 0; i < getRows(); i++) {
      for (int j = 0; j < getCols(); j++) {
        if (cells[i][j].getColor() == null) {
          return false;
        }
      }
    }
    return true;
  }

  // Get adjacent cells to the given coordinates
  public List<Cell> getAdjacentCells(int x, int y) {
    List<Cell> adjacentCells = new ArrayList<>();

    // Check four directions: up, right, down, left
    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

    for (int[] dir : directions) {
      Cell adjacent = getCell(x + dir[0], y + dir[1]);
      if (adjacent != null) {
        adjacentCells.add(adjacent);
      }
    }

    return adjacentCells;
  }

  // Clear a specific path (all non-endpoint cells of a color)
  public void clearPath(String color) {
    for (int i = 0; i < getRows(); i++) {
      for (int j = 0; j < getCols(); j++) {
        Cell cell = cells[i][j];
        if (!cell.isEndpoint() && color.equals(cell.getColor())) {
          cell.setColor(null);
          cell.setPartOfPath(false);
        }
      }
    }
  }
}