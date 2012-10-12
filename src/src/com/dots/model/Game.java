package com.dots.model;

import java.util.ArrayList;
import java.util.Collection;

import com.dots.model.GameListener.StoneAddedEvent;

public class Game {
  final int tableSize;
  final Collection<Stone> stones;
  final Stone[][] stonesIndex;
  final StoneColor myColor;
  GameListener listener;

  public Game(int tableSize, StoneColor myColor) {
    this.tableSize = tableSize;
    this.myColor = myColor;
    stones = new ArrayList<Stone>(tableSize * tableSize);
    stonesIndex = new Stone[tableSize][tableSize];
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

  public void add(Stone... newStones) throws SpaceTakenException {
    for (int i = 0; i < newStones.length; i++) {
      Stone stone = newStones[i];
      if (stonesIndex[stone.row][stone.col] != null) {
        throw new SpaceTakenException(stone.row, stone.row);
      }
    }
    for (int i = 0; i < newStones.length; i++) {
      Stone stone = newStones[i];
      stones.add(stone);
      stonesIndex[stone.row][stone.col] = stone;
    }
  }

  public void makeTurnAt(int row, int col) throws SpaceTakenException {
    if (stonesIndex[row][col] != null) {
      throw new SpaceTakenException(row, col);
    }
    Stone newStone = new Stone(row, col, myColor);
    stones.add(newStone);
    stonesIndex[row][col] = newStone;
    fire(new StoneAddedEvent(newStone));
  }

  private void fire(StoneAddedEvent event) {
    listener.onStoneAdded(event);
  }

  public void setListener(GameListener listener) {
    this.listener = listener;
  }

  public class SpaceTakenException extends Exception {
    final int row;
    final int col;
    public SpaceTakenException(int row, int col) {
      this.row = row;
      this.col = col;
    }

  }
}
