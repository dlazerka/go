package name.dlazerka.go.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Game state *before* {@link #whoseTurn} placed her stone.
 */
public class GameState {
  /** Mutable for current turn, but must not be modified later. */
  final Set<Stone> stones;
  final StoneColor whoseTurn;
  /** 0-based */
  final int turnNo;
  final int blacksCaptured;
  final int whitesCaptured;

  GameState() {
    stones = new HashSet<Stone>();
    this.whoseTurn = StoneColor.BLACK;
    this.turnNo = 0;
    this.blacksCaptured = 0;
    this.whitesCaptured = 0;
  }

  GameState(
      StoneColor whoseTurn,
      int turnNo,
      Set<Stone> stones,
      int blacksCaptured,
      int whitesCaptured) {
    this.whoseTurn = whoseTurn;
    this.turnNo = turnNo;
    this.stones = stones;
    this.blacksCaptured = blacksCaptured;
    this.whitesCaptured = whitesCaptured;
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
}


