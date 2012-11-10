package name.dlazerka.go;

import static name.dlazerka.go.model.StoneColor.BLACK;

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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.inject.Inject;

public class GameArea extends ViewGroup {
  static final int MAX_DRAWN_CAPTURED_STONES = 16;

  /** Minimal distance between stone edge and view edge. */
  final static int PADDING = 3;

  final GridDrawable gridDrawable;
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

  /** Relevant on history browsing. */
  private boolean skipNextTouchUp;

  public GameArea(Context context, AttributeSet attrs) {
    super(context, attrs);
    RoboGuice.injectMembers(context, this);
    setKeepScreenOn(true);

    stones = new Stone[game.getTableSize()][game.getTableSize()];
    blackStoneDrawable = new StoneDrawable.Black();
    whiteStoneDrawable = new StoneDrawable.White();
    gridDrawable = new GridDrawable(game.getTableSize());

    setGameState(game.getLastState());

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
    canvas.save();
    int p = PADDING + cellSize / 2;
    canvas.clipRect(p, p, rect.width() - p, rect.height() - p);
    gridDrawable.draw(canvas);
    canvas.restore();

    drawStones(canvas);

    drawCapturedStones(canvas);

    drawWhoseTurn(canvas);
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
    int dx = PADDING + cellSize / 2;
    int dy = rect.height() + cellSize;

    // Blacks
    canvas.save();
    // Below the grid on the left.
    canvas.translate(dx, dy);
    drawCapturedStones(canvas, gameState.getBlacksCaptured(), blackStoneDrawable, false);
    canvas.restore();

    // Whites
    canvas.save();
    // Below the grid on the right.
    canvas.translate(canvas.getWidth() - dx - cellSize, dy);
    drawCapturedStones(canvas, gameState.getWhitesCaptured(), whiteStoneDrawable, true);
    canvas.restore();
  }

  private void drawCapturedStones(
      Canvas canvas, int count, StoneDrawable drawable, boolean flip) {
    if (count > MAX_DRAWN_CAPTURED_STONES) {
      count = MAX_DRAWN_CAPTURED_STONES;
    }
    for (int i = 0; i < count; i++) {
      int rowCol = Util.getCapturedRowCol(i);
      int row = (rowCol / 1000);
      int col = rowCol % 1000;

      if (flip) {
        col = -col;
      }

      canvas.save();
      int x = col * cellSize;
      int y = row * cellSize;
      canvas.translate(x, y);
      drawable.draw(canvas);
      canvas.restore();
    }
  }

  private void drawWhoseTurn(Canvas canvas) {
    // Below the grid on the left.
    int dx = 5 * cellSize + cellSize / 2 + PADDING;
    int dy = rect.height();
    canvas.save();
    StoneDrawable drawable;
    if (gameState.getWhoseTurn() == BLACK) {
      // Flip horizontally.
      canvas.translate(rect.width() - dx - 2 * cellSize, dy);
      drawable = blackStoneDrawable;
    } else {
      canvas.translate(dx, dy);
      drawable = whiteStoneDrawable;
    }
    int oldSize = drawable.getSize();
    drawable.setSize(2 * cellSize);
    drawable.draw(canvas);
    drawable.setSize(oldSize);
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
  }

  void skipNextTouchUp() {
    this.skipNextTouchUp = true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int row = getRow(event.getY());
    int col = getCol(event.getX());
    gridDrawable.unhighlight();
    invalidate();
    if (row < 0 || row >= game.getTableSize() || col < 0 || col >= game.getTableSize()) {
      return true;
    }
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        gridDrawable.highlight(row, col);
        return true;
      case MotionEvent.ACTION_UP:
        if (skipNextTouchUp) {
          skipNextTouchUp = false;
          return true;
        }
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
    return true;
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
