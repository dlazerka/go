package com.dots;

import android.content.Context;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class WhiteStoneView extends StoneView {

  public WhiteStoneView(Context context, int size) {
    super(context, size);
  }

  @Override
  Shader getShader(float size) {
    RadialGradient radialGradient = new RadialGradient(
        size * .47f, size * .47f, size * .48f,
        new int[] {0xFFFFFFFF, 0xFFDDDDDD, 0xFF777777},
        new float[] {.7f, .9f, 1.0f},
        Shader.TileMode.CLAMP);
    return radialGradient;
  }
}
