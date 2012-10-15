package name.dlazerka.go.model;

public interface GameListener {
  void onStateAdvanced(GameState newState);
  void onGameReset();
}