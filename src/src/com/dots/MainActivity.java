package com.dots;

import static com.dots.Util.TAG;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClient.TurnBasedMatchListener;
import com.google.android.gms.games.data.match.Match;
import com.google.android.gms.games.data.match.PlayerResult;
import com.google.android.gms.games.data.match.TurnBasedMatchImpl;

public class MainActivity extends GamesAPIActivity implements TurnBasedMatchListener {
  // If true, shows Play Alone button, if false proceeds to Invite Friends immediately.
  public static final boolean DEVMODE = true;


  private static final int REQUEST_CODE_CREATE_MATCH = 9001;
  public static final int REQUEST_SELECT_PLAYERS = 9002;
  public static final int REQUEST_GAME = 9003;

  public static final String MY_PLAYER_ID = "MyPlayerID";
  public static final String SCORE = "Score";
  private String mOpponentPlayerId;
  private TurnBasedMatchImpl mMatch;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    Intent intent = getIntent();
    // Check if the activity was launched by invitation to a match.
    if (intent != null && intent.hasExtra(GamesClient.EXTRA_TURN_BASED_MATCH)) {
      Log.i(TAG, "Match found, showing GameActivity");
      TurnBasedMatchImpl match = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
      int status2 = match.getStatus();
      Log.i(TAG, "Match found, showing GameActivity" + status2 + " vs " + Match.MATCH_STATUS_ACTIVE);
      if (status2 == Match.MATCH_STATUS_ACTIVE) {
        Log.i(TAG, "Match found, showing GameActivity");
        mMatch = match;
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!mGamesClient.isConnected()) {
      setContentView(R.layout.activity_connecting);
    }
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
      case REQUEST_CODE_CREATE_MATCH:
        if (resultCode == Activity.RESULT_OK) {
          mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
          startMatch();
        }
        break;
      case REQUEST_GAME:
//        if (resultCode == Activity.RESULT_OK) {
          Bundle extras = intent.getExtras();
          String score = extras.getString(SCORE);
          TurnBasedMatchImpl match = extras.getParcelable(GamesClient.EXTRA_TURN_BASED_MATCH);
          ArrayList<PlayerResult> results = new ArrayList<PlayerResult>(2);
          results.add(new PlayerResult(mGamesClient.getCurrentPlayerId(), 1, PlayerResult.PLACING_UNINITIALIZED));
          results.add(new PlayerResult(mOpponentPlayerId, 0, PlayerResult.PLACING_UNINITIALIZED));
          mGamesClient.finishTurnBasedMatch(this, match.getMatchId(), new byte[] {}, results);
          Toast.makeText(this, "Your score is " + score, Toast.LENGTH_LONG).show();
//          mGamesClient.finishRealTimeMatch(mMatch, arg1, arg2)
//        }
    }
  }

  private void showButtons() {
    setContentView(R.layout.activity_main);

    View playAloneView = findViewById(R.id.playAlone);
    playAloneView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
         Intent intent = new Intent(MainActivity.this, GameActivity.class);
         startActivityForResult(intent, REQUEST_GAME);
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
    Intent intent = new Intent(MainActivity.this, GameActivity.class);
    intent.putExtra(GamesClient.EXTRA_TURN_BASED_MATCH, mMatch);
    intent.putExtra(MY_PLAYER_ID, mGamesClient.getCurrentPlayerId());
    startActivity(intent);
  }

  @Override
  protected GamesAPIListener newGamesAPIListener() {
    return new MyGamesAPIListener();
  }

  private class MyGamesAPIListener extends GamesAPIListener {
    @Override
    public void onConnected() {
      super.onConnected();
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
