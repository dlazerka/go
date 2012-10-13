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

  @Override
  public String toString() {
    return color + "(" + row + ", " + col + ")";
  }

  /** For comparing game states. */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + col;
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    result = prime * result + row;
    return result;
  }

  /** For comparing game states. */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Stone other = (Stone) obj;
    if (col != other.col)
      return false;
    if (color != other.color)
      return false;
    if (row != other.row)
      return false;
    return true;
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
