package com.flowfree.config;

/**
 * Contains game-wide configuration constants.
 */
public final class GameConfig {
  // Application settings
  public static final String APPLICATION_TITLE = "Flow Free Clone";
  public static final int WINDOW_WIDTH = 500;
  public static final int WINDOW_HEIGHT = 600;

  // Game settings
  public static final int DEFAULT_GRID_SIZE = 7;
  public static final int CELL_SIZE = 40;
  public static final int CELL_PADDING = 0;

  // Prevent instantiation
  private GameConfig() {
  }
}