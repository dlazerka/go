package com.dots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class BlackStoneView extends StoneView {

//  public BlackStoneView(Context context, AttributeSet attrs) {
//    super(context, attrs);
//  }

  public BlackStoneView(Context context, int size) {
    super(context, size);
  }

  @Override
  Shader getShader(float size) {
    RadialGradient radialGradient = new RadialGradient(
        size * .3f, size * .3f, size * .8f,
        new int[] {0xFF777777, 0xFF222222, 0xFF000000},
        new float[] {0, .3f, 1.0f},
        Shader.TileMode.CLAMP);
    return radialGradient;
  }
}
