package com.flowfree.model;

import java.util.*;

/**
 * Represents the game grid containing cells.
 */
public class Grid {
  private final int rows;
  private final int cols;
  private final Cell[][] cells;
  private final Map<Color, List<Cell>> endpointsByColor;

  // Direction vectors for adjacent cell calculations
  private static final int[][] DIRECTIONS = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

  public Grid(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.cells = new Cell[rows][cols];
    this.endpointsByColor = new EnumMap<>(Color.class);

    initializeCells();
  }

  private void initializeCells() {
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        cells[row][col] = new Cell(new Position(row, col));
      }
    }
  }

  public int getRows() {
    return rows;
  }

  public int getCols() {
    return cols;
  }

  public Cell getCell(int row, int col) {
    if (isValidPosition(row, col)) {
      return cells[row][col];
    }
    return null;
  }

  public Cell getCell(Position position) {
    return getCell(position.getRow(), position.getCol());
  }

  public boolean isValidPosition(int row, int col) {
    return row >= 0 && row < rows && col >= 0 && col < cols;
  }

  public boolean isValidPosition(Position position) {
    return isValidPosition(position.getRow(), position.getCol());
  }

  /**
   * Add an endpoint for a path
   */
  public void addEndpoint(int row, int col, Color color) {
    Cell cell = getCell(row, col);
    if (cell != null) {
      cell.setColor(color);
      cell.setEndpoint(true);
      cell.setPartOfPath(false);

      endpointsByColor.computeIfAbsent(color, k -> new ArrayList<>()).add(cell);
    }
  }

  public List<Cell> getEndpoints(Color color) {
    return endpointsByColor.getOrDefault(color, Collections.emptyList());
  }

  public List<Cell> getAdjacentCells(Cell cell) {
    return getAdjacentCells(cell.getRow(), cell.getCol());
  }

  public List<Cell> getAdjacentCells(int row, int col) {
    List<Cell> adjacentCells = new ArrayList<>(4);

    for (int[] dir : DIRECTIONS) {
      int newRow = row + dir[0];
      int newCol = col + dir[1];

      Cell adjacent = getCell(newRow, newCol);
      if (adjacent != null) {
        adjacentCells.add(adjacent);
      }
    }

    return adjacentCells;
  }

  public void clearPath(Color color) {
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Cell cell = cells[row][col];
        if (!cell.isEndpoint() &&
            cell.getColor() != null &&
            cell.getColor().equals(color)) {
          cell.clear();
        }
      }
    }
  }

  /**
   * Completely clears all cells in the grid, including endpoints
   */
  public void clearAll() {
    endpointsByColor.clear();

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Cell cell = cells[row][col];
        cell.setColor(null);
        cell.setEndpoint(false);
        cell.setPartOfPath(false);
      }
    }
  }

  public boolean isComplete() {
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        if (cells[row][col].isEmpty()) {
          return false;
        }
      }
    }
    return true;
  }

  public Cell findLastCellInPath(Color color) {
    List<Cell> pathCells = new ArrayList<>();

    // Get all cells in the path (non-endpoints)
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Cell cell = cells[row][col];
        if (cell.isPartOfPath() &&
            !cell.isEndpoint() &&
            cell.getColor() == color) {
          pathCells.add(cell);
        }
      }
    }

    if (pathCells.isEmpty()) {
      return null;
    }

    // Find cells with only one adjacent cell of the same color (path end)
    for (Cell cell : pathCells) {
      int adjacentSameColorCount = 0;
      for (Cell adjacent : getAdjacentCells(cell)) {
        if (adjacent.getColor() == color) {
          adjacentSameColorCount++;
        }
      }

      if (adjacentSameColorCount == 1) {
        return cell;
      }
    }

    return null;
  }
}