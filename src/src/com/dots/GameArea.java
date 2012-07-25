package com.dots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class GameArea extends View {
  // A paint object to help us draw the lines.
  Paint mPaint;
  //GameState mGameState;

  float[] mGrid;
  // The constructor just initializes the paint object and sets some default colors.
  public GameArea(Context context) {
     super(context);
     mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     setBackgroundColor(Color.WHITE);
     mPaint.setColor(Color.BLACK);
  }
  
  private float[] computeGridLines(Rect rect) {
    int l = rect.left;
    int r = rect.right;
    int t = rect.top;
    int b = rect.bottom;
    int numCells = 10;
    int size = Math.min(r - l, (t - b)) / numCells;
    //ArrayList<Integer> l = new ArrayList<Integer>();
    // Draw 2 * (num cells + 1) lines.
    int numLines = (numCells + 1) << 1;
    // Each line is specified with 4 coordinates.
    float[] points = new float[numLines << 2];
    
    for (int i = 0, start = 0, at = 0; i <= numCells; ++i, start += size) {
      //for ()
      // horizontal.
      // (start, l) -> (start, r)
      points[at++] = start;
      points[at++] = l;
      points[at++] = start;
      points[at++] = r;
      // (b, start) -> (t, start)
      points[at++] = b;
      points[at++] = start;
      points[at++] = t;
      points[at++] = start;
    }
    return points;
  }
  
  private void drawGrid(Canvas canvas) {
    //for ()
    // draw
    Rect rect = canvas.getClipBounds();
    canvas.drawRect(rect, mPaint);
    /*
    int l = rect.left;
    int r = rect.right;
    int t = rect.top;
    int bottom = rect.bottom;
    int numCells = 10;
    int size = Math.min(r - l, (t - b)) / numCells;
    //ArrayList<Integer> l = new ArrayList<Integer>();
    float[] ppints
    for (int i = 0; i <= size; ++i) {
      
      
    }
    */
    if (mGrid == null) {
      // Lazily initialize mGrid.
      mGrid = computeGridLines(rect);
    }
    canvas.drawLines(mGrid, mPaint);
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     drawGrid(canvas);
     // renderGameState();
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
