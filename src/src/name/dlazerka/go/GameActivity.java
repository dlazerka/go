package name.dlazerka.go;

import name.dlazerka.go.model.Game;
import name.dlazerka.go.model.GameState;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

@ContentView(R.layout.activity_game)
public class GameActivity extends GamesApiActivity {
  // private static final String CONNECTING_MESSAGE = "Connecting to Games API";
  // private GameState mGameState;
  // private TurnBasedMatchImpl mMatch;
  // private Toast currentToast;

  @InjectView(R.id.gameArea)
  GameArea gameArea;
  @InjectView(R.id.pass)
  Button pass;
  @InjectView(R.id.back)
  Button back;
  @InjectView(R.id.forward)
  Button forward;
  @InjectView(R.id.turnNo)
  TextView turnNo;

  @Inject
  Game game;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    game.addListener(new GameListener());

    Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Shojumaru-Regular.ttf");
    pass.setTypeface(font);
    back.setTypeface(font);
    turnNo.setTypeface(font);
    forward.setTypeface(font);

    updateBackForward();
    pass.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        game.passTurn();
      }
    });
    back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int turnNo = gameArea.gameState.getTurnNo();
        if (turnNo == 0) {
          // Shouldn't be possible, because button is gone.
          Ln.e("Attempt to go back before first turn");
          return;
        }
        GameState prevState = game.getStateAt(turnNo - 1);
        gameArea.setGameState(prevState);

        updateBackForward();
      }
    });
    forward.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int turnNo = gameArea.gameState.getTurnNo();
        GameState lastState = game.getLastState();
        if (turnNo == lastState.getTurnNo()) {
          // Shouldn't be possible, because button is gone.
          Ln.e("Attempt to go forward after last turn");
          return;
        }
        GameState prevState = game.getStateAt(turnNo + 1);
        gameArea.setGameState(prevState);

        updateBackForward();
      }
    });
    gameArea.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (!game.getLastState().equals(gameArea.gameState)) {
          gameArea.setGameState(game.getLastState());
          gameArea.skipNextTouchUp();
          updateBackForward();
          return true;
        }
        return false;
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

  private void updateBackForward() {
    if (gameArea.gameState.equals(game.getLastState())) {
      forward.setVisibility(View.INVISIBLE);
    } else {
      forward.setVisibility(View.VISIBLE);
    }
    if (gameArea.gameState.equals(game.getStateAt(0))) {
      back.setVisibility(View.INVISIBLE);
    } else {
      back.setVisibility(View.VISIBLE);
    }

    turnNo.setText(gameArea.gameState.getTurnNo() + "");
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

  // void showToastConnecting() {
  // if (currentToast == null) {
  // currentToast = Toast.makeText(GameActivity.this, CONNECTING_MESSAGE,
  // Toast.LENGTH_SHORT);
  // }
  // // To not schedule many toasts.
  // if (!currentToast.getView().isShown()) {
  // currentToast.show();
  // }
  // }

  @Override
  protected GamesApiListener newGamesApiListener() {
    return new GamesApiListener();// Not interested.
  }

  private class GameListener implements name.dlazerka.go.model.GameListener {
    @Override
    public void onStateAdvanced(GameState newState) {
      updateBackForward();
    }

    @Override
    public void onGameReset() {
      updateBackForward();
    }
  }

}
