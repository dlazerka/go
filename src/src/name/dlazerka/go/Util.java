package name.dlazerka.go;

public class Util {
  public static final String TAG = "Dots";

  /**
   * Magic function for handsome placement of captured stones.
   * Stones are placed in order:
   * 0  2  6 12
   * 1  3  7 13
   * 4  5  8 14
   * 9 10 11 15
   * @return row * 1000 + col; all 0-based.
   */
  public static int getCapturedRowCol(int i) {
    int row, col;
    int rank = 0;
    while (rank * rank <= i) rank++;
    int d = rank * rank - i;
    if (d <= rank) {// column
      row = rank - d;
      col = rank - 1;
    } else {
      row = rank - 1;
      col = rank + rank - d - 1;
    }
    return row * 1000 + col;
  }
}
