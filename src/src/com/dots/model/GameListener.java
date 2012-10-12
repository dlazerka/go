package com.dots.model;

public interface GameListener {
  void onStoneAdded(StoneAddedEvent event);

  public interface GameEvent {}

  public static class StoneAddedEvent implements GameEvent {
    final Stone stone;

    public StoneAddedEvent(Stone stone) {
      this.stone = stone;
    }

    public Stone getStone() {
      return stone;
    }
  }
}