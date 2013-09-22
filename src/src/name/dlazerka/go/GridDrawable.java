package name.dlazerka.go;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GridDrawable extends Drawable {
  private static final float STROKE_WIDTH = 1f;
  final int boardSize;
  final Paint paintLine;
  final Paint paintHighlighted;
  final Paint paintPrehighlighted;
  final int[][] dotsPositions;
  final float dotsRadius = 3f;
  int highlightedRow = -1;
  int highlightedCol = -1;
  boolean prehighlighted;
  Rect clipBounds;

  GridDrawable(int boardSize) {
    this.boardSize = boardSize;
    clipBounds = new Rect();
    paintLine = new Paint();
    paintLine.setColor(Color.DKGRAY);
    paintLine.setStrokeWidth(STROKE_WIDTH);
    paintLine.setStrokeCap(Paint.Cap.ROUND);
    paintPrehighlighted = new Paint(paintLine);
    paintPrehighlighted.setStrokeWidth(2*STROKE_WIDTH);
    paintHighlighted = new Paint(paintLine);
    paintHighlighted.setStrokeWidth(3*STROKE_WIDTH);

    if (boardSize == 9) {
      dotsPositions = new int[][]
          {{2, 2}, {2, 6}, {6, 2}, {6, 6}, {4, 4}};
    } else if (boardSize == 13) {
      dotsPositions = new int[][]
          {{3, 3}, {3, 9}, {9, 3}, {9, 9}};
    } else {
      dotsPositions = new int[][]
          {{3, 3}, {3, 9}, {3, 15},
          {9, 3}, {9, 9}, {9, 15},
          {15, 3}, {15, 9}, {15, 15}};
    }
  }

  public Integer getHighlightedRow() {
    return highlightedRow == -1 ? null : highlightedRow;
  }

  public int getHighlightedCol() {
    return highlightedCol == -1 ? null : highlightedCol;
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

  public void highlight() {
    prehighlighted = false;
  }

  public void prehighlight(int row, int col) {
    prehighlighted = true;
    highlightedRow = row;
    highlightedCol = col;
  }

  public void unhighlight() {
    this.highlightedRow = -1;
    this.highlightedCol = -1;
    prehighlighted = false;
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.getClipBounds(clipBounds);
    float minX = getX(0);
    float maxX = getX(boardSize - 1);
    float minY = getY(0);
    float maxY = getY(boardSize - 1);
    Paint paint;
    for (int i = 0; i < boardSize; i++) {
      float x = getX(i);
      float y = getY(i);
      paint = i != highlightedCol ? paintLine :
        prehighlighted ? paintPrehighlighted : paintHighlighted;
      canvas.drawLine(x, minY, x, maxY, paint);
      paint = i != highlightedRow ? paintLine :
        prehighlighted ? paintPrehighlighted : paintHighlighted;
      canvas.drawLine(minX, y, maxX, y, paint);
    }
    // Dots
    for (int i = 0; i < dotsPositions.length; i++) {
      float x = getX(dotsPositions[i][0]);
      float y = getY(dotsPositions[i][1]);
      canvas.drawCircle(x, y, dotsRadius, paintLine);
    }
  }

  @Override
  public void setAlpha(int alpha) {
    paintLine.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    paintLine.setColorFilter(cf);
  }

  @Override
  public int getOpacity() {
    return android.graphics.PixelFormat.OPAQUE;
  }

}
