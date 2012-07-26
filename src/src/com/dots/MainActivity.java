package com.dots;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionStatus;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClient.TurnBasedMatchListener;
import com.google.android.gms.games.data.match.Match;
import com.google.android.gms.games.data.match.TurnBasedMatchImpl;

public class MainActivity extends Activity implements TurnBasedMatchListener {
  // If true, shows Play Alone button, if false proceeds to Invite Friends immediately.
  public static final boolean DEVMODE = true;

  public static final String TAG = "Dots";

  private static final int REQUEST_RECONNECT_GAMES_API = 9000;
  private static final int REQUEST_CODE_CREATE_MATCH = 9001;
  public static final int REQUEST_SELECT_PLAYERS = 9002;

  public static final String MY_PLAYER_ID = "MyPlayerID";
  private String mOpponentPlayerId;
  public static GamesClient mGamesClient;
  private TurnBasedMatchImpl mMatch;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String appId = getString(R.string.app_id);
    GamesAPIConnectionListener gamesAPIConnectionListener = new GamesAPIConnectionListener();
    mGamesClient = new GamesClient(this, appId, gamesAPIConnectionListener,
        gamesAPIConnectionListener);

    Intent intent = getIntent();
    // Check if the activity was launched by invitation to a match.
    if (intent != null && intent.hasExtra(GamesClient.EXTRA_TURN_BASED_MATCH)) {
      Log.i(TAG, "Match found, showing GameActivity");
      mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!mGamesClient.isConnected()) {
      setContentView(R.layout.activity_connecting);
      mGamesClient.connect();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mGamesClient.disconnect();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    switch (requestCode) {
      case REQUEST_SELECT_PLAYERS:
        if (resultCode == Activity.RESULT_OK) {
          ArrayList<String> players = intent.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
          mOpponentPlayerId = players.get(0);

          mGamesClient.createTurnBasedMatch(this, Match.INVITE_TYPE_INVITE_ALL_NOW,
              Match.MATCH_VARIANT_ANY, mOpponentPlayerId);
        }
        break;
      case REQUEST_RECONNECT_GAMES_API:
        if (resultCode == Activity.RESULT_OK) {
          // We resolved ConnectionStatus error successfully.
          // Try to connect again.
          mGamesClient.connect();
        }
        break;
      case REQUEST_CODE_CREATE_MATCH:
        if (resultCode == Activity.RESULT_OK) {
          mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
          startMatch();
        }
        break;
    }
  }

  private void showButtons() {
    setContentView(R.layout.activity_main);

    View playView = findViewById(R.id.playAlone);
    playView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
         Intent intent = new Intent(MainActivity.this, Game.class);
         startActivity(intent);
      }
    });

    View inviteFriendsView = findViewById(R.id.inviteFriends);
    inviteFriendsView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        goToInviteFriends();
      }
    });
  }

  private void startMatch() {
    Intent intent = new Intent(MainActivity.this, Game.class);
    intent.putExtra(GamesClient.EXTRA_TURN_BASED_MATCH, mMatch);
    intent.putExtra(MY_PLAYER_ID, mGamesClient.getCurrentPlayerId());
    startActivity(intent);
  }

  private final class GamesAPIConnectionListener implements ConnectionCallbacks,
      OnConnectionFailedListener {
    @Override
    public void onConnected() {
      Log.d(TAG, "Connected to Games API");
      if (mMatch == null) {
        if (DEVMODE) {
          showButtons();
        } else {
          goToInviteFriends();
        }
      } else {
        startMatch();
      }
    }

    @Override
    public void onDisconnected() {
      Log.d(TAG, "disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionStatus status) {
      int errorCode = status.getErrorCode();
      if (status.hasResolution()) {
        try {
          status.startResolutionForResult(MainActivity.this, REQUEST_RECONNECT_GAMES_API);
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

  @Override
  public void onTurnBasedMatchLoaded(TurnBasedMatchImpl match) {
    if (match == null) {
      Toast.makeText(this, "Loaded match is null", Toast.LENGTH_LONG).show();
      return;
    }
    switch (match.getStatus()) {
      case Match.MATCH_STATUS_ACTIVE:
        Log.i(TAG, "MATCH_STATUS_ACTIVE");
        mMatch = match;
        startMatch();
        break;
      case Match.MATCH_STATUS_AUTO_MATCHING:
        Log.i(TAG, "MATCH_STATUS_AUTO_MATCHING");
        break;
      case Match.MATCH_STATUS_COMPLETE:
        Log.i(TAG, "MATCH_STATUS_COMPLETE");
        break;
      case Match.MATCH_STATUS_CONNECTING:
        Log.i(TAG, "MATCH_STATUS_CONNECTING");
        break;
      case Match.MATCH_STATUS_INVITING:
        Log.i(TAG, "MATCH_STATUS_INVITING");
        break;
    }
  }

  private void goToInviteFriends() {
    Intent selectPlayersIntent = mGamesClient.getSelectPlayersIntent(1, 1);
    startActivityForResult(selectPlayersIntent, REQUEST_SELECT_PLAYERS);
  }

}
