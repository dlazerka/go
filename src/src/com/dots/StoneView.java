package com.dots;

import static com.dots.model.StoneColor.*;
import com.dots.model.Stone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

class StoneView extends View {
  final Stone stone;
  final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  int mSize;

  StoneView(Stone stone, Context context) {
    super(context);
    this.stone = stone;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    this.mSize = right - left;
    Shader shader = stone.getColor() == WHITE ? getWhite() : getBlack();
    mFillPaint.setShader(shader);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawCircle(mSize / 2, mSize / 2, mSize / 2, mFillPaint);
  }

  RadialGradient getBlack() {
    return new RadialGradient(
        mSize * .3f, mSize * .3f, mSize * .8f,
        new int[] { 0xFF777777, 0xFF222222, 0xFF000000 },
        new float[] { 0, .3f, 1.0f },
        Shader.TileMode.CLAMP);
  }

  RadialGradient getWhite() {
    return new RadialGradient(
        mSize * .47f, mSize * .47f, mSize * .48f,
        new int[] { 0xFFFFFFFF, 0xFFDDDDDD, 0xFF777777 },
        new float[] { .7f, .9f, 1.0f },
        Shader.TileMode.CLAMP);
  }
}
