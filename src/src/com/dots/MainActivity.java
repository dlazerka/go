package com.dots;

import com.dots.R;
import com.google.android.gms.common.ConnectionStatus;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.games.GamesClient;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

  public static final String TAG = "Dots";
  private static final int REQUEST_CODE_RESOLVE_ERR = 0;
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

    View playView = findViewById(R.id.play);
    playView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, Game.class);
        startActivityForResult(intent, 0);
      }
    });
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
          case REQUEST_CODE_RESOLVE_ERR:
              if (resultCode == Activity.RESULT_OK) {
                  // We resolved ConnectionStatus error successfully. Try to connect again.
                  mGamesClient.connect();
              }
              break;
      }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  private final class GamesAPIOnConnectionFailedListener implements OnConnectionFailedListener {
    @Override
    public void onConnectionFailed(ConnectionStatus status) {
      int errorCode = status.getErrorCode();
      if (status.hasResolution()) {
          try {
              status.startResolutionForResult(MainActivity.this, errorCode);
          } catch (SendIntentException e) {
              Log.e(TAG, "Unable to recover from a connection failure.");
              finish();
          }
      } else {
          Log.e(TAG, "Unable to recover from a connection error. Did you install GmsCore.apk?");
          finish();
      }
    }
  }

  private final class GamesAPIConnectionCallbacks implements ConnectionCallbacks {
    @Override
    public void onConnected() {
      Log.d(TAG, "connected");
      String playerId = mGamesClient.getCurrentPlayerId();
      Toast.makeText(MainActivity.this, playerId + " is connected.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected() {
      Log.d(TAG, "disconnected");
    }
  }
}
