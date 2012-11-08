package name.dlazerka.go.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;

import name.dlazerka.go.GameArea;
import name.dlazerka.go.Util;

import org.junit.Test;


public class GameTest {
  /** Simplest Ko example. */
  @Test
  public void testKo() throws Exception {
    Game game = new Game(19);

    // Blacks on left.
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

  @Test
  public void testGetCapturedPlacement0() {
    assertEquals(0, Util.getCapturedRowCol(0));
  }

  @Test
  public void testGetCapturedPlacement1() {
    assertEquals(1000, Util.getCapturedRowCol(1));
  }

  @Test
  public void testGetCapturedPlacement2() {
    assertEquals(1, Util.getCapturedRowCol(2));
  }

  @Test
  public void testGetCapturedPlacement3() {
    assertEquals(1001, Util.getCapturedRowCol(3));
  }

  @Test
  public void testGetCapturedPlacement4() {
    assertEquals(2000, Util.getCapturedRowCol(4));
  }

  @Test
  public void testGetCapturedPlacement5() {
    assertEquals(2001, Util.getCapturedRowCol(5));
  }

  @Test
  public void testGetCapturedPlacement6() {
    assertEquals(2, Util.getCapturedRowCol(6));
  }

  @Test
  public void testGetCapturedPlacement7() {
    assertEquals(1002, Util.getCapturedRowCol(7));
  }

  @Test
  public void testGetCapturedPlacement8() {
    assertEquals(2002, Util.getCapturedRowCol(8));
  }

  @Test
  public void testGetCapturedPlacement9() {
    assertEquals(3000, Util.getCapturedRowCol(9));
  }

  @Test
  public void testGetCapturedPlacement10() {
    assertEquals(3001, Util.getCapturedRowCol(10));
  }
}
