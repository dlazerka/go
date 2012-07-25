package com.dots;

import java.util.ArrayList;

import com.dots.Dot.Color;

public class GameState {
  //
  private ArrayList<Dot> mRedDots;
  private ArrayList<Dot> mBlueDots;
  private Dot.Color mCurrentTurn;
  
  public GameState() {
    //
    mRedDots = new ArrayList<Dot>();
    mBlueDots = new ArrayList<Dot>();
    mCurrentTurn = Color.BLUE;
  }

  public void addDot(Dot.Color color, int atX, int atY) {
    getDots(color).add(new Dot(color, atX, atY));
  }
  
  public ArrayList<Dot> getDots(Dot.Color color) {
    switch (color) {
    case RED: return mRedDots;
    case BLUE: return mBlueDots;
    }
    return null;
  }
  
  public Dot.Color getCurrentTurn() { return mCurrentTurn; }
  public void flipTurn() {
    mCurrentTurn = mCurrentTurn == Color.BLUE ? Color.RED : Color.BLUE;
  }
}
