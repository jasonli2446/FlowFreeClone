package com.flowfree.model;

import java.util.Objects;

/**
 * Immutable class representing a position on the grid.
 */
public final class Position {
  private final int row;
  private final int col;

  public Position(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public Position move(int rowOffset, int colOffset) {
    return new Position(row + rowOffset, col + colOffset);
  }

  public boolean isAdjacent(Position other) {
    int rowDiff = Math.abs(this.row - other.row);
    int colDiff = Math.abs(this.col - other.col);
    return (rowDiff + colDiff == 1);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Position position = (Position) o;
    return row == position.row && col == position.col;
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, col);
  }

  @Override
  public String toString() {
    return "(" + row + "," + col + ")";
  }
}