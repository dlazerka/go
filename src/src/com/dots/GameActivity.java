package com.dots;

import static com.dots.Util.TAG;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.data.match.PlayerResult;
import com.google.android.gms.games.data.match.TurnBasedMatchImpl;

public class GameActivity extends GamesAPIActivity {
  private GameState mGameState;
  private TurnBasedMatchImpl mMatch;
  private String mMyPlayerId;
  private GameArea mGameArea;

  private String mOpponentPlayerId;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(GamesClient.EXTRA_TURN_BASED_MATCH)) {
      mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
      mMyPlayerId = intent.getExtras().getString(MainActivity.MY_PLAYER_ID);
      Toast.makeText(this, "Match with players: " + mMatch.getPlayerIds().toString(),
          Toast.LENGTH_LONG).show();
      mGameState = new GameState(this, mGamesClient, mMatch, mMyPlayerId);
      ArrayList<String> playerIds = mMatch.getPlayerIds();
      // ArrayList<ParticipantImpl> participants = match.getParticipantList()();
      if (playerIds.get(0).equals(mMyPlayerId)) {
        mOpponentPlayerId = playerIds.get(1);
      } else {
        mOpponentPlayerId = playerIds.get(0);
      }
    } else {
      mGameState = new GameState(this, mGamesClient);
    }
  }

  private void fillView() {
    setContentView(R.layout.activity_game);
    Button surrenderButton = (Button) findViewById(R.id.surrenderButton);
    mGameArea = (GameArea) findViewById(R.id.desk);
    mGameArea.setGameState(mGameState);

    surrenderButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mMatch == null) {
          finish();
        } else {
          ArrayList<PlayerResult> results = new ArrayList<PlayerResult>(2);
          results.add(new PlayerResult(mGamesClient.getCurrentPlayerId(), 1, PlayerResult.PLACING_UNINITIALIZED));
          results.add(new PlayerResult(mOpponentPlayerId, 0, PlayerResult.PLACING_UNINITIALIZED));

          mGamesClient.finishTurnBasedMatch(mGameState, mMatch.getMatchId(), null, results);
        }
//        finish();
//        Intent intent2 = Game.this.getIntent();
//        intent2.putExtra(MainActivity.SCORE, "10");
//        Game.this.setResult(Activity.RESULT_OK, intent2);
//        finish();
      }
    });
  }

  @Override
  protected GamesAPIListener newGamesAPIListener() {
    return new MyGamesAPIListener();
  }

  private class MyGamesAPIListener extends GamesAPIListener {
    @Override
    public void onConnected() {
      super.onConnected();
      fillView();
      if (mMatch != null) {
        try {
          mGameState.deserialize(mMatch.getData());
        } catch (IOException e) {
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

}