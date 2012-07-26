package com.dots;

import java.util.ArrayList;
import java.util.Stack;

import com.dots.Dot.Colour;

public class GameState {
  //
  public static final int SIZE = 11;
  private ArrayList<Dot> mRedDots;
  private ArrayList<Dot> mBlueDots;
  private Dot.Colour mCurrentTurn;
  private int mLastTurnId;
  private Dot[][] mGrid;
  private Stack mStack;
  private ArrayList<Dot> mSurrounded;
  
  public GameState() {
    //
    mRedDots = new ArrayList<Dot>();
    mBlueDots = new ArrayList<Dot>();
    mGrid = new Dot[SIZE][SIZE];
    mStack = new Stack<Dot>();
    //
    mSurrounded = new ArrayList<Dot>();
    reset();
  }
  
  public void reset() {
    mLastTurnId = 0;
    mRedDots.clear();
    mBlueDots.clear();
    mCurrentTurn = Colour.CL_BLUE;
    for (int i = 0; i < SIZE; ++i)
      for (int j = 0; j < SIZE; ++j)
        mGrid[i][j] = null;
    mStack.clear();
  }
  
  private int rayCount(int startX, int startY) {
    int result = 0;
    for (int i = 1; startY + i < SIZE; ++i) {
      Dot d = mGrid[startX][startY + i];
      if (d == null) continue;
      for (int j = 0; j < Dot.NUM_DIRECTIONS; ++j) if (Dot.dx[j] == -1 && d.neignbours[j]) {
        ++result;
        break;
      }
    }
    return result;
  }

  public boolean addDot(Dot.Colour color, int atX, int atY) {
    if (mGrid[atX][atY] != null || (rayCount(atX, atY) & 1) > 0) {
      return false;
    }
    Dot d = new Dot(color, atX, atY);
    getDots(color).add(d);
    mGrid[atX][atY] = d;
    search(d);
      //mStack
    //}
    flipTurn();
    return true;
  }
  
  public ArrayList<Dot> getDots(Dot.Colour color) {
    switch (color) {
    case CL_RED: return mRedDots;
    case CL_BLUE: return mBlueDots;
    }
    return null;
  }
  
  public Dot.Colour getCurrentTurn() { return mCurrentTurn; }
  private void flipTurn() {
    mCurrentTurn = Dot.oppositeColor(mCurrentTurn);
    ++mLastTurnId;
  }
  
  private int numSurrounded(Dot.Colour color) {
    mSurrounded.clear();
    ArrayList<Dot> dots = getDots(color);
    for (Dot d : dots) if (!d.isSurrounded) {
      int sign = 0;
      int n = mStack.size();
      boolean ok = true;
      for (int i = 0; i < n; ++i) {
        Dot cur = (Dot)mStack.get(i);
        Dot next = (Dot)mStack.get(i + 1 < n ? i + 1 : 0);
        int dx1 = d.x - cur.x, dy1 = d.y - cur.y;
        int dx2 = next.x - cur.x, dy2 = next.y - cur.y;
        int vp = dx1 * dy2 - dx2 * dy1;
        if (vp > 0) vp = 1;
        if (vp < 0) vp = -1;
        if (sign == 0) sign = vp;
        else if (sign != vp) {
          ok = false;
          break;
        }
      }
      if (ok) mSurrounded.add(d);
    }
    return mSurrounded.size();
  }
  
  private boolean search(Dot d) {
    if (mStack.indexOf(d) >= 0) {
      return false;
    }
    mStack.push(d);
    for (int i = 0; i < Dot.NUM_DIRECTIONS; ++i) {
      int nx = d.getNx(i), ny = d.getNy(i);
      if (nx < 0 || nx >= SIZE || ny < 0 || ny >= SIZE) continue;
      Dot nd = mGrid[nx][ny];
      if (nd == null || nd.color != d.color || nd.isSurrounded || nd == mStack.peek()) continue;
      if (mStack.size() >= 4 && nd == mStack.get(0) && numSurrounded(Dot.oppositeColor(d.color)) > 0 ||
          search(nd)) {
        // got it.
        if (mSurrounded.size() > 0) {
          for (Dot s : mSurrounded) {
            s.isSurrounded = true;
          }
          mSurrounded.clear();
        }
        d.neignbours[i] = nd.neignbours[Dot.oppositeDirection(i)] = true;
        mStack.pop();
        return true;
      }
      /*
      if (search(nd)) {
        // i <-> opposite.
        
        return true;
      }
      */
    }
    mStack.pop();
    return false;
  }
}
