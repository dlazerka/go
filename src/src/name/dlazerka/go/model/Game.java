package name.dlazerka.go.model;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.Ln;

/** Interface for GameState and turn making. */
public class Game {
  final int tableSize;

  boolean lastPassed ;

  final List<GameListener> listeners = new ArrayList<GameListener>();

  /**
   * Komi multiplied by two (to store to int)
   * Komi is score that added to White's to compensate for Black first-move advantage.
   * In handicap, komi is 0.5, otherwise:
   * American rules: 7.5
   * Japanese rules: 6.5 (since 2002).
   * Chineese rules: 5.5.
   */
  // int twoKomi = 13;

  /**
   * Ordered collection of game states. One state per turn. The very first state
   * should contain one (or more for handicap) black stones, no white.
   */
  List<GameState> history;

  public Game(int tableSize) {
    this.tableSize = tableSize;

    history = new ArrayList<GameState>(tableSize * tableSize / 2);
    resetGame();
  }

  public int getTableSize() {
    return tableSize;
  }

  public GameState getLastState() {
    return history.get(history.size() - 1);
  }

  public GameState getStateAt(int turnNo) {
    return history.get(turnNo);
  }

  /**
   * Chinese rules score: number of stones + number of captured dead stones.
   * Moonshine-life is not counted.
   * @return positive value: black wins, negative: white wins, zero: black wins (+1/2).
   */
  public int getCurrentScore() {
    return 0;
  }

  public void passTurn() {
    GameState newState = new TurnMaker(this).buildGameState();
    history.add(newState);
    if (lastPassed) {
      Ln.i("Two consecutive passes: game ended");
      resetGame();
    }
    lastPassed = true;
    notifyListener();
  }

  void resetGame() {
    history.clear();
    history.add(new GameState());
    lastPassed = false;
  }


  public void makeTurnAt(int row, int col)
      throws SpaceTakenException, NoLibertiesException, KoRuleException {
    TurnMaker turnMaker = new TurnMaker(this);
    GameState newState = turnMaker.makeTurnAt(row, col);
    history.add(newState);
    lastPassed = false;
    notifyListener();
  }

  public void addListener(GameListener listener) {
    listeners.add(listener);
  }

  /** Notifies listener for differences that occured between two last game states. */
  void notifyListener() {
    for (GameListener listener : listeners)
      listener.onStateAdvanced(getLastState());
  }
}
