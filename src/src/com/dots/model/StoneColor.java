package com.dots.model;

public enum StoneColor {
    BLACK, WHITE;

    public StoneColor other() {
      return this == BLACK ? WHITE : BLACK;
    }
}
