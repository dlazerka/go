package name.dlazerka.go;

import static name.dlazerka.go.model.StoneColor.*;

import java.util.HashSet;
import java.util.Set;

import name.dlazerka.go.model.Game;
import name.dlazerka.go.model.GameState;
import name.dlazerka.go.model.KoRuleException;
import name.dlazerka.go.model.NoLibertiesException;
import name.dlazerka.go.model.SpaceTakenException;
import name.dlazerka.go.model.Stone;
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
  final Paint paintGrid = new Paint();
  /** Layout area. Mutable. */
  final Rect rect = new Rect();
  /** Row-major. */
  final Stone[][] stones;
  StoneDrawable blackStoneDrawable;
  StoneDrawable whiteStoneDrawable;

  @Inject
  Game game;

  GameState gameState;

  int cellSize;
  float[] grid;

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);
    RoboGuice.injectMembers(context, this);
    setKeepScreenOn(true);

    stones = new Stone[game.getTableSize()][game.getTableSize()];
    blackStoneDrawable = new StoneDrawable.Black();
    whiteStoneDrawable = new StoneDrawable.White();

    setGameState(game.getLastState());

    paintGrid.setColor(Color.DKGRAY);
    paintGrid.setStrokeWidth(2f);
    paintGrid.setStrokeCap(Paint.Cap.ROUND);

    game.addListener(new GameListener());
  }

  void setGameState(GameState state) {
    if (gameState != null) {
      Set<Stone> prevStones = gameState.getStones();
      Set<Stone> newStones = state.getStones();
      Set<Stone> prevStonesM = new HashSet<Stone>(prevStones);
      Set<Stone> newStonesM = new HashSet<Stone>(newStones);

      prevStonesM.removeAll(newStones);
      for (Stone stone : prevStonesM) {
        stones[stone.getRow()][stone.getCol()] = null;
      }

      newStonesM.removeAll(prevStones);
      for (Stone stone : newStonesM) {
        stones[stone.getRow()][stone.getCol()] = stone;
      }
      invalidate();
    }
    gameState = state;
  }

  /** @return Position on the canvas for given row (zero-based) */
  private int getX(int row) {
    int l = rect.left + cellSize / 2 + PADDING;
    int r = rect.right - cellSize / 2 - PADDING;
    return getCoord(row, l, r);
  }

  /** @return Position on canvas for given row (zero-based) */
  private int getY(int col) {
    int t = rect.top + cellSize / 2 + PADDING;
    int b = rect.bottom - cellSize / 2 - PADDING;
    return getCoord(col, t, b);
  }

  private int getCoord(int i, int min, int max) {
    // l + (r-l) * i/N
    // But N-1 because we want i/N to be equal to 1 at the last line.
    return min + (max - min) * i / (game.getTableSize() - 1);
  }

  /** @return Row (zero-based) for given position on the canvas */
  private int getRow(float y) {
    return Math.round(0.5f + (y - PADDING) / (rect.bottom - rect.top - 2*PADDING) * game.getTableSize()) - 1;
  }

  /** @return Column (zero-based) for given position on the canvas */
  private int getCol(float x) {
    return Math.round(0.5f + (x - PADDING) / (rect.right - rect.left - 2*PADDING) * game.getTableSize()) - 1;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    // Grid
    canvas.drawLines(grid, paintGrid);

    drawStones(canvas);

    drawCapturedStones(canvas);
  }

  private void drawStones(Canvas canvas) {
    for (int i = 0; i < stones.length; i++) {
      for (int j = 0; j < stones[i].length; j++) {
        Stone stone = stones[i][j];
        if (stone == null) continue;
        int x = getX(stone.getCol());
        int y = getY(stone.getRow());
        canvas.save();
        canvas.translate(x - cellSize / 2, y - cellSize / 2);
        if (stone.getColor() == BLACK) {
          blackStoneDrawable.draw(canvas);
        } else {
          whiteStoneDrawable.draw(canvas);
        }
        canvas.restore();
      }
    }
  }

  private void drawCapturedStones(Canvas canvas) {
    canvas.save();
    canvas.translate(PADDING + cellSize / 2, rect.height() + cellSize);
    int b = gameState.getBlacksCaptured();
    for (int i = 0; i < b; i++) {
      int rowCol = Util.getCapturedRowCol(i);
      int row = (rowCol / 1000);
      int col = rowCol % 1000;

      canvas.save();
      canvas.translate(
          col * cellSize,
          row * cellSize);
      blackStoneDrawable.draw(canvas);
      canvas.restore();
    }
    canvas.restore();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (!changed) return;
    rect.left = l;
    rect.top = t;
    rect.right = Math.min(r, b);
    rect.bottom = Math.min(r, b);

    cellSize = (rect.width() - 2 * PADDING) / game.getTableSize();
    blackStoneDrawable.setSize(cellSize);
    whiteStoneDrawable.setSize(cellSize);

    if (grid == null) {
      grid = new float[game.getTableSize() * 8];

      float minX = getX(0);
      float maxX = getX(game.getTableSize() - 1);
      float minY = getY(0);
      float maxY = getY(game.getTableSize() - 1);
      for (int i = 0, at = 0; i < game.getTableSize(); ++i) {
        float x = getX(i);
        float y = getY(i);
        // vertical line
        grid[at++] = x;
        grid[at++] = minY;
        grid[at++] = x;
        grid[at++] = maxY;
        // horizontal line
        grid[at++] = minX;
        grid[at++] = y;
        grid[at++] = maxX;
        grid[at++] = y;
      }
    }

  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        int row = getRow(event.getY());
        int col = getCol(event.getX());
        if (row >= 0 && row < game.getTableSize() &&
            col >= 0 && col < game.getTableSize()) {
          try {
            game.makeTurnAt(row, col);
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
      for (int row = 0; row < game.getTableSize(); row++) {
        for (int col = 0; col < game.getTableSize(); col++) {
          stones[row][col] = null;
        }
      }
      removeAllViews();
    }
  }
}
