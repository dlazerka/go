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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.dots.Dot.Colour;

public class GameArea extends GridView {
  Paint mPaint;
  GameState mGameState;
  final static int PADDING = 0;
  final static int CELL_SIZE = 44;
  final static int GRID_SIZE = 10;

  float[] mGrid;
  LayoutInflater mLi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(Color.BLACK);
    mPaint.setStrokeCap(Paint.Cap.SQUARE);
    setAdapter(new StonesAdapter());
    setNumColumns(GRID_SIZE);
    // Padding for drawing children (stones);
    setPadding(PADDING, PADDING, PADDING, PADDING);
    // 4 -- 15
    // 8 -- 6
    // 10 - 5? 4?
    int spacing = 480 / (GRID_SIZE - 1) - CELL_SIZE - 4;
    setHorizontalSpacing(spacing);
    setVerticalSpacing(spacing);
  }

  void setGameState(GameState gameState) {
    mGameState = gameState;
  }

  private float[] computeGridLines(Rect rect) {
//    int size = Math.min(rect.width(), rect.height());
    int l = rect.left + CELL_SIZE/2 + PADDING;
    int r = rect.right - CELL_SIZE/2 - PADDING;
    int t = l;
    int b = r;

    // Each line is specified with 4 coordinates.
    float[] points = new float[GRID_SIZE * 8];

    float offset;
    for (int i = 0, at = 0; i < GRID_SIZE; ++i) {
      // l + (r-l) * i/N
      // But N-1 because we want i/N to be equal to 1 at the last line.
      offset = l + (r-l) * i / ((float)GRID_SIZE - 1);
      // vertical
      points[at++] = f(offset);
      points[at++] = f(t);
      points[at++] = f(offset);
      points[at++] = f(b);
      // horizontal
      points[at++] = f(l);
      points[at++] = f(offset);
      points[at++] = f(r);
      points[at++] = f(offset);
    }
    return points;
  }

  private static float f(float t) {
    return t;
  }
  private static float f(int t) {
    return t + 0.5f;
  }

  private void drawGrid(Canvas canvas) {
    Rect rect = canvas.getClipBounds();
//    if (mGrid == null) {
      mGrid = computeGridLines(rect);
//    }

    mPaint.setColor(Color.BLACK);
    mPaint.setStrokeWidth(1.5f);
    canvas.drawLines(mGrid, mPaint);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

//    drawDotBackgrounds(canvas);
    drawGrid(canvas);
//    drawDots(canvas);
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
//      RadialGradient grad = new RadialGradient(5, 5, 10, Color.BLACK, Color.WHITE, null);
//      mPaint.setShader(grad);
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

  private void drawDotBackgroundsForColor(int color, ArrayList<Pair<Integer, Integer>> dots, Canvas canvas) {
    int savedColor = mPaint.getColor();
    try {
      // Color
      mPaint.setColor(color);
      for (Pair<Integer, Integer> d : dots) {
        float x0 = cell2Coord(d.first);
        float y0 = cell2Coord(d.second);
        canvas.drawCircle(x0, y0, 15, mPaint);
      }
    } finally {
      mPaint.setColor(savedColor);
    }
  }

  private void drawDotBackgrounds(Canvas canvas) {
    //
    ArrayList<Pair<Integer, ArrayList<Pair<Integer, Integer>>>> backgrounds =
        mGameState.getDotBackgrounds();
    for (Pair<Integer, ArrayList<Pair<Integer, Integer>>> bg : backgrounds) {
      final int color = bg.first.intValue();
      final ArrayList<Pair<Integer, Integer>> dots = bg.second;
      drawDotBackgroundsForColor(color, dots, canvas);
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


  private class StonesAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return GRID_SIZE * GRID_SIZE;
//      return 0;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      StoneView view;
      if (position % 2 == 0) {
        view = new WhiteStoneView(getContext(), CELL_SIZE);
      } else {
        view = new BlackStoneView(getContext(), CELL_SIZE);
      }
      return view;
    }

  }
}
