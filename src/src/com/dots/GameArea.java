package com.dots;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dots.Dot.Colour;

public class GameArea extends ViewGroup {
  GameState mGameState;
  final static int PADDING = 3;
  final static int CELL_SIZE = 44;
  final static int GRID_SIZE = 10;

  float[] mGrid;
  LayoutInflater mLi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

  final Paint mPaintGrid = new Paint();
  final Paint mPaint;

  /** Layout area. Mutable. */
  final Rect mRect = new Rect(0, 0, 0, 0);

  /** Row-major. */
  final StoneView[][] stones = new StoneView[GRID_SIZE][GRID_SIZE];

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);

    mPaintGrid.setColor(Color.BLACK);
    mPaintGrid.setStrokeWidth(2f);
    mPaintGrid.setStrokeCap(Paint.Cap.ROUND);

    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(Color.BLACK);
    mPaint.setStrokeCap(Paint.Cap.SQUARE);
    // Padding for drawing children (stones);
    setPadding(PADDING, PADDING, PADDING, PADDING);

    stones[0][0] = new WhiteStoneView(getContext(), CELL_SIZE);
    for (int row = 0; row < GRID_SIZE; row++) {
      for (int col = 0; col < GRID_SIZE; col++) {
        if (stones[row][col] != null)
          addView(stones[row][col]);
      }
    }
  }

  void setGameState(GameState gameState) {
    mGameState = gameState;
  }

  /** @return Position on the canvas for given row (zero-based) */
  private int getX(int row) {
    int l = mRect.left + CELL_SIZE/2 + PADDING;
    int r = mRect.right - CELL_SIZE/2 - PADDING;
    return getCoord(row, l, r);
  }

  /** @return Position on canvas for given row (zero-based) */
  private int getY(int col) {
    int l = mRect.top + CELL_SIZE/2 + PADDING;
    int r = mRect.bottom - CELL_SIZE/2 - PADDING;
    return getCoord(col, l, r);
  }

  private int getCoord(int i, int min, int max) {
    // l + (r-l) * i/N
    // But N-1 because we want i/N to be equal to 1 at the last line.
    return min + (max-min) * i / (GRID_SIZE - 1);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Grid
    canvas.drawLines(mGrid, mPaintGrid);

    // Stones
    for (int row = 0; row < GRID_SIZE; row++) {
      for (int col = 0; col < GRID_SIZE; col++) {
        if (stones[row][col] != null)
          stones[row][col].draw(canvas);
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getMeasuredWidth() < getMeasuredHeight()) {
      getLayoutParams().height = getMeasuredWidth();
    } else {
      getLayoutParams().width = getMeasuredHeight();
    }
  }

  private void drawDotsForColor(Dot.Colour color, Canvas canvas) {
    int savedColor = mPaint.getColor();
    try {
      // Color
      mPaint.setColor(Color.BLACK);
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
    return (int) ((t - PADDING) / CELL_SIZE + 0.5);
  }

  private int cell2Coord(int t) {
    return PADDING + t * CELL_SIZE;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float xx, yy;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        if (!mGameState.isGamesApiConnected()) {
          throw new IllegalStateException(GameActivity.class + " must have checked for connectivity.");
        } else {
          xx = event.getX();
          yy = event.getY();
          if (PADDING <= xx && xx <= PADDING + CELL_SIZE * GRID_SIZE &&
              PADDING <= yy && yy <= PADDING + CELL_SIZE * GRID_SIZE) {
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
    postInvalidate();
    return super.onTouchEvent(event);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    mRect.left = l;
    mRect.top = t;
    mRect.right = r;
    mRect.bottom = b;

    if (mGrid == null) {
      mGrid = new float[GRID_SIZE * 8];

      float minX = getX(0);
      float maxX = getX(GRID_SIZE - 1);
      float minY = getY(0);
      float maxY = getY(GRID_SIZE - 1);
      for (int i = 0, at = 0; i < GRID_SIZE; ++i) {
        float x = getX(i);
        float y = getY(i);
        // vertical line
        mGrid[at++] = x;
        mGrid[at++] = minY;
        mGrid[at++] = x;
        mGrid[at++] = maxY;
        // horizontal line
        mGrid[at++] = minX;
        mGrid[at++] = y;
        mGrid[at++] = maxX;
        mGrid[at++] = y;
      }
    }
  }
}
