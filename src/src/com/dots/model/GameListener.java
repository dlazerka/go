package com.dots.model;

public interface GameListener {
  void onStateAdvanced(GameState newState);
  void onGameReset();
}