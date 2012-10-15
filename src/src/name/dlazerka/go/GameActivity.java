package name.dlazerka.go;

import name.dlazerka.go.model.Game;
import name.dlazerka.go.model.GameListener;
import name.dlazerka.go.model.GameState;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import name.dlazerka.go.R;
import com.google.inject.Inject;

@ContentView(R.layout.activity_game)
public class GameActivity extends GamesApiActivity {
  private static final String CONNECTING_MESSAGE = "Connecting to Games API";
  // private GameState mGameState;
  // private TurnBasedMatchImpl mMatch;
  private Toast currentToast;

  @InjectView(R.id.gameArea)
  private GameArea mGameArea;
  @InjectView(R.id.pass)
  Button mPassButton;
  @InjectView(R.id.back)
  Button mBackButton;
  @InjectView(R.id.forward)
  Button mForwardButton;
  @Inject
  Game mGame;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mGame.addListener(new GameListener());

    Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Shojumaru-Regular.ttf");
    mPassButton.setTypeface(font);
    mBackButton.setTypeface(font);
    mForwardButton.setTypeface(font);
    mForwardButton.setVisibility(View.INVISIBLE);
    mPassButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mGame.passTurn();
        Toast.makeText(GameActivity.this, "Passed", Toast.LENGTH_SHORT).show();
      }
    });
    mBackButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int turnNo = mGameArea.mGameState.getTurnNo();
        if (turnNo == 0) {
          // Shouldn't be possible.
          Ln.e("Attempt to go back before first turn");
          return;
        }
        mForwardButton.setVisibility(View.VISIBLE);
        if (turnNo - 1 == 0) {
          mBackButton.setVisibility(View.INVISIBLE);
        }

        GameState prevState = mGame.getStateAt(turnNo - 1);
        mGameArea.setGameState(prevState);
      }
    });
    mForwardButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int turnNo = mGameArea.mGameState.getTurnNo();
        GameState lastState = mGame.getLastState();
        if (turnNo == lastState.getTurnNo()) {
          // Shouldn't be possible.
          Ln.e("Attempt to go forward after last turn");
          return;
        }
        mBackButton.setVisibility(View.VISIBLE);
        if (turnNo + 1 == lastState.getTurnNo()) {
          mForwardButton.setVisibility(View.INVISIBLE);
        }

        GameState prevState = mGame.getStateAt(turnNo + 1);
        mGameArea.setGameState(prevState);
      }
    });

    // Intent intent = getIntent();
    // if (intent != null &&
    // intent.hasExtra(GamesClient.EXTRA_TURN_BASED_MATCH)) {
    // mMatch = intent.getParcelableExtra(GamesClient.EXTRA_TURN_BASED_MATCH);
    // } else {
    // mGameState = new GameState(this);
    // }
    //
    // fillView();
    //
    // if (mMatch != null) {
    // try {
    // mGameState.deserialize(mMatch.getData());
    // } catch (IOException e) {
    // e.printStackTrace();
    // } catch (ClassNotFoundException e) {
    // e.printStackTrace();
    // }
    // }
    //
    // mGameArea.setOnTouchListener(new OnTouchListener() {
    // @Override
    // public boolean onTouch(View v, MotionEvent event) {
    // if (!getGamesClient().isConnected()) {
    // showToastConnecting();
    // return true;
    // }
    // TextView scoreBlueView = (TextView) findViewById(R.id.scoreBlue);
    // TextView scoreRedView = (TextView) findViewById(R.id.scoreRed);
    // Integer scoreBlue = mGameState.getScore(Dot.Colour.CL_BLUE);
    // Integer scoreRed = mGameState.getScore(Dot.Colour.CL_RED);
    // scoreBlueView.setText(scoreBlue.toString());
    // scoreRedView.setText(scoreRed.toString());
    // return false;
    // }
    // });
  }

  // private String getOpponentPlayerId() throws NotYetConnectedException {
  // String currentPlayerId = getConnectedGamesClient().getCurrentPlayerId();
  // ArrayList<String> playerIds = mMatch.getPlayerIds();
  // String result = playerIds.get(0);
  // if (result.equals(currentPlayerId)) {
  // result = playerIds.get(1);
  // }
  // return result;
  // }

  // private void fillView() {
  // setContentView(R.layout.activity_game);
  // mGameArea.setGameState(mGameState);
  //
  // mPassButton.setOnClickListener(new View.OnClickListener() {
  // @Override
  // public void onClick(View v) {
  // try {
  // String opponentPlayerId;
  // opponentPlayerId = getOpponentPlayerId();
  // GamesClient gamesClient = getConnectedGamesClient();
  // ArrayList<PlayerResult> results = new ArrayList<PlayerResult>(2);
  // results.add(new PlayerResult(gamesClient.getCurrentPlayerId(), 1,
  // PlayerResult.PLACING_UNINITIALIZED));
  // results.add(new PlayerResult(opponentPlayerId, 0,
  // PlayerResult.PLACING_UNINITIALIZED));
  //
  // gamesClient.finishTurnBasedMatch(mGameState, mMatch.getMatchId(), null,
  // results);
  // } catch (NotYetConnectedException e) {
  // showToastConnecting();
  // }
  // }
  //
  // });
  // }

  void showToastConnecting() {
    if (currentToast == null) {
      currentToast = Toast.makeText(GameActivity.this, CONNECTING_MESSAGE, Toast.LENGTH_SHORT);
    }
    // To not schedule many toasts.
    if (!currentToast.getView().isShown()) {
      currentToast.show();
    }
  }

  @Override
  protected GamesApiListener newGamesApiListener() {
    return new GamesApiListener();// Not interested.
  }
//
//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//    switch (item.getItemId()) {
//      case android.R.id.home:
//        NavUtils.navigateUpFromSameTask(this);
//        return true;
//    }
//    return super.onOptionsItemSelected(item);
//  }

  private class GameListener implements name.dlazerka.go.model.GameListener {
    @Override
    public void onStateAdvanced(GameState newState) {
      mBackButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onGameReset() {
      mBackButton.setVisibility(View.VISIBLE);
      mForwardButton.setVisibility(View.INVISIBLE);
    }
  }

}
