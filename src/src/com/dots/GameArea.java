package com.dots;

import static com.dots.model.StoneColor.BLACK;
import static com.dots.model.StoneColor.WHITE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dots.model.Game;
import com.dots.model.Game.NoLibertiesException;
import com.dots.model.Game.SpaceTakenException;
import com.dots.model.Stone;

public class GameArea extends ViewGroup {
  final static int PADDING = 3;

  /** For painting grid. */
  final Paint mPaintGrid = new Paint();
  /** Layout area. Mutable. */
  final Rect mRect = new Rect(0, 0, 0, 0);
  /** Row-major. */
  final StoneView[][] stoneViews;

  Game mGame;
  GameState mGameState;
  int mCellSize;
  float[] mGrid;

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);

    mGame = new Game(10, BLACK);
    mGame.add(
        new Stone(1, 2, WHITE),
        new Stone(3, 2, WHITE),
        new Stone(4, 2, BLACK));

    stoneViews = new StoneView[mGame.getTableSize()][mGame.getTableSize()];
    for (Stone stone : mGame.getStones()) {
      addStoneView(stone);
    }

    mPaintGrid.setColor(Color.DKGRAY);
    mPaintGrid.setStrokeWidth(2f);
    mPaintGrid.setStrokeCap(Paint.Cap.ROUND);

    // Padding for drawing children (stones);
    setPadding(PADDING, PADDING, PADDING, PADDING);

    mGame.setListener(new GameListener());
  }

  private void addStoneView(Stone stone) {
    StoneView view = new StoneView(stone, getContext());
    stoneViews[stone.getRow()][stone.getCol()] = view;
    addView(view);
  }

  void setGameState(GameState gameState) {
    mGameState = gameState;
  }

  /** @return Position on the canvas for given row (zero-based) */
  private int getX(int row) {
    int l = mRect.left + mCellSize / 2 + PADDING;
    int r = mRect.right - mCellSize / 2 - PADDING;
    return getCoord(row, l, r);
  }

  /** @return Position on canvas for given row (zero-based) */
  private int getY(int col) {
    int l = mRect.top + mCellSize / 2 + PADDING;
    int r = mRect.bottom - mCellSize / 2 - PADDING;
    return getCoord(col, l, r);
  }

  private int getCoord(int i, int min, int max) {
    // l + (r-l) * i/N
    // But N-1 because we want i/N to be equal to 1 at the last line.
    return min + (max - min) * i / (mGame.getTableSize() - 1);
  }

  /** @return Row (zero-based) for given position on the canvas */
  private int getRow(float y) {
    return Math.round(0.5f + y / (mRect.bottom - mRect.top) * mGame.getTableSize()) - 1;
  }

  /** @return Column (zero-based) for given position on the canvas */
  private int getCol(float x) {
    return Math.round(0.5f + x / (mRect.right - mRect.left) * mGame.getTableSize()) - 1;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Grid
    canvas.drawLines(mGrid, mPaintGrid);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    mRect.left = l;
    mRect.top = t;
    mRect.right = Math.min(r, b);
    mRect.bottom = Math.min(r, b);

    mCellSize = (int) (mRect.width() / (float) mGame.getTableSize());
    mCellSize -= mCellSize % 2;

    if (mGrid == null) {
      mGrid = new float[mGame.getTableSize() * 8];

      float minX = getX(0);
      float maxX = getX(mGame.getTableSize() - 1);
      float minY = getY(0);
      float maxY = getY(mGame.getTableSize() - 1);
      for (int i = 0, at = 0; i < mGame.getTableSize(); ++i) {
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

    for (Stone stone : mGame.getStones()) {
      int x = getX(stone.getCol());
      int y = getY(stone.getRow());
      StoneView stoneView = stoneViews[stone.getRow()][stone.getCol()];
      stoneView.layout(
          x - mCellSize / 2,
          y - mCellSize / 2,
          x + mCellSize / 2,
          y + mCellSize / 2);
      // stoneView.layout(PADDING, PADDING, 120 - PADDING, 120 - PADDING);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        int row = getRow(event.getY());
        int col = getCol(event.getX());
        if (row >= 0 && row < mGame.getTableSize() &&
            col >= 0 && col < mGame.getTableSize()) {

          Log.d(Util.TAG, "Touch at row " + row + ", col" + col);
          try {
            mGame.makeTurnAt(row, col);
          } catch (SpaceTakenException e) {
            Toast.makeText(getContext(), "This space is taken", Toast.LENGTH_SHORT).show();
          } catch (NoLibertiesException e) {
            Toast.makeText(getContext(), "No liberty", Toast.LENGTH_SHORT).show();
          }
        }

        // if (!mGameState.isGamesApiConnected()) {
        // throw new IllegalStateException(GameActivity.class
        // + " must have checked for connectivity.");
        // } else {
        // xx = event.getX();
        // yy = event.getY();
        // if (PADDING <= xx && xx <= PADDING + STONE_SIZE *
        // mGame.getTableSize() &&
        // PADDING <= yy && yy <= PADDING + STONE_SIZE * mGame.getTableSize()) {
        // Colour currentTurn = mGameState.getCurrentTurn();
        // if (currentTurn != null) {
        // if (mGameState.addDot(currentTurn, roundCoordinate(xx),
        // roundCoordinate(yy))) {
        // invalidate();
        // return true;
        // }
        // } else {
        // Toast.makeText(getContext(), "Waiting for opponent's turn",
        // Toast.LENGTH_LONG).show();
        // }
        // }
        // }
    }
    return super.onTouchEvent(event);
  }

  private class GameListener implements com.dots.model.GameListener {
    @Override
    public void onStoneAdded(Stone stone) {
      addStoneView(stone);
    }

    @Override
    public void onStoneCaptured(Stone stone) {
      StoneView stoneView = stoneViews[stone.getRow()][stone.getCol()];
      removeView(stoneView);
    }
  }

  // @Override
  // protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
  // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  //
  // if (getMeasuredWidth() < getMeasuredHeight()) {
  // getLayoutParams().height = getMeasuredWidth();
  // } else {
  // getLayoutParams().width = getMeasuredHeight();
  // }
  // }
  //
  // private void drawDotsForColor(Dot.Colour color, Canvas canvas) {
  // int savedColor = mPaint.getColor();
  // try {
  // // Color
  // mPaint.setColor(Color.BLACK);
  // for (Dot d : mGameState.getDots(color)) {
  // float x0 = cell2Coord(d.x);
  // float y0 = cell2Coord(d.y);
  //
  // canvas.drawCircle(x0, y0, 10, mPaint);
  // for (int i = 0; i < (Dot.NUM_DIRECTIONS >> 1); ++i) {
  // if (d.neignbours[i]) {
  // float x1 = cell2Coord(d.getNx(i));
  // float y1 = cell2Coord(d.getNy(i));
  // canvas.drawLine(x0, y0, x1, y1, mPaint);
  // }
  // }
  // }
  // //
  // } finally {
  // mPaint.setStrokeWidth(1);
  // mPaint.setColor(savedColor);
  // }
  // }
  //
  // private void drawDots(Canvas canvas) {
  // drawDotsForColor(Dot.Colour.CL_BLUE, canvas);
  // drawDotsForColor(Dot.Colour.CL_RED, canvas);
  // }
  //
  // public void erase() {
  // mGameState.reset();
  // invalidate();
  // }
  //
  // private int roundCoordinate(float t) {
  // return (int) ((t - PADDING) / STONE_SIZE + 0.5);
  // }
  //
  // private int cell2Coord(int t) {
  // return PADDING + t * STONE_SIZE;
  // }
}
