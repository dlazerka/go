package com.dots;

public class Stone {
  final int row;
  final int col;
  final boolean white;

  public static Stone white(int row, int col) {
    return new Stone(row, col, true);
  }

  public static Stone black(int row, int col) {
    return new Stone(row, col, false);
  }

  private Stone(int row, int col, boolean white) {
    this.row = row;
    this.col = col;
    this.white = white;
  }

  public boolean isWhite() {
    return white;
  }

  public boolean isBlack() {
    return !white;
  }
}
