package com.dots;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Dot {
  public enum Color { RED, BLUE };
  Color color;
  int x;
  int y;
  
  public Dot(Color color, int x, int y) {
    this.color = color;
    this.x = x;
    this.y = y;
  }
  
  public void draw(Canvas canvas, Paint paint) {
    canvas.drawCircle(x, y, 10, paint);
  }
}
