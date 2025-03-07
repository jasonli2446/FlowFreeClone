package com.flowfree.model;

public class Cell {
  private int x;
  private int y;
  private String color;
  private boolean isEndpoint;
  private boolean isPartOfPath;

  public Cell(int x, int y, String color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.isEndpoint = false;
    this.isPartOfPath = false;
  }

  // Getters and setters
  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
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

  @Override
  public String toString() {
    return "Cell[" + x + "," + y + "," + (color != null ? color : "empty") + "]";
  }
}