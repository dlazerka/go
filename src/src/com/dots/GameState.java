package com.dots;

import java.util.ArrayList;

import com.dots.Dot.Colour;

public class GameState {
  //
  public static final int SIZE = 11;
  private ArrayList<Dot> mRedDots;
  private ArrayList<Dot> mBlueDots;
  private Dot.Colour mCurrentTurn;
  private Dot[][] mGrid;
  
  public GameState() {
    //
    mRedDots = new ArrayList<Dot>();
    mBlueDots = new ArrayList<Dot>();
    mGrid = new Dot[SIZE][SIZE];
    reset();
  }
  
  public void reset() {
    mRedDots.clear();
    mBlueDots.clear();
    mCurrentTurn = Colour.CL_BLUE;
    for (int i = 0; i < SIZE; ++i)
      for (int j = 0; j < SIZE; ++j)
        mGrid[i][j] = null;
  }

  public boolean addDot(Dot.Colour color, int atX, int atY) {
    if (mGrid[atX][atY] != null) {
      return false;
    }
    Dot d = new Dot(color, atX, atY);
    getDots(color).add(d);
    mGrid[atX][atY] = d;
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
  public void flipTurn() {
    mCurrentTurn = mCurrentTurn == Colour.CL_BLUE ? Colour.CL_RED : Colour.CL_BLUE;
  }
}
