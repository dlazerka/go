package name.dlazerka.go;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Dot implements Serializable {
  public enum Colour { CL_RED, CL_BLUE };

  public static final int NUM_DIRECTIONS = 8;
  public static final int dx[] = {-1, -1, -1,  0, 0,  1, 1, 1};
  public static final int dy[] = {-1,  0,  1, -1, 1, -1, 0, 1};
  Colour color;
  int x;
  int y;
  boolean[] neignbours;
  int label;
  boolean isSurrounded;
  Colour finalColour;

  public static int oppositeDirection(int direction) { return NUM_DIRECTIONS - 1 - direction; }
  public static Colour oppositeColor(Colour color) {
    return color == Colour.CL_BLUE ? Colour.CL_RED : Colour.CL_BLUE;
  }
  public static boolean isDirectionDiagonal(int direction) {
    return dx[direction] != 0 && dy[direction] != 0;
  }
  public static int delta2dir(int dx0, int dy0) {
    // dx, dy in {-1, 0, 1}.
    for (int i = 0; i < NUM_DIRECTIONS; ++i) if (dx[i] == dx0 && dy[i] == dy0) {
      return i;
    }
    return -1;
  }

  public Dot(Colour color, int x, int y) {
    this.color = this.finalColour = color;
    this.x = x;
    this.y = y;
    this.neignbours = new boolean[NUM_DIRECTIONS];
    this.isSurrounded = false;
  }

  public int getNx(int dir) { return x + dx[dir]; }
  public int getNy(int dir) { return y + dy[dir]; }

  public static int systemColor(Colour color) {
    switch (color) {
    case CL_RED: return Color.RED;
    case CL_BLUE: return Color.BLUE;
    }
    return 0;
  }
}
