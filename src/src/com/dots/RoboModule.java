package com.dots;

import com.dots.model.Game;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class RoboModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  Game game() {
    return new Game(19);
  }
}
