package com.dots;

import com.google.android.gms.games.GamesClient;

public interface GamesApiAware {
  public GamesClient getConnectedGamesClient() throws NotYetConnectedException;

  public class NotYetConnectedException extends Exception {
    public NotYetConnectedException() {
      super("Connecting to Games API");
    }
  }
}
