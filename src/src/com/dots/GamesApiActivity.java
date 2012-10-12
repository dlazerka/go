package com.dots;

import static com.dots.Util.TAG;

import java.util.ArrayList;

import roboguice.activity.RoboActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.dots.GamesApiAware.NotYetConnectedException;
import com.google.android.gms.common.ConnectionStatus;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.games.GamesClient;

public abstract class GamesApiActivity extends RoboActivity implements OnConnectionFailedListener,
    GamesApiAware {
  private static final int REQUEST_CODE_RECONNECT = 9000;
  private GamesClient mGamesClient;

  protected abstract GamesApiListener newGamesApiListener();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mGamesClient = new GamesClient(
        this,
        getString(R.string.app_id),
        new GamesApiListener(),
        this);
  }

  @Override
  protected void onStart() {
    super.onStart();
//    mGamesClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mGamesClient.disconnect();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_CODE_RECONNECT:
        if (resultCode == Activity.RESULT_OK) {
          // We resolved ConnectionStatus error successfully.
          // Try to connect again.
          mGamesClient.connect();
        }
        break;
    }
  }

  @Override
  public void onConnectionFailed(ConnectionStatus status) {
    int errorCode = status.getErrorCode();
    if (status.hasResolution()) {
      try {
        // This usually happen when user needs to authenticate into Games API.
        status.startResolutionForResult(this, REQUEST_CODE_RECONNECT);
      } catch (SendIntentException e) {
        Log.e(TAG, "Unable to recover from a connection failure: " + errorCode + ".");
        this.finish();
      }
    } else {
      Log.e(TAG, "Did you install GmsCore.apk?");
      this.finish();
    }
  }

  GamesClient getGamesClient() {
    return mGamesClient;
  }

  @Override
  public GamesClient getConnectedGamesClient() throws NotYetConnectedException {
    if (mGamesClient.isConnected()) {
      return mGamesClient;
    } else {
      throw new NotYetConnectedException();
    }
  }

}
