package com.dots.model;

public class Stone {
  final int row;
  final int col;
  final StoneColor color;

  public Stone(int row, int col, StoneColor color) {
    this.row = row;
    this.col = col;
    this.color = color;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public StoneColor getColor() {
    return color;
  }

}
