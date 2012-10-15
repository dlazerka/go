package name.dlazerka.go.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import name.dlazerka.go.model.Game;
import name.dlazerka.go.model.Stone;
import name.dlazerka.go.model.StoneColor;
import name.dlazerka.go.model.Game.KoRuleException;

import org.junit.Test;


public class GameTest {
  @Test
  public void testKo() throws Exception {
    Game game = new Game(19);

    game.makeTurnAt(0, 1);
    game.makeTurnAt(0, 2);
    game.makeTurnAt(1, 0);
    game.makeTurnAt(1, 3);
    game.makeTurnAt(2, 1);
    game.makeTurnAt(2, 2);
    game.makeTurnAt(1, 2);
    game.makeTurnAt(1, 1);// White captures
    try {
      game.makeTurnAt(1, 2);// Black tries to recapture
    } catch (KoRuleException e) {
      // Expected.
      game.makeTurnAt(5, 5);// Black plays elsewhere.
      game.makeTurnAt(5, 6);// White responds.
      game.makeTurnAt(1, 2);// Retake ko.
      assertTrue(game.getLastState().getStones().contains(new Stone(1, 2, StoneColor.BLACK)));
      assertFalse(game.getLastState().getStones().contains(new Stone(1, 1, StoneColor.WHITE)));
      return;
    }
    fail();
  }
}
