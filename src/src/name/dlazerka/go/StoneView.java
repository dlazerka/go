package name.dlazerka.go;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Don't forget to call setSize(), or you get problems drawing. */
abstract class StoneView extends Drawable {
  private final Paint fillPaint;
  private final Matrix matrix;
  private final Shader shader;
  private int size;

  private StoneView(Shader shader) {
    this.shader = shader;
    matrix = new Matrix();
    fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    fillPaint.setShader(shader);
  }

  void setSize(int size) {
    this.size = size;
    matrix.setScale(size, size);
    shader.setLocalMatrix(matrix);
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.drawCircle(size / 2, size / 2, size / 2, fillPaint);
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

  static class Black extends StoneView {
    Black() {
      super(new RadialGradient(
          .3f, .3f, .8f,
          new int[] { 0xFF777777, 0xFF222222, 0xFF000000 },
          new float[] { 0, .3f, 1.0f },
          Shader.TileMode.CLAMP));
    }
  }

  static class White extends StoneView {
    White() {
      super(new RadialGradient(
          .47f, .47f, .48f,
          new int[] { 0xFFFFFFFF, 0xFFDDDDDD, 0xFF777777 },
          new float[] { .7f, .9f, 1.0f },
          Shader.TileMode.CLAMP));
    }
  }
}
