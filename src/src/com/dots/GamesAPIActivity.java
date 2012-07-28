package com.dots;

import static com.dots.Util.TAG;

import com.google.android.gms.common.ConnectionStatus;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.games.GamesClient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

public abstract class GamesAPIActivity extends Activity {
  protected GamesClient mGamesClient;

  private static final int REQUEST_RECONNECT_GAMES_API = 9000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String appId = getString(R.string.app_id);
    GamesAPIListener gamesAPIListener = newGamesAPIListener();
    mGamesClient = new GamesClient(
        this,
        appId,
        gamesAPIListener,
        gamesAPIListener);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGamesClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mGamesClient.disconnect();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    switch (requestCode) {
      case REQUEST_RECONNECT_GAMES_API:
        if (resultCode == Activity.RESULT_OK) {
          // We resolved ConnectionStatus error successfully.
          // Try to connect again.
          mGamesClient.connect();
        }
        break;
    }
  }

  protected abstract GamesAPIListener newGamesAPIListener();

  protected abstract class GamesAPIListener
      implements ConnectionCallbacks, OnConnectionFailedListener {

    @Override
    public void onConnected() {
      Log.d(TAG, "Connected to Games API");
    }

    @Override
    public void onDisconnected() {
      Log.d(TAG, "Disconnected from Games API");
    }

    @Override
    public void onConnectionFailed(ConnectionStatus status) {
      int errorCode = status.getErrorCode();
      if (status.hasResolution()) {
        try {
          // This usually happen when user needs to authenticate into Games API.
          status.startResolutionForResult(GamesAPIActivity.this, REQUEST_RECONNECT_GAMES_API);
        } catch (SendIntentException e) {
          Log.e(TAG, "Unable to recover from a connection failure: " + errorCode + ".");
          finish();
        }
      } else {
        Log.e(TAG, "Did you install GmsCore.apk?");
        finish();
      }
    }
  }
}
