package com.dots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout.LayoutParams;

abstract class StoneView extends View {
  private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  RectF mRect = new RectF();

  public StoneView(Context context, int size) {
    super(context);
    setSize(size);
  }

  public void setSize(int s) {
    // The image itself.
    mFillPaint.setShader(getShader(s));

    // Where the image will be drawn.
    mRect.right = s;
    mRect.bottom = s;

    // The space parent should allocate for the image.
    LayoutParams params = new LayoutParams();
    params.width = s;
    params.height = s;
    setLayoutParams(params);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mRect.right == 0) {
      throw new IllegalStateException("You must set size");
    }
    canvas.drawOval(mRect, mFillPaint);
  }

  abstract Shader getShader(float size);
}
