package com.dots;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Dot {
  public enum Colour { CL_RED, CL_BLUE };
  Colour color;
  int x;
  int y;
  
  public Dot(Colour color, int x, int y) {
    this.color = color;
    this.x = x;
    this.y = y;
  }
  
  public int systemColor() {
    //
    switch (color) {
    case CL_RED: return Color.RED;
    case CL_BLUE: return Color.BLUE;
    }
    return 0;
  }
  /*
  public void draw(Canvas canvas, Paint paint) {
    //paint.setColor(color);
    int savedColor = paint.getColor();
    try {
      //Color
      paint.setColor(color2Int(this.color));
      canvas.drawCircle(x, y, 10, paint);
    } finally {
      paint.setColor(savedColor);
    }
  }
  */
}
