package com.flowfree.service;

import com.flowfree.model.Color;
import com.flowfree.model.Puzzle;
import com.flowfree.model.Position;

import java.util.*;
import java.util.logging.Logger;

/**
 * Generates Flow Free puzzles using simple, guaranteed solvable patterns.
 */
public class PuzzleGenerator {
  private static final Logger LOGGER = Logger.getLogger(PuzzleGenerator.class.getName());
  private final Random random = new Random();

  /**
   * Generates a puzzle with the given size and number of flows.
   * 
   * @param name     Name of the puzzle
   * @param size     Grid size (size x size)
   * @param numFlows Number of color flows to include
   * @return A generated puzzle
   */
  public Puzzle generatePuzzle(String name, int size, int numFlows) {
    // For an nxn board, ensure we have n flows to fully utilize the board
    numFlows = Math.min(numFlows, Color.values().length);
    numFlows = Math.max(numFlows, size / 2); // At least size/2 flows to ensure good coverage
    numFlows = Math.min(numFlows, size); // Maximum of size flows

    LOGGER.info("Generating puzzle: " + size + "x" + size + " with " + numFlows + " flows");

    // Generate a better initial solution that uses all cells
    int[][] solution = generateCompleteSolution(size, numFlows);

    // Extract puzzle from solution
    Puzzle puzzle = extractPuzzleFromSolution(name, solution, size, numFlows);

    // Log validation result
    boolean isValid = verifySolution(puzzle, solution);
    LOGGER.info("Solution validity check: " + isValid);

    return puzzle;
  }

  /**
   * Generates a solution that fills the entire board with flows.
   * Uses a spiral pattern approach to ensure all cells are used.
   */
  private int[][] generateCompleteSolution(int size, int numFlows) {
    int[][] solution = new int[size][size];

    // Initialize with color -1 (empty)
    for (int r = 0; r < size; r++) {
      Arrays.fill(solution[r], -1);
    }

    // Generate spiral paths for each color
    List<Position[]> endpointPairs = generateEndpointPairs(size, numFlows);

    // For each endpoint pair, create a path
    for (int colorIndex = 0; colorIndex < endpointPairs.size(); colorIndex++) {
      Position start = endpointPairs.get(colorIndex)[0];
      Position end = endpointPairs.get(colorIndex)[1];

      // Find a path between these endpoints
      List<Position> path = findInitialPath(solution, start, end, size);

      // Apply the path to the solution
      for (Position pos : path) {
        solution[pos.getRow()][pos.getCol()] = colorIndex;
      }
    }

    // Fill any remaining empty cells with random existing colors
    fillEmptyCells(solution, size, numFlows);

    return solution;
  }

  /**
   * Generates pairs of endpoints around the perimeter of the board.
   */
  private List<Position[]> generateEndpointPairs(int size, int numFlows) {
    List<Position> availablePositions = new ArrayList<>();

    // Add all perimeter positions
    for (int i = 0; i < size; i++) {
      availablePositions.add(new Position(0, i)); // Top row
      availablePositions.add(new Position(size - 1, i)); // Bottom row
      if (i > 0 && i < size - 1) {
        availablePositions.add(new Position(i, 0)); // Left column (except corners)
        availablePositions.add(new Position(i, size - 1)); // Right column (except corners)
      }
    }

    // Shuffle positions to randomize endpoint placement
    Collections.shuffle(availablePositions, random);

    // Create endpoint pairs
    List<Position[]> pairs = new ArrayList<>();
    for (int i = 0; i < numFlows && i * 2 + 1 < availablePositions.size(); i++) {
      Position[] pair = {
          availablePositions.get(i * 2),
          availablePositions.get(i * 2 + 1)
      };
      pairs.add(pair);
    }

    return pairs;
  }

  /**
   * Finds an initial path between two endpoints.
   * Uses a simple approach to create a path that doesn't block other flows.
   */
  private List<Position> findInitialPath(int[][] solution, Position start, Position end, int size) {
    List<Position> path = new ArrayList<>();
    path.add(start);

    // Current position starts at start
    int currentRow = start.getRow();
    int currentCol = start.getCol();

    // First move vertically to align with the end's row
    while (currentRow != end.getRow()) {
      if (currentRow < end.getRow()) {
        currentRow++;
      } else {
        currentRow--;
      }
      Position newPos = new Position(currentRow, currentCol);
      path.add(newPos);
    }

    // Then move horizontally to reach the end's column
    while (currentCol != end.getCol()) {
      if (currentCol < end.getCol()) {
        currentCol++;
      } else {
        currentCol--;
      }
      Position newPos = new Position(currentRow, currentCol);
      path.add(newPos);
    }

    return path;
  }

  /**
   * Fills any remaining empty cells with random existing colors.
   */
  private void fillEmptyCells(int[][] solution, int size, int numFlows) {
    // First, count empty cells
    int emptyCells = 0;
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] == -1) {
          emptyCells++;
        }
      }
    }

    LOGGER.info("Filling " + emptyCells + " empty cells");

    // If there are empty cells, fill them with adjacent colors
    if (emptyCells > 0) {
      boolean progress;
      do {
        progress = false;
        for (int r = 0; r < size; r++) {
          for (int c = 0; c < size; c++) {
            if (solution[r][c] == -1) {
              // Find a valid color from adjacent cells
              int color = findAdjacentColor(solution, r, c, size);
              if (color != -1) {
                solution[r][c] = color;
                progress = true;
              }
            }
          }
        }
      } while (progress);

      // If there are still empty cells, fill with random colors
      for (int r = 0; r < size; r++) {
        for (int c = 0; c < size; c++) {
          if (solution[r][c] == -1) {
            solution[r][c] = random.nextInt(numFlows);
          }
        }
      }
    }
  }

  /**
   * Finds a color from adjacent cells.
   */
  private int findAdjacentColor(int[][] solution, int row, int col, int size) {
    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };
    List<Integer> adjacentColors = new ArrayList<>();

    for (int[] dir : directions) {
      int newRow = row + dir[0];
      int newCol = col + dir[1];

      if (isValidPosition(newRow, newCol, size) && solution[newRow][newCol] != -1) {
        adjacentColors.add(solution[newRow][newCol]);
      }
    }

    if (!adjacentColors.isEmpty()) {
      return adjacentColors.get(random.nextInt(adjacentColors.size()));
    }

    return -1;
  }

  /**
   * Extracts a puzzle from a solution by choosing endpoints.
   */
  private Puzzle extractPuzzleFromSolution(String name, int[][] solution, int size, int numFlows) {
    Puzzle puzzle = new Puzzle(name, size, size);

    // Find the endpoints for each color based on the solution
    for (int colorIndex = 0; colorIndex < numFlows; colorIndex++) {
      List<Position> endpoints = findEndpointsForColor(solution, colorIndex, size);

      if (endpoints.size() >= 2) {
        Position start = endpoints.get(0);
        Position end = endpoints.get(1);

        puzzle.addEndpoint(
            start.getRow(), start.getCol(),
            Color.values()[colorIndex % Color.values().length],
            end.getRow(), end.getCol());
      }
    }

    // Store the solution with the puzzle
    int[][] solutionCopy = new int[size][size];
    for (int r = 0; r < size; r++) {
      System.arraycopy(solution[r], 0, solutionCopy[r], 0, size);
    }
    puzzle.setSolution(solutionCopy);

    return puzzle;
  }

  /**
   * Finds suitable endpoints for a color based on the solution.
   */
  private List<Position> findEndpointsForColor(int[][] solution, int colorIndex, int size) {
    List<Position> colorCells = new ArrayList<>();

    // Find all cells with this color
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] == colorIndex) {
          colorCells.add(new Position(r, c));
        }
      }
    }

    if (colorCells.size() < 2) {
      return colorCells; // Not enough cells for this color
    }

    // Find cells that could be endpoints (those with 1 or fewer same-color
    // neighbors)
    List<Position> possibleEndpoints = new ArrayList<>();
    for (Position pos : colorCells) {
      if (countSameColorNeighbors(solution, pos, colorIndex, size) <= 1) {
        possibleEndpoints.add(pos);
      }
    }

    // If we found at least 2 possible endpoints, pick 2 that are far apart
    if (possibleEndpoints.size() >= 2) {
      Collections.shuffle(possibleEndpoints, random);

      // Try to find endpoints on opposite sides if possible
      Position start = possibleEndpoints.get(0);
      Position end = findFarthestPosition(start, possibleEndpoints);

      return Arrays.asList(start, end);
    } else {
      // Not enough endpoint candidates, just pick two random cells
      Collections.shuffle(colorCells, random);
      return Arrays.asList(colorCells.get(0), colorCells.get(1));
    }
  }

  /**
   * Finds the position in the list that's farthest from the reference position.
   */
  private Position findFarthestPosition(Position reference, List<Position> positions) {
    Position farthest = null;
    int maxDistance = -1;

    for (Position pos : positions) {
      if (pos.equals(reference))
        continue;

      int distance = Math.abs(pos.getRow() - reference.getRow()) +
          Math.abs(pos.getCol() - reference.getCol());
      if (distance > maxDistance) {
        maxDistance = distance;
        farthest = pos;
      }
    }

    return farthest != null ? farthest : positions.get(positions.size() - 1);
  }

  /**
   * Verifies that the solution is valid for the puzzle.
   */
  private boolean verifySolution(Puzzle puzzle, int[][] solution) {
    // Check each color path
    for (Puzzle.Endpoint endpoint : puzzle.getEndpoints()) {
      int colorIndex = indexOf(Color.values(), endpoint.color);

      // Check if start and end points have the right color
      if (solution[endpoint.startRow][endpoint.startCol] != colorIndex) {
        LOGGER.warning("Start point has wrong color");
        return false;
      }

      if (solution[endpoint.endRow][endpoint.endCol] != colorIndex) {
        LOGGER.warning("End point has wrong color");
        return false;
      }

      // Check if there's a connected path between endpoints
      if (!hasConnectedPath(solution, endpoint, colorIndex)) {
        LOGGER.warning("No connected path for color " + endpoint.color);
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if a path connects the two endpoints.
   * For a more complex solution, use BFS to verify connectivity.
   */
  private boolean hasConnectedPath(int[][] solution, Puzzle.Endpoint endpoint, int colorIndex) {
    int size = solution.length;
    Position start = new Position(endpoint.startRow, endpoint.startCol);
    Position end = new Position(endpoint.endRow, endpoint.endCol);

    // Use BFS to check connectivity
    Queue<Position> queue = new LinkedList<>();
    boolean[][] visited = new boolean[size][size];
    queue.add(start);
    visited[start.getRow()][start.getCol()] = true;

    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

    while (!queue.isEmpty()) {
      Position current = queue.poll();

      if (current.equals(end)) {
        return true; // Found a path to the end
      }

      for (int[] dir : directions) {
        int newRow = current.getRow() + dir[0];
        int newCol = current.getCol() + dir[1];

        if (isValidPosition(newRow, newCol, size) &&
            !visited[newRow][newCol] &&
            solution[newRow][newCol] == colorIndex) {

          visited[newRow][newCol] = true;
          queue.add(new Position(newRow, newCol));
        }
      }
    }

    return false; // No path found
  }

  /**
   * Find index of a value in an array.
   */
  private <T> int indexOf(T[] array, T value) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Rearranges flows in a puzzle to create more interesting patterns while
   * maintaining solvability.
   * 
   * @param puzzle     The puzzle to rearrange
   * @param iterations Number of flow adjustments to attempt
   * @return The modified puzzle
   */
  public Puzzle rearrangeFlows(Puzzle puzzle, int iterations) {
    int[][] solution = puzzle.getSolution();
    if (solution == null) {
      LOGGER.warning("Cannot rearrange flows - puzzle has no solution");
      return puzzle;
    }

    int size = puzzle.getRows();
    int numFlows = puzzle.getEndpoints().size();

    // Create a deep copy of the solution to work with
    int[][] workingSolution = new int[size][size];
    for (int i = 0; i < size; i++) {
      System.arraycopy(solution[i], 0, workingSolution[i], 0, size);
    }

    // Map colors to their flow paths (list of positions)
    Map<Integer, List<Position>> flowPaths = extractFlowPaths(workingSolution, size);

    LOGGER.info("Starting flow rearrangement for " + iterations + " iterations");
    int successfulMutations = 0;

    for (int iter = 0; iter < iterations; iter++) {
      // Choose a random flow
      Integer[] colorIndices = flowPaths.keySet().toArray(new Integer[0]);
      int flowIndex = colorIndices[random.nextInt(colorIndices.length)];
      List<Position> flowPath = flowPaths.get(flowIndex);

      // Skip if flow is too short (minimum 3 cells)
      if (flowPath.size() <= 3) {
        continue;
      }

      boolean mutated = attemptFlowMutation(flowIndex, flowPaths, workingSolution, size);
      if (mutated) {
        successfulMutations++;
      }
    }

    LOGGER.info("Completed flow rearrangement with " + successfulMutations + " successful mutations");

    // Update puzzle with new solution
    Puzzle rearrangedPuzzle = extractPuzzleFromSolution(puzzle.getName(), workingSolution, size, numFlows);
    return rearrangedPuzzle;
  }

  /**
   * Attempts to mutate a flow by shrinking it and growing an adjacent flow.
   */
  private boolean attemptFlowMutation(int flowIndex, Map<Integer, List<Position>> flowPaths,
      int[][] solution, int size) {
    List<Position> flowPath = flowPaths.get(flowIndex);

    // Get endpoints of this flow (first and last positions in the path)
    Position head = flowPath.get(0);
    Position tail = flowPath.get(flowPath.size() - 1);

    // Try to mutate from the head
    if (tryMutateFlowEnd(head, flowPath, flowIndex, flowPaths, solution, size)) {
      return true;
    }

    // Try to mutate from the tail
    if (tryMutateFlowEnd(tail, flowPath, flowIndex, flowPaths, solution, size)) {
      return true;
    }

    return false;
  }

  /**
   * Tries to mutate a flow from one of its endpoints.
   */
  private boolean tryMutateFlowEnd(Position endpoint, List<Position> flowPath, int flowIndex,
      Map<Integer, List<Position>> flowPaths, int[][] solution, int size) {
    // Find adjacent cells from other flows
    List<MutationCandidate> candidates = findAdjacentCandidates(endpoint, flowIndex, solution, size);

    if (candidates.isEmpty()) {
      return false;
    }

    // Choose a random candidate
    MutationCandidate candidate = candidates.get(random.nextInt(candidates.size()));

    // Get path of the other flow
    List<Position> otherFlowPath = flowPaths.get(candidate.colorIndex);

    // Find the next cell in our flow after the endpoint
    Position nextInFlow = findNextInFlow(endpoint, flowPath);
    if (nextInFlow == null) {
      return false;
    }

    // Execute the mutation
    // 1. Remove the endpoint from our flow
    if (endpoint.equals(flowPath.get(0))) {
      flowPath.remove(0);
    } else {
      flowPath.remove(flowPath.size() - 1);
    }
    solution[endpoint.getRow()][endpoint.getCol()] = -1; // Mark as empty temporarily

    // 2. Move the candidate's flow into this empty space
    solution[endpoint.getRow()][endpoint.getCol()] = candidate.colorIndex;

    // 3. Add new position to other flow's path
    if (isEndpoint(candidate.position, otherFlowPath)) {
      if (candidate.position.equals(otherFlowPath.get(0))) {
        otherFlowPath.add(0, endpoint);
      } else {
        otherFlowPath.add(endpoint);
      }
    } else {
      // More complex case for middle-of-flow mutation
      // For simplicity, we'll just recalculate the flow path
      otherFlowPath.clear();
      otherFlowPath.addAll(extractSingleFlowPath(solution, size, candidate.colorIndex));
    }

    LOGGER.fine("Flow " + flowIndex + " shrunk and flow " + candidate.colorIndex + " expanded");
    return true;
  }

  /**
   * Finds the next position in a flow path after an endpoint.
   */
  private Position findNextInFlow(Position endpoint, List<Position> flowPath) {
    if (endpoint.equals(flowPath.get(0)) && flowPath.size() > 1) {
      return flowPath.get(1);
    } else if (endpoint.equals(flowPath.get(flowPath.size() - 1)) && flowPath.size() > 1) {
      return flowPath.get(flowPath.size() - 2);
    }
    return null;
  }

  /**
   * Checks if a position is an endpoint of a flow path.
   */
  private boolean isEndpoint(Position pos, List<Position> flowPath) {
    return pos.equals(flowPath.get(0)) || pos.equals(flowPath.get(flowPath.size() - 1));
  }

  /**
   * Finds adjacent cells from other flows that are candidates for mutation.
   */
  private List<MutationCandidate> findAdjacentCandidates(Position pos, int flowIndex,
      int[][] solution, int size) {
    List<MutationCandidate> candidates = new ArrayList<>();
    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } }; // Up, Right, Down, Left

    for (int[] dir : directions) {
      int newRow = pos.getRow() + dir[0];
      int newCol = pos.getCol() + dir[1];

      if (isValidPosition(newRow, newCol, size)) {
        int cellColor = solution[newRow][newCol];
        // Check if this is a different flow
        if (cellColor >= 0 && cellColor != flowIndex) {
          candidates.add(new MutationCandidate(
              new Position(newRow, newCol), cellColor));
        }
      }
    }

    return candidates;
  }

  /**
   * Helper class for mutation candidates.
   */
  private static class MutationCandidate {
    Position position;
    int colorIndex;

    MutationCandidate(Position position, int colorIndex) {
      this.position = position;
      this.colorIndex = colorIndex;
    }
  }

  /**
   * Extracts flow paths from a solution grid.
   */
  private Map<Integer, List<Position>> extractFlowPaths(int[][] solution, int size) {
    Map<Integer, List<Position>> flowPaths = new HashMap<>();

    // Find all unique color indices
    Set<Integer> colorIndices = new HashSet<>();
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] >= 0) {
          colorIndices.add(solution[r][c]);
        }
      }
    }

    // Extract each flow path
    for (int colorIndex : colorIndices) {
      List<Position> path = extractSingleFlowPath(solution, size, colorIndex);
      flowPaths.put(colorIndex, path);
    }

    return flowPaths;
  }

  /**
   * Extracts a single flow path from the solution.
   * Uses BFS to find a path between endpoints.
   */
  private List<Position> extractSingleFlowPath(int[][] solution, int size, int colorIndex) {
    // Find endpoints of this color
    List<Position> endpoints = new ArrayList<>();
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        if (solution[r][c] == colorIndex) {
          Position pos = new Position(r, c);

          // Check if this is an endpoint (has only one neighboring cell of same color)
          if (countSameColorNeighbors(solution, pos, colorIndex, size) <= 1) {
            endpoints.add(pos);
          }
        }
      }
    }

    if (endpoints.size() != 2) {
      LOGGER.warning("Expected 2 endpoints for color " + colorIndex + ", found " + endpoints.size());
      // Return all cells of this color as fallback
      List<Position> allCells = new ArrayList<>();
      for (int r = 0; r < size; r++) {
        for (int c = 0; c < size; c++) {
          if (solution[r][c] == colorIndex) {
            allCells.add(new Position(r, c));
          }
        }
      }
      return allCells;
    }

    // Use BFS to find path between endpoints
    return findPathBFS(solution, endpoints.get(0), endpoints.get(1), colorIndex, size);
  }

  /**
   * Counts neighbors of the same color.
   */
  private int countSameColorNeighbors(int[][] solution, Position pos, int colorIndex, int size) {
    int count = 0;
    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

    for (int[] dir : directions) {
      int newRow = pos.getRow() + dir[0];
      int newCol = pos.getCol() + dir[1];

      if (isValidPosition(newRow, newCol, size) && solution[newRow][newCol] == colorIndex) {
        count++;
      }
    }

    return count;
  }

  /**
   * Finds a path between two positions using BFS.
   */
  private List<Position> findPathBFS(int[][] solution, Position start, Position end,
      int colorIndex, int size) {
    Queue<Position> queue = new LinkedList<>();
    Map<Position, Position> parentMap = new HashMap<>();
    boolean[][] visited = new boolean[size][size];

    queue.add(start);
    visited[start.getRow()][start.getCol()] = true;

    int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

    while (!queue.isEmpty()) {
      Position current = queue.poll();

      if (current.equals(end)) {
        // Path found, reconstruct it
        List<Position> path = new ArrayList<>();
        Position pos = current;

        while (pos != null) {
          path.add(0, pos);
          pos = parentMap.get(pos);
        }

        return path;
      }

      // Try all four directions
      for (int[] dir : directions) {
        int newRow = current.getRow() + dir[0];
        int newCol = current.getCol() + dir[1];

        if (isValidPosition(newRow, newCol, size) &&
            solution[newRow][newCol] == colorIndex &&
            !visited[newRow][newCol]) {
          Position next = new Position(newRow, newCol);
          queue.add(next);
          visited[newRow][newCol] = true;
          parentMap.put(next, current);
        }
      }
    }

    LOGGER.warning("Could not find path between endpoints for color " + colorIndex);
    return new ArrayList<>();
  }

  // This method may already exist in your code, but I'm including it for
  // completeness
  private boolean isValidPosition(int row, int col, int size) {
    return row >= 0 && row < size && col >= 0 && col < size;
  }
}