package com.dots.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Game state *before* {@link #whoseTurn} placed her stone.
 */
public class GameState {
  /** Mutable for current turn, but must not be modified later. */
  private Set<Stone> stones = new HashSet<Stone>();
  private final StoneColor whoseTurn;
  /** 0-based */
  private final int turnNo;

  GameState() {
    this.whoseTurn = StoneColor.BLACK;
    this.turnNo = 0;
  }

  private GameState(GameState prev) {
    this.whoseTurn = prev.whoseTurn.other();
    this.turnNo = prev.turnNo + 1;
    stones.addAll(prev.stones);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((stones == null) ? 0 : stones.hashCode());
    return result;
  }

  /**
   * For Positional super-ko (PSK).
   * For Situational super-ko (SSK) need to include {@link #whoseTurn}.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GameState other = (GameState) obj;
    if (stones == null) {
      if (other.stones != null)
        return false;
    } else if (!stones.equals(other.stones))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "(" + whoseTurn + ": " + stones + ")";
  }

  public Set<Stone> getStones() {
    return stones;
  }

  public int getTurnNo() {
    return turnNo;
  }

  public StoneColor getWhoseTurn() {
    return whoseTurn;
  }

  GameState.Builder nextTurnBuilder() {
    return new GameState(this).new Builder();
  }

  class Builder {
    private final Stone[][] stonesIndex;

    Builder() {
      stonesIndex = new Stone[19][19];
      for (Stone stone: stones) {
        stonesIndex[stone.row][stone.col] = stone;
      }
    }

    void add(Stone stone) {
      stones.add(stone);
      stonesIndex[stone.row][stone.col] = stone;
    }
    void remove(Stone stone) {
      stones.remove(stone);
      stonesIndex[stone.row][stone.col] = null;
    }

    /** @return stone or null if row and col unvalid (for convenience). */
    Stone getStoneAt(int row, int col) {
      if (row < 0 ||row >= stonesIndex.length || col < 0 || col >= stonesIndex[row].length) {
        return null;
      }
      return stonesIndex[row][col];
    }

    GameState build() {
      stones = Collections.unmodifiableSet(stones);
      return GameState.this;
//      for (int row = 0; row < tableSize; row++) {
//        for (int col = 0; col < tableSize; col++) {
//          stonesIndex[row][col] = null;
//        }
//      }

    }
  }
}


