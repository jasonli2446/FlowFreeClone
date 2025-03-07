package com.flowfree.model;

import javafx.scene.paint.Paint;

/**
 * Enum representing available colors for flow paths.
 */
public enum Color {
  RED("Red", javafx.scene.paint.Color.RED),
  GREEN("Green", javafx.scene.paint.Color.GREEN),
  BLUE("Blue", javafx.scene.paint.Color.BLUE),
  YELLOW("Yellow", javafx.scene.paint.Color.YELLOW),
  PURPLE("Purple", javafx.scene.paint.Color.PURPLE),
  ORANGE("Orange", javafx.scene.paint.Color.ORANGE),
  PINK("Pink", javafx.scene.paint.Color.PINK),
  CYAN("Cyan", javafx.scene.paint.Color.CYAN);

  private final String name;
  private final Paint paint;

  Color(String name, Paint paint) {
    this.name = name;
    this.paint = paint;
  }

  public String getName() {
    return name;
  }

  public Paint getPaint() {
    return paint;
  }

  @Override
  public String toString() {
    return name;
  }

  public static Color fromString(String colorName) {
    for (Color color : values()) {
      if (color.name.equalsIgnoreCase(colorName)) {
        return color;
      }
    }
    throw new IllegalArgumentException("No color found for name: " + colorName);
  }
}