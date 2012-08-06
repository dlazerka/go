package com.dots;

import com.dots.Dot.Colour;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GameArea extends View {
  Paint mPaint;
  GameState mGameState;
  final static int MARGIN = 10;
  final static int NUM_CELLS = GameState.SIZE - 1;
  int mCellSize;

  float[] mGrid;

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(Color.BLACK);
    mPaint.setStrokeCap(Paint.Cap.SQUARE);
  }

  void setGameState(GameState gameState) {
    mGameState = gameState;
  }

  private float[] computeGridLines(Rect rect) {
    int minDimension = Math.min(rect.right - rect.left, (rect.bottom - rect.top));

    mCellSize = (minDimension - 2 * MARGIN) / NUM_CELLS;
    int l = MARGIN;
    int r = l + mCellSize * NUM_CELLS;
    int t = MARGIN;
    int b = t + mCellSize * NUM_CELLS;

    // ArrayList<Integer> l = new ArrayList<Integer>();
    // Draw 2 * (num cells + 1) lines.
    int numLines = (NUM_CELLS + 1) * 2;
    // Each line is specified with 4 coordinates.
    float[] points = new float[numLines * 4];

    for (int i = 0, start = MARGIN, at = 0; i <= NUM_CELLS; ++i, start += mCellSize) {
      // vertical
      points[at++] = start;
      points[at++] = t;
      points[at++] = start;
      points[at++] = b;
      // horizontal
      points[at++] = l;
      points[at++] = start;
      points[at++] = r;
      points[at++] = start;
    }
    return points;
  }

  private void drawGrid(Canvas canvas) {
    Rect rect = canvas.getClipBounds();
    if (mGrid == null) {
      mGrid = computeGridLines(rect);
    }
    canvas.drawLines(mGrid, mPaint);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawGrid(canvas);
    drawDots(canvas);
    displayScore(canvas);
  }

  private void displayScore(Canvas canvas) {
    Rect rect = canvas.getClipBounds();
    int l = rect.left;
    int r = rect.right;
    int b = rect.bottom;
    float textSize = mPaint.getTextSize();
    int color = mPaint.getColor();
    try {
      mPaint.setTextSize(70);
      mPaint.setColor(Color.BLUE);
      canvas.drawText(Integer.toString(mGameState.getScore(Dot.Colour.CL_BLUE)), l + 40, b - 25,
          mPaint);
      mPaint.setColor(Color.RED);
      canvas.drawText(Integer.toString(mGameState.getScore(Dot.Colour.CL_RED)), r - 80, b - 25,
          mPaint);
    } finally {
      mPaint.setTextSize(textSize);
      mPaint.setColor(color);
    }
  }

  private void drawDotsForColor(Dot.Colour color, Canvas canvas) {
    int savedColor = mPaint.getColor();
    try {
      // Color
      mPaint.setColor(Dot.systemColor(color));
      for (Dot d : mGameState.getDots(color)) {
        float x0 = cell2Coord(d.x);
        float y0 = cell2Coord(d.y);
        canvas.drawCircle(x0, y0, 10, mPaint);
        for (int i = 0; i < (Dot.NUM_DIRECTIONS >> 1); ++i) {
          if (d.neignbours[i]) {
            float x1 = cell2Coord(d.getNx(i));
            float y1 = cell2Coord(d.getNy(i));
            mPaint.setStrokeWidth(3);
            canvas.drawLine(x0, y0, x1, y1, mPaint);
          }
        }
      }
      //
    } finally {
      mPaint.setStrokeWidth(1);
      mPaint.setColor(savedColor);
    }
  }

  private void drawDots(Canvas canvas) {
    drawDotsForColor(Dot.Colour.CL_BLUE, canvas);
    drawDotsForColor(Dot.Colour.CL_RED, canvas);
  }

  public void erase() {
    mGameState.reset();
    invalidate();
  }

  private int roundCoordinate(float t) {
    return (int) ((t - MARGIN) / mCellSize + 0.5);
  }

  private int cell2Coord(int t) {
    return MARGIN + t * mCellSize;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float xx, yy;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        if (!mGameState.isGamesApiConnected()) {
          Toast.makeText(getContext(), "Connecting to Games API", Toast.LENGTH_LONG).show();
        } else {
          xx = event.getX();
          yy = event.getY();
          if (MARGIN <= xx && xx <= MARGIN + mCellSize * NUM_CELLS &&
              MARGIN <= yy && yy <= MARGIN + mCellSize * NUM_CELLS) {
            Log.d("action up", "at " + xx + ", " + yy);
            Colour currentTurn = mGameState.getCurrentTurn();
            if (currentTurn != null) {
              if (mGameState.addDot(currentTurn, roundCoordinate(xx), roundCoordinate(yy))) {
                invalidate();
                return true;
              }
            } else {
              Toast.makeText(getContext(), "Waiting for opponent's turn", Toast.LENGTH_LONG).show();
            }
          }
        }
    }
    return super.onTouchEvent(event);
  }

}
