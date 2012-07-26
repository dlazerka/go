package com.dots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GameArea extends View {
  // A paint object to help us draw the lines.
  Paint mPaint;
  GameState mGameState;
  final static int MARGIN = 10;
  final static int NUM_CELLS = GameState.SIZE - 1;
  int mCellSize;
  //Dot currentDot;

  float[] mGrid;
  // The constructor just initializes the paint object and sets some default colors.
  public GameArea(Context context, GameState gameState) {
     super(context);
     mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
     setBackgroundColor(Color.WHITE);
     mPaint.setColor(Color.BLACK);
     mGameState = gameState;
     //mCurrentDot = new Dot();
  }
  
  private float[] computeGridLines(Rect rect) {
    int l = rect.left;
    int r = rect.right;
    int t = rect.top;
    int b = rect.bottom;
    //int numCells = 10;
    
    int minDimension = Math.min(r - l, (b - t));
    // leave 10 pts for margin.
    //final int margin = 10;
    mCellSize = (minDimension - MARGIN) / NUM_CELLS;
    //ArrayList<Integer> l = new ArrayList<Integer>();
    // Draw 2 * (num cells + 1) lines.
    int numLines = (NUM_CELLS + 1) << 1;
    // Each line is specified with 4 coordinates.
    float[] points = new float[numLines << 2];
    
    for (int i = 0, start = MARGIN, at = 0; i <= NUM_CELLS; ++i, start += mCellSize) {
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
    //canvas.drawRect(rect, mPaint);
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
     drawDots(canvas);
     //for (int)
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
  
  private void drawDot(Dot dot, Canvas canvas, Paint paint) {
    //
    //public void draw(Canvas canvas, Paint paint) {
    //paint.setColor(color);
    int savedColor = paint.getColor();
    try {
      //Color
      paint.setColor(dot.systemColor());
      canvas.drawCircle(cell2Coord(dot.x), cell2Coord(dot.y), 10, paint);
    } finally {
      paint.setColor(savedColor);
    }
    //}
  }
  
  private void drawDots(Canvas canvas) {
    //for ()
    for (Dot d : mGameState.getDots(Dot.Colour.CL_RED)) {
      drawDot(d, canvas, mPaint);
    }
    for (Dot d : mGameState.getDots(Dot.Colour.CL_BLUE)) {
      drawDot(d, canvas, mPaint);
    }
  }
  
  public void erase() {
    mGameState.reset();
    invalidate();
    //Log.d("123", "erased grid");
  }
  
  private int roundCoordinate(float t) {
    return (int)((t - MARGIN) / mCellSize + 0.5);
  }
  
  private int cell2Coord(int t) {
    return MARGIN + t * mCellSize;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float xx, yy;
    switch(event.getAction()) {
     /*
     case MotionEvent.ACTION_DOWN:
        currentLine = new Line(event.getX(), event.getY());
        invalidate();
        return true;
     case MotionEvent.ACTION_MOVE:
        currentLine.x1 = event.getX();
        currentLine.y1 = event.getY();
        invalidate();
        return true;
     */
    case MotionEvent.ACTION_DOWN:
      xx = event.getX();
      yy = event.getY();
      if (MARGIN <= xx && xx <= MARGIN + mCellSize * NUM_CELLS &&
          MARGIN <= yy && yy <= MARGIN + mCellSize * NUM_CELLS) {
        Log.d("action up", "at " + xx + ", " + yy);
        if (mGameState.addDot(mGameState.getCurrentTurn(), roundCoordinate(xx), roundCoordinate(yy))) {
          mGameState.flipTurn();
          invalidate();
          return true;
        }
        //canvas.drawCircle(x, y, r, mPaint);
        
      }
    }
    return super.onTouchEvent(event);
  }

}
