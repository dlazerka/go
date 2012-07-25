package com.dots;

import com.google.android.gms.common.ConnectionStatus;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.games.GamesClient;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * Application ID: 2149793076813558136
 */
public class MainActivity extends Activity {

  private GamesClient mGamesClient;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    String appId = getString(R.string.app_id);
    mGamesClient = new GamesClient(
        this,
        appId,
        new GamesAPIConnectionCallbacks(),
        new GamesAPIOnConnectionFailedListener()
    );
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  private final class GamesAPIOnConnectionFailedListener implements OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(ConnectionStatus arg0) {
    }
  }

  private final class GamesAPIConnectionCallbacks implements ConnectionCallbacks {
    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
    }
  }
}
