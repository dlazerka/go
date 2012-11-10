package name.dlazerka.go;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GridDrawable extends Drawable {
  private static final float STROKE_WIDTH = 2f;
  final int boardSize;
  final Paint paint;
  Rect clipBounds;

  GridDrawable(int boardSize) {
    this.boardSize = boardSize;
    clipBounds = new Rect();
    paint = new Paint();
    paint.setColor(Color.DKGRAY);
    paint.setStrokeWidth(STROKE_WIDTH);
    paint.setStrokeCap(Paint.Cap.ROUND);
  }

  /** @return Position on the canvas for given row (zero-based) */
  private float getX(int row) {
    float p = (float) row / (boardSize - 1);
    return (clipBounds.width() - STROKE_WIDTH) * p
        + clipBounds.left + STROKE_WIDTH / 2;
  }

  /** @return Position on canvas for given row (zero-based) */
  private float getY(int col) {
    float p = (float) col / (boardSize - 1);
    return (clipBounds.height() - STROKE_WIDTH) * p
        + clipBounds.top + STROKE_WIDTH / 2;
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.getClipBounds(clipBounds);
    float minX = getX(0);
    float maxX = getX(boardSize - 1);
    float minY = getY(0);
    float maxY = getY(boardSize - 1);
    for (int i = 0; i < boardSize; i++) {
      float x = getX(i);
      float y = getY(i);
      canvas.drawLine(x, minY, x, maxY, paint);
      canvas.drawLine(minX, y, maxX, y, paint);
    }
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    paint.setColorFilter(cf);
  }

  @Override
  public int getOpacity() {
    return android.graphics.PixelFormat.OPAQUE;
  }

}
