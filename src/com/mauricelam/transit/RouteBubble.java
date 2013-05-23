package com.mauricelam.transit;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import include.Helper;

import static include.Helper.dp2Px;

/**
 * User: mauricelam
 * Date: 8/5/13
 * Time: 1:02 PM
 */
public class RouteBubble extends View {

    private boolean istop = false;
    private int color = Color.WHITE;
    private boolean shiny = false;
    private LinearGradient shader;

    private Paint paint;

    public RouteBubble(Context context) {
        super(context);
        init();
    }

    public RouteBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouteBubble(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init () {
        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter);
    }

    public void setColorFilter(int color) {
        ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        this.setColorFilter(filter);
    }

    public void clearColorFilter () {
        paint.setColorFilter(null);
    }

    public void setIStop (boolean istop) {
        this.istop = istop;
    }

    public void setColor (int color, boolean shiny) {
        this.color = color;
        this.shiny = shiny;
        if (this.shiny) {
            int brighter = Helper.lighterColor(color, 50);
            this.shader = new LinearGradient(
                    0, 0, 100, 100,
                    new int[] { color, brighter, color },
                    new float[] { 0, 0.5f, 1 },
                    Shader.TileMode.REPEAT
            );
        }
    }

    private Bitmap drawBitmap (int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        paint.setColor(this.color);
        paint.setStyle(Paint.Style.FILL);
        if (this.shiny)
            paint.setShader(this.shader);
        float centerX = canvas.getWidth() / 2.0f;
        float centerY = canvas.getHeight() / 2.0f;
        canvas.drawColor(0);
        canvas.drawCircle(centerX, centerY, centerX - 1, paint);
        paint.setShader(null);

        if (this.istop) {
            float iradius = dp2Px(getContext(), 2.5f);
            float halfIWidth = dp2Px(getContext(), 2.2f);

            int brighter = Helper.lighterColor(color, 30);
            paint.setColor(brighter);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawCircle(centerX, centerY - iradius, iradius, paint);
            RectF rect = new RectF(centerX - halfIWidth, centerY + 2, centerX + halfIWidth, canvas.getHeight());
            canvas.drawRoundRect(rect, 2, 2, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.rgb(0,0,0));
            paint.setAlpha(40);
            paint.setStrokeWidth(1.0f);
            canvas.drawCircle(centerX, centerY - iradius, iradius, paint);
            canvas.drawRoundRect(rect, 2, 2, paint);
        }

        paint.setAlpha(255);
        paint.setColor(Color.rgb(80, 80, 80));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, centerX - 1, paint);

        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = drawBitmap(canvas.getWidth(), canvas.getHeight());
        canvas.drawBitmap(bitmap, 0, 0, new Paint());
        bitmap.recycle();
    }

}
