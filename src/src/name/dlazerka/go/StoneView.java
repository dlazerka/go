package name.dlazerka.go;

import static name.dlazerka.go.model.StoneColor.WHITE;
import name.dlazerka.go.model.Stone;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

class StoneView extends Drawable {
  final Stone stone;
  final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  int mSize;

  StoneView(Stone stone) {
    this.stone = stone;
  }

  public Stone getStone() {
    return stone;
  }

  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    super.setBounds(left, top, right, bottom);
    
    this.mSize = right - left;
    Shader shader = stone.getColor() == WHITE ? getWhite() : getBlack();
    mFillPaint.setShader(shader);
  }

  @Override
  public void draw(Canvas canvas) {
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

  @Override
  public void setAlpha(int alpha) {
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
  }

  @Override
  public int getOpacity() {
    return 0;
  }
}
