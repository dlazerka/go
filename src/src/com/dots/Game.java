package com.dots;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class Game extends Activity {
  
  private GameState mGameState;

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_game);
      //getActionBar().setDisplayHomeAsUpEnabled(true);
      
      LinearLayout container = (LinearLayout) findViewById(R.id.game_area);
      Button eraseButton = (Button)findViewById(R.id.eraseButton);

      mGameState = new GameState();
      final GameArea gameArea = new GameArea(this, mGameState);
      

      gameArea.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
      container.addView(gameArea);
      
      eraseButton.setOnClickListener(new View.OnClickListener() {
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
