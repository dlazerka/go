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

public class Game extends GamesAPIActivity {
  public GamesClient mGamesClient;
  private GameState mGameState;
  private TurnBasedMatchImpl mMatch;
  private String mMyPlayerId;
  private GameArea mGameArea;

  private String mOpponentPlayerId;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    // getActionBar().setDisplayHomeAsUpEnabled(true);

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

    LinearLayout container = (LinearLayout) findViewById(R.id.game_area);
    Button eraseButton = (Button) findViewById(R.id.eraseButton);
    Button surrenderButton = (Button) findViewById(R.id.surrenderButton);

    mGameArea = new GameArea(this, mGameState);
    container.addView(mGameArea);
    mGameArea.setVisibility(View.INVISIBLE);

    mGameArea.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT));

    eraseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mGameArea.erase();
      }
    });

    surrenderButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ArrayList<PlayerResult> results = new ArrayList<PlayerResult>(2);
        results.add(new PlayerResult(mGamesClient.getCurrentPlayerId(), 1, PlayerResult.PLACING_UNINITIALIZED));
        results.add(new PlayerResult(mOpponentPlayerId, 0, PlayerResult.PLACING_UNINITIALIZED));

        mGamesClient.finishTurnBasedMatch(mGameState, mMatch.getMatchId(), null, results);
//        finish();
//        Intent intent2 = Game.this.getIntent();
//        intent2.putExtra(MainActivity.SCORE, "10");
//        Game.this.setResult(Activity.RESULT_OK, intent2);
//        finish();
      }
    });

    if (!MainActivity.DEVMODE) {
      eraseButton.setVisibility(View.GONE);
    }
  }

  @Override
  protected GamesAPIListener newGamesAPIListener() {
    return new MyGamesAPIListener();
  }

  private class MyGamesAPIListener extends GamesAPIListener {
    @Override
    public void onConnected() {
      Log.d(TAG, "Connected to Games API");
      mGameArea.setVisibility(View.VISIBLE);
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
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_game, menu);
    return true;
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
