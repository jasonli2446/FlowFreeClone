package com.flowfree.model;

import java.util.Objects;

/**
 * Represents a cell in the game grid.
 */
public class Cell {
  private final Position position;
  private Color color;
  private boolean isEndpoint;
  private boolean isPartOfPath;

  public Cell(Position position) {
    this.position = position;
    this.color = null;
    this.isEndpoint = false;
    this.isPartOfPath = false;
  }

  public Position getPosition() {
    return position;
  }

  public int getRow() {
    return position.getRow();
  }

  public int getCol() {
    return position.getCol();
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public boolean isEndpoint() {
    return isEndpoint;
  }

  public void setEndpoint(boolean isEndpoint) {
    this.isEndpoint = isEndpoint;
  }

  public boolean isPartOfPath() {
    return isPartOfPath;
  }

  public void setPartOfPath(boolean isPartOfPath) {
    this.isPartOfPath = isPartOfPath;
  }

  public boolean isEmpty() {
    return color == null;
  }

  public void clear() {
    if (!isEndpoint) {
      this.color = null;
      this.isPartOfPath = false;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Cell cell = (Cell) o;
    return Objects.equals(position, cell.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(position);
  }

  @Override
  public String toString() {
    return "Cell" + position + "[" + (color != null ? color : "empty") +
        (isEndpoint ? ",endpoint" : "") +
        (isPartOfPath ? ",path" : "") + "]";
  }
}