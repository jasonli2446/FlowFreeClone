package com.flowfree.controller;

import com.flowfree.model.Grid;
import com.flowfree.service.PuzzleService;
import com.flowfree.view.GameBoard;
import com.flowfree.service.GameStateService;

/**
 * Controller for puzzle progression and management.
 */
public class PuzzleController {
  private final Grid grid;
  private final GameBoard gameBoard;
  private final PuzzleService puzzleService;
  private final GameStateService gameStateService;

  public PuzzleController(Grid grid, GameBoard gameBoard) {
    this.grid = grid;
    this.gameBoard = gameBoard;
    this.puzzleService = new PuzzleService();
    this.gameStateService = new GameStateService(grid);

    loadInitialPuzzle();
  }

  /**
   * Loads the initial puzzle.
   */
  public void loadInitialPuzzle() {
    puzzleService.loadPuzzle(grid, 0);
    gameStateService.resetStats();
    gameBoard.updateView();
  }

  /**
   * Advances to the next puzzle if available.
   */
  public boolean nextPuzzle() {
    if (puzzleService.nextPuzzle(grid)) {
      gameStateService.resetStats();
      gameBoard.updateView();
      return true;
    }
    return false;
  }

  /**
   * Goes back to the previous puzzle if available.
   */
  public boolean previousPuzzle() {
    if (puzzleService.previousPuzzle(grid)) {
      gameStateService.resetStats();
      gameBoard.updateView();
      return true;
    }
    return false;
  }

  /**
   * Resets the current puzzle.
   */
  public void resetPuzzle() {
    int currentIndex = puzzleService.getCurrentPuzzleIndex();
    puzzleService.loadPuzzle(grid, currentIndex);
    gameStateService.resetStats();
    gameBoard.updateView();
  }

  /**
   * Checks if the current puzzle is solved.
   */
  public boolean isPuzzleSolved() {
    boolean complete = gameStateService.isPuzzleComplete();
    if (complete && !gameStateService.isPuzzleCompleted()) {
      gameStateService.setPuzzleCompleted(true);
    }
    return complete;
  }

  /**
   * Gets the current puzzle name.
   */
  public String getCurrentPuzzleName() {
    if (puzzleService.getCurrentPuzzle() != null) {
      return puzzleService.getCurrentPuzzle().getName();
    }
    return "Unknown Puzzle";
  }

  /**
   * Gets the elapsed time in seconds.
   */
  public int getElapsedTime() {
    return gameStateService.getElapsedTimeSeconds();
  }

  /**
   * Gets the current move count.
   */
  public int getMoveCount() {
    return gameStateService.getMoveCount();
  }

  /**
   * Gets the current score.
   */
  public int getScore() {
    return gameStateService.calculateScore();
  }
}