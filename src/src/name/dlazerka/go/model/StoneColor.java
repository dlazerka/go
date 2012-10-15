package name.dlazerka.go.model;

public enum StoneColor {
    BLACK, WHITE;

    public StoneColor other() {
      return this == BLACK ? WHITE : BLACK;
    }
}
