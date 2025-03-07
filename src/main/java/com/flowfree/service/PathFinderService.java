package com.flowfree.service;

import com.flowfree.model.Cell;
import com.flowfree.model.Grid;
import com.flowfree.model.Color;
import com.flowfree.model.Position;

import java.util.*;

/**
 * Service for finding paths between endpoints.
 */
public class PathFinderService {
  private final Grid grid;

  public PathFinderService(Grid grid) {
    this.grid = grid;
  }

  /**
   * Finds a path between two endpoints of the same color if possible.
   *
   * @param color The color to find a path for
   * @return List of cells forming a path, or empty list if no path found
   */
  public List<Cell> findPath(Color color) {
    List<Cell> endpoints = grid.getEndpoints(color);
    if (endpoints.size() != 2) {
      return Collections.emptyList();
    }

    Cell start = endpoints.get(0);
    Cell end = endpoints.get(1);

    return findPathBFS(start, end);
  }

  /**
   * Finds a path between start and end cells using BFS.
   *
   * @param start Starting cell
   * @param end   Target cell
   * @return List of cells forming the path, or empty list if no path found
   */
  private List<Cell> findPathBFS(Cell start, Cell end) {
    Queue<Cell> queue = new LinkedList<>();
    Map<Cell, Cell> parentMap = new HashMap<>();
    Set<Cell> visited = new HashSet<>();

    queue.add(start);
    visited.add(start);

    while (!queue.isEmpty()) {
      Cell current = queue.poll();

      if (current.equals(end)) {
        return reconstructPath(start, end, parentMap);
      }

      for (Cell neighbor : grid.getAdjacentCells(current)) {
        if (!visited.contains(neighbor) && (neighbor.isEmpty() || neighbor.equals(end))) {
          visited.add(neighbor);
          queue.add(neighbor);
          parentMap.put(neighbor, current);
        }
      }
    }

    return Collections.emptyList(); // No path found
  }

  /**
   * Reconstructs path from the parent map.
   */
  private List<Cell> reconstructPath(Cell start, Cell end, Map<Cell, Cell> parentMap) {
    List<Cell> path = new ArrayList<>();
    Cell current = end;

    while (!current.equals(start)) {
      path.add(current);
      current = parentMap.get(current);
    }

    Collections.reverse(path);
    return path;
  }

  /**
   * Checks if a path can be found between the endpoints of the given color.
   *
   * @param color The color to check
   * @return True if a path is possible, false otherwise
   */
  public boolean isPathPossible(Color color) {
    return !findPath(color).isEmpty();
  }
}