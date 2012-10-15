package name.dlazerka.go;

import roboguice.RoboGuice;
import name.dlazerka.go.model.Game;

import com.google.inject.Inject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class GameAreaGrid extends View {
  @Inject
  Game mGame;
  Paint mPaintGrid = new Paint();
  Rect mRect = new Rect();
  float[] mGrid;
  int mCellSize;

  public GameAreaGrid(Context context, AttributeSet attrs) {
    super(context, attrs);
    RoboGuice.injectMembers(context, this);

    mPaintGrid.setColor(Color.DKGRAY);
    mPaintGrid.setStrokeWidth(2f);
    mPaintGrid.setStrokeCap(Paint.Cap.ROUND);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
//    if (mPaintGrid != null)
    // Grid
    canvas.drawLines(mGrid, mPaintGrid);
//    mPaintGrid = null;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (!changed) return;
    mRect.left = l;
    mRect.top = t;
    mRect.right = Math.min(r, b);
    mRect.bottom = Math.min(r, b);

    mCellSize = mRect.width() / mGame.getTableSize();
    mCellSize -= mCellSize % 2;

    if (mGrid == null) {
      mGrid = new float[mGame.getTableSize() * 8];
    }
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

  /** @return Position on the canvas for given row (zero-based) */
  private int getX(int row) {
    int l = mRect.left + mCellSize / 2;
    int r = mRect.right - mCellSize / 2;
    return getCoord(row, l, r);
  }

  /** @return Position on canvas for given row (zero-based) */
  private int getY(int col) {
    int l = mRect.top + mCellSize / 2;
    int r = mRect.bottom - mCellSize / 2;
    return getCoord(col, l, r);
  }

  private int getCoord(int i, int min, int max) {
    // l + (r-l) * i/N
    // But N-1 because we want i/N to be equal to 1 at the last line.
    return min + (max - min) * i / (mGame.getTableSize() - 1);
  }

}
