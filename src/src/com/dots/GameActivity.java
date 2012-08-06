package com.dots;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.data.match.PlayerResult;
import com.google.android.gms.games.data.match.TurnBasedMatchImpl;

public class GameActivity extends GamesApiActivity {
  private GameState mGameState;
  private TurnBasedMatchImpl mMatch;
  private GameArea mGameArea;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(GamesClient.EXTRA_TURN_BASED_MATCH)) {
      mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
    } else {
      mGameState = new GameState(this);
    }

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

    mGameArea.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        TextView scoreBlueView = (TextView) findViewById(R.id.scoreBlue);
        TextView scoreRedView = (TextView) findViewById(R.id.scoreRed);
        Integer scoreBlue = mGameState.getScore(Dot.Colour.CL_BLUE);
        Integer scoreRed = mGameState.getScore(Dot.Colour.CL_RED);
        scoreBlueView.setText(scoreBlue.toString());
        scoreRedView.setText(scoreRed.toString());
        return false;
      }
    });
  }

  private String getOpponentPlayerId() throws NotYetConnectedException {
    String currentPlayerId = getConnectedGamesClient().getCurrentPlayerId();
    ArrayList<String> playerIds = mMatch.getPlayerIds();
    String result = playerIds.get(0);
    if (result.equals(currentPlayerId)) {
      result = playerIds.get(1);
    }
    return result;
  }

  private void fillView() {
    setContentView(R.layout.activity_game);
    Button surrenderButton = (Button) findViewById(R.id.surrenderButton);
    mGameArea = (GameArea) findViewById(R.id.desk);
    mGameArea.setGameState(mGameState);

    surrenderButton.setOnClickListener(new View.OnClickListener() {
      private Toast currentToast;

      @Override
      public void onClick(View v) {
        if (mMatch == null) {
          finish();
        } else {
          try {
            String opponentPlayerId;
            opponentPlayerId = getOpponentPlayerId();
            GamesClient gamesClient = getConnectedGamesClient();
            ArrayList<PlayerResult> results = new ArrayList<PlayerResult>(2);
            results.add(new PlayerResult(gamesClient.getCurrentPlayerId(), 1,
                PlayerResult.PLACING_UNINITIALIZED));
            results.add(new PlayerResult(opponentPlayerId, 0, PlayerResult.PLACING_UNINITIALIZED));

            gamesClient.finishTurnBasedMatch(mGameState, mMatch.getMatchId(), null, results);
          } catch (NotYetConnectedException e) {
            if (currentToast == null) {
              currentToast = Toast.makeText(GameActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
            }
            currentToast.show();
          }
        }
      }
    });
  }

  @Override
  protected GamesApiListener newGamesApiListener() {
    return new GamesApiListener();// Not interested.
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
