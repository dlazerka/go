package name.dlazerka.go.model;

import static name.dlazerka.go.model.StoneColor.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/** Turn making algorithm implementation. */
class TurnMaker {
  private final Game game;
  final Stone[][] stoneIndex ;
  /** Table of viewed points for graph search. */
  final boolean[][] seenIndex;
  private Stone addedStone;
  final Set<Stone> captured;

  TurnMaker(Game game) {
    this.game = game;
    stoneIndex = new Stone[game.tableSize][game.tableSize];
    seenIndex = new boolean[game.tableSize][game.tableSize];
    for (Stone stone : game.getLastState().stones) {
      stoneIndex[stone.row][stone.col] = stone;
    }
    captured = new HashSet<Stone>();
  }

  GameState makeTurnAt(int row, int col)
      throws SpaceTakenException, NoLibertiesException, KoRuleException {
    if (getStoneAt(row, col) != null) {
      throw new SpaceTakenException();
    }

    addedStone = new Stone(row, col, game.getLastState().getWhoseTurn());
    stoneIndex[row][col] = addedStone;

    boolean libertyFound = false;
    // We need to check for liberties (except New Zealand rules).
    // First, we check if we're capturing enemy stone so that liberty appears.
    Stone[] stonesAround = getStonesAround(row, col);
    for (int i = 0; i < 4; i++) {
      Stone stoneAround = stonesAround[i];
      if (stoneAround != null && stoneAround.color != addedStone.color) {
        if (!hasLiberty(stoneAround)) {
          capture(stoneAround);
          libertyFound = true;
        }
      }
    }
    // Second, we check liberties.
    if (!libertyFound && !hasLiberty(addedStone)) {
      throw new NoLibertiesException();
    }

    // Check positional super ko (PSK) rule.
    GameState newState = buildGameState();
    int turnNo = game.history.lastIndexOf(newState);
    if (turnNo > -1) {
      throw new KoRuleException(game.history.size() - turnNo);
    }
    return newState;
  }

  /** @return stone or null if row and col unvalid (for convenience). */
  Stone getStoneAt(int row, int col) {
    if (row < 0 ||row >= stoneIndex.length || col < 0 || col >= stoneIndex[row].length) {
      return null;
    }
    return stoneIndex[row][col];
  }

  Stone[] getStonesAround(int row, int col) {
    Stone[] stonesAround = new Stone[] {
        getStoneAt(row - 1, col),
        getStoneAt(row + 1, col),
        getStoneAt(row, col - 1),
        getStoneAt(row, col + 1)
    };
    return stonesAround;
  }

  /**
   * Given stone or its group has at least one liberty. Depth-first search.
   */
  boolean hasLiberty(Stone stone) {
    int row = stone.row;
    int col = stone.col;

    if (getStoneAt(row - 1, col) == null ||
        getStoneAt(row, col - 1) == null ||
        getStoneAt(row + 1, col) == null ||
        getStoneAt(row, col + 1) == null) {
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
    captured.add(stone);
    stoneIndex[row][col] = null;

    Stone[] stonesAround = getStonesAround(row, col);
    for (int i = 0; i < 4; i++) {
      Stone s = stonesAround[i];
      if (s == null || getStoneAt(s.row, s.col) == null) {
        // Stone may be already captured from another path.
        continue;
      }
      if (s.color == stone.color) {
        capture(s);
      }
    }
  }

  GameState buildGameState() {
    GameState lastState = game.getLastState();
    Set<Stone> newStones = new HashSet<Stone>();
    newStones.addAll(lastState.getStones());
    newStones.removeAll(captured);
    if (addedStone != null) {
      newStones.add(addedStone);
    }

    int blacksCaptured = lastState.whoseTurn == BLACK ? 0 : captured.size();
    int whitesCaptured = lastState.whoseTurn == WHITE ? 0 : captured.size();
    GameState newGameState = new GameState(
        lastState.whoseTurn.other(),
        lastState.turnNo + 1,
        Collections.unmodifiableSet(newStones),
        lastState.blacksCaptured + blacksCaptured,
        lastState.whitesCaptured + whitesCaptured
    );
    return newGameState;
  }
}
