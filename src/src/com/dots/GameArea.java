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
    // draw
    Rect rect = canvas.getClipBounds();
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
  }
  
  private void drawDotsForColor(Dot.Colour color, Canvas canvas) {
    int savedColor = mPaint.getColor();
    try {
      //Color
      mPaint.setColor(Dot.systemColor(color));
      for (Dot d : mGameState.getDots(color)) {
        float x0 = cell2Coord(d.x);
        float y0 = cell2Coord(d.y);
        canvas.drawCircle(x0, y0, 10, mPaint);
        for (int i = 0; i < (Dot.NUM_DIRECTIONS >> 1); ++i) {
          if (d.neignbours[i]) {
            float x1 = cell2Coord(d.getNx(i));
            float y1 = cell2Coord(d.getNy(i));
            canvas.drawLine(x0, y0, x1, y1, mPaint);
          }
        }
      }
      //
    } finally {
      mPaint.setColor(savedColor);
    }
  }
  
  private void drawDots(Canvas canvas) {
    //for ()
    drawDotsForColor(Dot.Colour.CL_BLUE, canvas);
    drawDotsForColor(Dot.Colour.CL_RED, canvas);
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
          //mGameState.flipTurn();
          invalidate();
          return true;
        }
        //canvas.drawCircle(x, y, r, mPaint);
        
      }
    }
    return super.onTouchEvent(event);
  }

}
