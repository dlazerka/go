package com.dots.model;

import static com.dots.model.StoneColor.*;
import java.util.ArrayList;
import java.util.Collection;

public class Game {
  final int tableSize;
  final Collection<Stone> stones;
  final Stone[][] stonesIndex;
  final StoneColor myColor;
  StoneColor currentTurn;
  GameListener listener;

  /** Table of viewed points for graph search. */
  final boolean[][] seenIndex;

  public Game(int tableSize, StoneColor myColor) {
    this.tableSize = tableSize;
    this.myColor = myColor;
    stones = new ArrayList<Stone>(tableSize * tableSize);
    stonesIndex = new Stone[tableSize][tableSize];
    seenIndex = new boolean[tableSize][tableSize];
    currentTurn = BLACK;
  }

  public int getTableSize() {
    return tableSize;
  }

  public Collection<Stone> getStones() {
    return stones;
  }

  public StoneColor getMyColor() {
    return myColor;
  }

  /** Bulk fill the table. No checks are made. */
  public void add(Stone... newStones) {
    for (int i = 0; i < newStones.length; i++) {
      Stone stone = newStones[i];
      stones.add(stone);
      stonesIndex[stone.row][stone.col] = stone;
    }
  }

  /** May accept invalid row and col */
  public Stone stoneAt(int row, int col) {
    if (row < 0 || col < 0 || row > tableSize - 1 || col > tableSize - 1) {
      return null;
    }
    return stonesIndex[row][col];
  }

  public void makeTurnAt(int row, int col) throws SpaceTakenException, NoLibertiesException {
    if (stonesIndex[row][col] != null) {
      throw new SpaceTakenException();
    }
    Stone stone = new Stone(row, col, currentTurn);

    // Adding, but may remove later, in case move is invalid (no liberty).
    add(stone);

    boolean libertyFound = false;
    // We need to check for liberties (except New Zealand rules).
    // First, we check if we're capturing enemy stone so that liberty appears.
    Stone[] stonesAround = getStonesAround(row, col);
    for (int i = 0; i < 4; i++) {
      Stone stoneAround = stonesAround[i];
      if (stoneAround != null && stoneAround.color != stone.color) {
        if (!hasLiberty(stoneAround)) {
          capture(stoneAround);
          libertyFound = true;
        }
      }
    }
    // Second, we check liberties.
    if (!libertyFound && !hasLiberty(stone)) {
      remove(stone);
      throw new NoLibertiesException();
    }

    currentTurn = currentTurn.other();
    listener.onStoneAdded(stone);
  }

  private Stone[] getStonesAround(int row, int col) {
    Stone[] stonesAround = new Stone[] {
        stoneAt(row - 1, col),
        stoneAt(row + 1, col),
        stoneAt(row, col - 1),
        stoneAt(row, col + 1) };
    return stonesAround;
  }

  /**
   * Given stone or its group has at least one liberty. Depth-first search.
   */
  private boolean hasLiberty(Stone stone) {
    int row = stone.row;
    int col = stone.col;

    if (row > 0 && stonesIndex[row - 1][col] == null ||
        col > 0 && stonesIndex[row][col - 1] == null ||
        row < tableSize - 1 && stonesIndex[row + 1][col] == null ||
        col < tableSize - 1 && stonesIndex[row][col + 1] == null) {
      // Reset seenIndex.
      for (int i = 0; i < tableSize; i++) {
        for (int j = 0; j < tableSize; j++) {
          seenIndex[i][j] = false;
        }
      }
      return true;
    }

    seenIndex[row][col] = true;
    Stone[] stonesAround = getStonesAround(row, col);
    for (int i = 0; i < 4; i++) {
      Stone s = stonesAround[i];
      if (s != null && !seenIndex[s.row][s.col] && s.color == stone.color) {
        if (hasLiberty(s)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Depth-first search.
   * Seen index is not needed, because we remove seen vertices.
   */
  private void capture(Stone stone) {
    int row = stone.row;
    int col = stone.col;
    remove(stone);
    listener.onStoneCaptured(stone);
    Stone[] stonesAround = getStonesAround(row, col);
    for (int i = 0; i < 4; i++) {
      Stone s = stonesAround[i];
      if (s == null || stonesIndex[s.row][s.col] == null) {
        // Stone may be already captured from another path.
        continue;
      }
      if (s.color == stone.color) {
        capture(s);
      }
    }
  }

  private void remove(Stone stone) {
    stones.remove(stone);
    stonesIndex[stone.row][stone.col] = null;
  }

  public void setListener(GameListener listener) {
    this.listener = listener;
  }

  public class SpaceTakenException extends Exception {
  }

  public class NoLibertiesException extends Exception {
  }
}
