package com.dots;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class GameArea extends View {
  // A paint object to help us draw the lines.
  //Paint mPaint;

  // The constructor just initializes the paint object and sets some default colors.
  public GameArea(Context context) {
     super(context);
     /*
     paint = new Paint(Paint.ANTI_ALIAS_FLAG);
     setBackgroundColor(Color.WHITE);
     paint.setColor(Color.BLACK);
     */
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     /*
     for(Line line : lines) {
        canvas.drawLine(line.x0, line.y0, line.x1, line.y1, paint);
     }
     if (currentLine != null) {
        canvas.drawLine(currentLine.x0, currentLine.y0, currentLine.x1, currentLine.y1, paint);
     }
     */
  }

}
