package name.dlazerka.go;

import java.util.HashSet;
import java.util.Set;

import name.dlazerka.go.model.Game;
import name.dlazerka.go.model.GameState;
import name.dlazerka.go.model.KoRuleException;
import name.dlazerka.go.model.NoLibertiesException;
import name.dlazerka.go.model.SpaceTakenException;
import name.dlazerka.go.model.Stone;
import static name.dlazerka.go.model.StoneColor.*;
import roboguice.RoboGuice;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.inject.Inject;

public class GameArea extends ViewGroup {
  /** Minimal distance between stone edge and view edge. */
  final static int PADDING = 3;

  /** For painting grid. */
  final Paint mPaintGrid = new Paint();
  /** Layout area. Mutable. */
  final Rect mRect = new Rect();
  /** Row-major. */
  final StoneView[][] stoneViews;

  @Inject
  Game mGame;

  GameState mGameState;

  int mCellSize;
  float[] mGrid;

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);
    RoboGuice.injectMembers(context, this);
    setKeepScreenOn(true);

    stoneViews = new StoneView[mGame.getTableSize()][mGame.getTableSize()];
    setGameState(mGame.getLastState());

    mPaintGrid.setColor(Color.DKGRAY);
    mPaintGrid.setStrokeWidth(2f);
    mPaintGrid.setStrokeCap(Paint.Cap.ROUND);

    mGame.addListener(new GameListener());
  }

  void setGameState(GameState state) {
    if (mGameState != null) {
      Set<Stone> prevStones = mGameState.getStones();
      Set<Stone> newStones = state.getStones();
      Set<Stone> prevStonesM = new HashSet<Stone>(prevStones);
      Set<Stone> newStonesM = new HashSet<Stone>(newStones);

      prevStonesM.removeAll(newStones);
      for (Stone stone : prevStonesM) {
        removeStoneView(stone);
      }

      newStonesM.removeAll(prevStones);
      for (Stone stone : newStonesM) {
        addStoneView(stone);
      }
    }
    mGameState = state;
  }

  private void addStoneView(Stone stone) {
    StoneView stoneView = new StoneView(stone, mCellSize);
    stoneViews[stone.getRow()][stone.getCol()] = stoneView;
    invalidate();
  }

  private void removeStoneView(Stone stone) {
    stoneViews[stone.getRow()][stone.getCol()] = null;
    invalidate();
  }

  void removeAllStoneViews() {
    removeAllViews();
    for (int i = 0; i < stoneViews.length; i++) {
      for (int j = 0; j < stoneViews[i].length; j++) {
        stoneViews[i][j] = null;
      }
    }
  }

  /** @return Position on the canvas for given row (zero-based) */
  private int getX(int row) {
    int l = mRect.left + mCellSize / 2 + PADDING;
    int r = mRect.right - mCellSize / 2 - PADDING;
    return getCoord(row, l, r);
  }

  /** @return Position on canvas for given row (zero-based) */
  private int getY(int col) {
    int t = mRect.top + mCellSize / 2 + PADDING;
    int b = mRect.bottom - mCellSize / 2 - PADDING;
    return getCoord(col, t, b);
  }

  private int getCoord(int i, int min, int max) {
    // l + (r-l) * i/N
    // But N-1 because we want i/N to be equal to 1 at the last line.
    return min + (max - min) * i / (mGame.getTableSize() - 1);
  }

  /** @return Row (zero-based) for given position on the canvas */
  private int getRow(float y) {
    return Math.round(0.5f + (y - PADDING) / (mRect.bottom - mRect.top - 2*PADDING) * mGame.getTableSize()) - 1;
  }

  /** @return Column (zero-based) for given position on the canvas */
  private int getCol(float x) {
    return Math.round(0.5f + (x - PADDING) / (mRect.right - mRect.left - 2*PADDING) * mGame.getTableSize()) - 1;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    // Grid
    canvas.drawLines(mGrid, mPaintGrid);

    // Stones
    for (int i = 0; i < stoneViews.length; i++) {
      for (int j = 0; j < stoneViews[i].length; j++) {
        StoneView stoneView = stoneViews[i][j];
        if (stoneView == null) continue;
        int x = getX(stoneView.stone.getCol());
        int y = getY(stoneView.stone.getRow());
        canvas.save();
        canvas.translate(x - mCellSize / 2, y - mCellSize / 2);
        stoneView.draw(canvas);
        canvas.restore();
      }
    }

    // Captured stones
    canvas.save();
    canvas.translate(PADDING + mCellSize / 2, mRect.height() + mCellSize);
    int b = mGameState.getBlacksCaptured();
    for (int i = 0; i < b; i++) {
      int rowCol = Util.getCapturedRowCol(i);
      int row = (rowCol / 1000);
      int col = rowCol % 1000;
      Stone stone = new Stone(row, col, BLACK);
      StoneView stoneView = new StoneView(stone, mCellSize);

      canvas.save();
      canvas.translate(
          stone.getCol() * mCellSize,
          stone.getRow() * mCellSize);
      stoneView.draw(canvas);
      canvas.restore();
    }
    canvas.restore();

  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (!changed) return;
    mRect.left = l;
    mRect.top = t;
    mRect.right = Math.min(r, b);
    mRect.bottom = Math.min(r, b);

    mCellSize = (mRect.width() - 2*PADDING) / mGame.getTableSize();

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

  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        int row = getRow(event.getY());
        int col = getCol(event.getX());
        if (row >= 0 && row < mGame.getTableSize() &&
            col >= 0 && col < mGame.getTableSize()) {
          try {
            mGame.makeTurnAt(row, col);
          } catch (SpaceTakenException e) {
            // Toast.makeText(getContext(), "This space is taken",
            // Toast.LENGTH_SHORT).show();
          } catch (NoLibertiesException e) {
            Toast.makeText(getContext(), "No liberty", Toast.LENGTH_SHORT).show();
          } catch (KoRuleException e) {
            String msg = "Ko rule violation";
            if (e.getTurnsAgo() > 2) {
              msg = "Super ko rule violation " + e.getTurnsAgo() + " turns ago";
            }
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
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

  private class GameListener implements name.dlazerka.go.model.GameListener {

    @Override
    public void onStateAdvanced(GameState newState) {
      setGameState(newState);
    }

    @Override
    public void onGameReset() {
      for (int row = 0; row < mGame.getTableSize(); row++) {
        for (int col = 0; col < mGame.getTableSize(); col++) {
          stoneViews[row][col] = null;
        }
      }
      removeAllViews();
    }
  }
}
