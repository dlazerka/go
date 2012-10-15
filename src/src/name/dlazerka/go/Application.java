package name.dlazerka.go;

import com.google.android.gms.games.GamesClient;

public class Application extends android.app.Application {
  private GamesClient mGamesClient;

  public GamesClient getGamesClient() {
    return mGamesClient;
  }

  public void setGamesClient(GamesClient mGamesClient) {
    this.mGamesClient = mGamesClient;
  }
}
