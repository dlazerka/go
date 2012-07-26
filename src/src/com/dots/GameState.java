package com.dots;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;
import android.util.Pair;

import com.dots.Dot.Colour;
import com.google.android.gms.games.GamesClient.TurnBasedMatchListener;
import com.google.android.gms.games.data.match.TurnBasedMatchImpl;

public class GameState implements TurnBasedMatchListener {
  //
  public static final int SIZE = 11;
  private ArrayList<Dot> mRedDots;
  private ArrayList<Dot> mBlueDots;
  private Dot.Colour mCurrentTurn;
  private Dot[][] mGrid;
  private int[][] mDisposition;
  private TurnBasedMatchImpl mMatch;
  private String mMyPlayerId;
  private String mOpponentPlayerId;

  public GameState(TurnBasedMatchImpl match, String myPlayerId) {
    this();
    mMatch = match;
    mMyPlayerId = myPlayerId;
    ArrayList<String> playerIds = match.getPlayerIds();
    if (playerIds.get(0).equals(myPlayerId)) {
      mOpponentPlayerId = playerIds.get(1);
    } else {
      mOpponentPlayerId = playerIds.get(0);
    }
  }

  public GameState() {
    mRedDots = new ArrayList<Dot>();
    mBlueDots = new ArrayList<Dot>();
    mGrid = new Dot[SIZE][SIZE];
    mDisposition = new int[SIZE][SIZE];
    reset();
  }

  public void reset() {
    mRedDots.clear();
    mBlueDots.clear();
    mCurrentTurn = Colour.CL_BLUE;
    for (int i = 0; i < SIZE; ++i)
      for (int j = 0; j < SIZE; ++j)
        mGrid[i][j] = null;

    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        if (mDisposition[i][j] != -1)
        mDisposition[i][j] = -1;
      }
    }
  }

  private boolean isDiagBlocked(int x, int y, int cl, int dir) {
    if (Dot.dx[dir] == 0 || Dot.dy[dir] == 0) return false;
    // check
    return mDisposition[x + Dot.dx[dir]][y] == cl && mDisposition[x][y + Dot.dy[dir]] == cl;
  }

  private void search(int startX, int startY, int cl, int ns, int[][] mm) {
    if (mm[startX][startY] != -1) return;
    Queue<Pair<Integer, Integer>> q = new LinkedList<Pair<Integer, Integer>>();
    q.add(new Pair<Integer, Integer>(startX, startY));
    mm[startX][startY] = ns;
    //while ()
    while (!q.isEmpty()) {
      //
      Pair<Integer, Integer> p = q.poll();
      int x = p.first.intValue();
      int y = p.second.intValue();
      for (int i = 0; i < Dot.NUM_DIRECTIONS; ++i) {
        //
        int nx = x + Dot.dx[i], ny = y + Dot.dy[i];
        if (0 <= nx && nx < SIZE && 0 <= ny && ny < SIZE && mm[nx][ny] == -1 &&
            !isDiagBlocked(x, y, cl, i)) {
          mm[nx][ny] = ns;
          q.add(new Pair<Integer, Integer>(nx, ny));
        }
      }
    }
  }
  
  static int cl2int(Dot.Colour c) {
    switch (c) {
      case CL_RED: return 1000;
      case CL_BLUE: return 2000;
    }
    return 3000;
  }
  
  private void fill(Colour color) {
    //
    //mDisposition = new int[SIZE][SIZE];
    int cl = cl2int(color);
    int[][] mm = new int[SIZE][SIZE];
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        if (mDisposition[i][j] == cl) {
          mm[i][j] = cl;
        } else {
          mm[i][j] = -1;
        }
      }
    }
    
    for (int i = 0, ns = 0; i < SIZE; ++i) {
      // start at (i, 0)
      search(i, 0, cl, ns++, mm);
      search(i, SIZE - 1, cl, ns++, mm);
      search(0, i, cl, ns++, mm);
      search(SIZE - 1, i, cl, ns++, mm);
    }
    
    int numSurrounded = 0;
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        if (mm[i][j] == -1) {
          if (mDisposition[i][j] != cl && mDisposition[i][j] != -1) {
            ++numSurrounded;
          }
          mDisposition[i][j] = cl;
        }
      }
    }
    if (numSurrounded > 0) {
      // draw the border.
      // TODO(rustema): add a better border computation.
      for (int i = 0; i < SIZE; ++i)
        for (int j = 0; j < SIZE; ++j) {
          //
          if (mGrid[i][j] != null && cl2int(mGrid[i][j].color) == cl) {
            for (int k = 0; k < Dot.NUM_DIRECTIONS; ++k) {
              int ni = i + Dot.dx[k], nj = j + Dot.dy[k];
              if (0 <= ni && ni < SIZE && 0 <= nj && nj < SIZE && mm[ni][nj] != cl &&
                  mm[ni][nj] >= 0) {
                mm[i][j] = -2;
              }
            }
          }
        }
      for (int i = 0; i < SIZE; ++i) 
        for (int j = 0; j < SIZE; ++j) if (mm[i][j] == -2) {
          for (int k = 0; k < Dot.NUM_DIRECTIONS; ++k) {
            int ni = i + Dot.dx[k], nj = j + Dot.dy[k];
            if (0 <= ni && ni < SIZE && 0 <= nj && nj < SIZE && mm[ni][nj] == -2) {
              // i -> ni
              mGrid[i][j].neignbours[k] = true;
            }
          }
        }
    }
  }
  
  public boolean addDot(Dot.Colour color, int atX, int atY) {
    if (mGrid[atX][atY] != null || mDisposition[atX][atY] != -1) {
      return false;
    }
    Dot d = new Dot(color, atX, atY);
    getDots(color).add(d);
    mGrid[atX][atY] = d;
    mDisposition[atX][atY] = cl2int(mCurrentTurn);
    
    fill(mCurrentTurn);
    flipTurn();
    /*
    for (int i = 0; i < SIZE; ++i) {
      StringBuilder sb = new StringBuilder();
      sb.append("line " + i + ":");
      for (int j = 0; j < SIZE; ++j) {
        sb.append(" " + mDisposition[i][j]);
      }
      Log.d("disp:", sb.toString());
    }
    Log.d("------", "-----------");
    */
    return true;
  }

  public ArrayList<Dot> getDots(Dot.Colour color) {
    switch (color) {
    case CL_RED: return mRedDots;
    case CL_BLUE: return mBlueDots;
    }
    return null;
  }

  public Dot.Colour getCurrentTurn() {
    if (mMatch == null) {
    return mCurrentTurn;
    } else {
      if (mMatch.getPendingPlayerId().equals(mMyPlayerId)) {
        return Dot.Colour.CL_RED;
      } else {
        return Dot.Colour.CL_BLUE;
      }
    }
  }
  private void flipTurn() {
    if (mMatch == null) {
      mCurrentTurn = Dot.oppositeColor(mCurrentTurn);
    } else {
      MainActivity.mGamesClient.takeTurn(this, mMatch.getMatchId(), new byte[] {}, mOpponentPlayerId);
    }
  }
  
  @Override
  public void onTurnBasedMatchLoaded(TurnBasedMatchImpl arg0) {
    Log.w(MainActivity.TAG, "onTurnBasedMatchLoaded in GameState");
  }
  
  public byte[] serialize() throws IOException {
    //Pair<int[][], Pair<ArrayList<Dot>, ArrayList<Dot>>> serialized = new Pair<int[][], Pair<ArrayList<Dot>, ArrayList<Dot>>>(
    //    mDisposition, new Pair<ArrayList<Dot>, ArrayList<Dot>>(mBlueDots, mRedDots));
    
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
    os.flush();
    os.writeObject(mDisposition);
    os.flush();
    os.writeObject(mBlueDots);
    os.flush();
    os.writeObject(mRedDots);
    os.flush();

    byte[] buf = byteStream.toByteArray();
    os.close();
    return buf;
  }
  
  public void deserialize(byte[] state) throws IOException, ClassNotFoundException {
    for (int i = 0; i < SIZE; ++i)
      for (int j = 0; j < SIZE; ++j)
        mGrid[i][j] = null;
    
    ByteArrayInputStream bis = new ByteArrayInputStream(state);
    ObjectInputStream ois = new ObjectInputStream(bis);
    
    mDisposition = (int[][]) ois.readObject();
    //System.out.println(book.toString());
    mBlueDots = (ArrayList<Dot>) ois.readObject();
    mRedDots = (ArrayList<Dot>) ois.readObject();
    
    ois.close();
    
    mCurrentTurn = mRedDots.size() == mBlueDots.size() ? Dot.Colour.CL_BLUE : Dot.Colour.CL_RED;
    for (Dot d : mRedDots) {
      mGrid[d.x][d.y] = d;
    }
    for (Dot d : mBlueDots) {
      mGrid[d.x][d.y] = d;
    }
    //private Dot.Colour mCurrentTurn;
    /*
    private Dot[][] mGrid;
    private int[][] mDisposition;
    private TurnBasedMatchImpl mMatch;
    private String mMyPlayerId;
    private String mOpponentPlayerId;
    */
  }
}
