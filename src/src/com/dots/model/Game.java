package com.dots.model;

import static com.dots.model.StoneColor.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import roboguice.util.Ln;

public class Game {
  final HashSet<Stone> stones;
  final int tableSize;
  final Stone[][] stonesIndex;
  final StoneColor myColor;
  StoneColor currentTurn;
  GameListener listener;
  boolean lastPassed;

  /**
   * Komi multiplied by two (to store to int). Komi is score that added to
   * White's to compensate for Black first-move advantage. In handicap, komi is
   * 0.5, otherwise: American rules: 7.5 Chineese rules: 5.5. Japanese rules:
   * 6.5 (since 2002).
   */
  int twoKomi = 13;

  /**
   * Ordered collection of game states. One state per turn. The very first state
   * should contain one (or more for handicap) black stones, no white.
   */
  List<Set<Stone>> history;

  /** Table of viewed points for graph search. */
  final boolean[][] seenIndex;

  public Game(int tableSize, StoneColor myColor) {
    this.tableSize = tableSize;
    this.myColor = myColor;
    stones = new HashSet<Stone>(tableSize * tableSize);
    stonesIndex = new Stone[tableSize][tableSize];
    seenIndex = new boolean[tableSize][tableSize];
    currentTurn = BLACK;
    history = new ArrayList<Set<Stone>>(tableSize * tableSize / 2);
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
  public void add(Collection<Stone> newStones) {
    add(newStones.toArray(new Stone[newStones.size()]));
  }

  /** Bulk fill the table. No checks are made. */
  public void add(Stone... newStones) {
    for (int i = 0; i < newStones.length; i++) {
      Stone stone = newStones[i];
      stones.add(stone);
      stonesIndex[stone.row][stone.col] = stone;
    }
  }

  /** May accept invalid row and col. */
  public Stone stoneAt(int row, int col) {
    if (row < 0 || col < 0 || row > tableSize - 1 || col > tableSize - 1)
      return null;
    return stonesIndex[row][col];
  }

  public void passTurn() {
    currentTurn = currentTurn.other();
    Ln.i(currentTurn + " passed");
    history.add(newState());
    if (lastPassed) {
      Ln.i("Two consecutive passes: game ended");
      resetGame();
    }
    lastPassed = true;
  }

  @SuppressWarnings("unchecked")
  Set<Stone> newState() {
    return Collections.unmodifiableSet((Set<Stone>) stones.clone());
  }

  void resetGame() {
    history.clear();
    clearState();
    currentTurn = BLACK;
    lastPassed = false;
    listener.onGameReset();
  }

  void clearState() {
    stones.clear();
    for (int row = 0; row < tableSize; row++) {
      for (int col = 0; col < tableSize; col++) {
        stonesIndex[row][col] = null;
      }
    }
  }

  public void makeTurnAt(int row, int col) throws SpaceTakenException, NoLibertiesException,
      KoRuleException {
    if (stonesIndex[row][col] != null)
      throw new SpaceTakenException();
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

    Set<Stone> state = newState();
    // Check super ko rule.
    int turnNo = history.lastIndexOf(state);
    if (turnNo > -1) {
      Set<Stone> lastState = history.get(history.size() - 1);
      clearState();
      add(lastState);
      throw new KoRuleException(history.size() - turnNo);
    }
    history.add(state);

    lastPassed = false;
    currentTurn = currentTurn.other();
    notifyListener();
  }

  /** Notifies listener for differences that occured between two last game states. */
  void notifyListener() {
    Set<Stone> prevState = history.size() > 1
        ? history.get(history.size() - 2)
        : Collections.<Stone>emptySet();
    Set<Stone> newState = history.get(history.size() - 1);
    Set<Stone> prevStateM = new HashSet<Stone>(prevState);
    Set<Stone> newStateM = new HashSet<Stone>(newState);

    prevStateM.removeAll(newState);
    for (Stone stone : prevStateM) {
      listener.onStoneCaptured(stone);
    }

    newStateM.removeAll(prevState);
    for (Stone stone : newStateM) {
      listener.onStoneAdded(stone);
    }
  }

  Stone[] getStonesAround(int row, int col) {
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
  boolean hasLiberty(Stone stone) {
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
        if (hasLiberty(s))
          return true;
      }
    }
    return false;
  }

  /**
   * Depth-first search.
   * Seen index is not needed, because we remove seen vertices.
   */
  void capture(Stone stone) {
    int row = stone.row;
    int col = stone.col;
    remove(stone);
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

  void remove(Stone stone) {
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

  public class KoRuleException extends Exception {
    final int turnsAgo;

    public KoRuleException(int turnsAgo) {
      this.turnsAgo = turnsAgo;
    }

    public int getTurnsAgo() {
      return turnsAgo;
    }
  }
}
