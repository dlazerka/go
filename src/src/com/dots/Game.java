package com.dots;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.data.match.TurnBasedMatchImpl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Game extends Activity {

  private GameState mGameState;
  private TurnBasedMatchImpl mMatch;

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);
      //getActionBar().setDisplayHomeAsUpEnabled(true);

      Intent intent = getIntent();
      if (intent != null && intent.hasExtra(GamesClient.EXTRA_TURN_BASED_MATCH)) {
        mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);

        Toast.makeText(
            this,
            "Match with players: " + mMatch.getPlayerIds().toString(),
            Toast.LENGTH_LONG).show();
      }

      LinearLayout container = (LinearLayout) findViewById(R.id.game_area);
      Button eraseButton = (Button)findViewById(R.id.eraseButton);

      mGameState = new GameState();
      final GameArea gameArea = new GameArea(this, mGameState);


      gameArea.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
      container.addView(gameArea);

      eraseButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          gameArea.erase();
        }
      });
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
