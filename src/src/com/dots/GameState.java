package com.dots;

import java.util.ArrayList;

import com.dots.Dot.Colour;

public class GameState {
  //
  private ArrayList<Dot> mRedDots;
  private ArrayList<Dot> mBlueDots;
  private Dot.Colour mCurrentTurn;
  
  public GameState() {
    //
    mRedDots = new ArrayList<Dot>();
    mBlueDots = new ArrayList<Dot>();
    reset();
  }
  
  public void reset() {
    mRedDots.clear();
    mBlueDots.clear();
    mCurrentTurn = Colour.CL_BLUE;
  }

  public void addDot(Dot.Colour color, int atX, int atY) {
    getDots(color).add(new Dot(color, atX, atY));
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
