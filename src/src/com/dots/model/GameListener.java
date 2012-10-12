package com.dots.model;

public interface GameListener {
  void onStoneAdded(Stone stone);
  void onStoneCaptured(Stone stone);
}