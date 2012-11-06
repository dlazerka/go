package name.dlazerka.go.model;

public class KoRuleException extends Exception {
  final int turnsAgo;

  public KoRuleException(int turnsAgo) {
    this.turnsAgo = turnsAgo;
  }

  public int getTurnsAgo() {
    return turnsAgo;
  }
}