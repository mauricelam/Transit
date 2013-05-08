package com.mauricelam.transit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import static include.Helper.dp2Px;

/**
 * User: mauricelam
 * Date: 7/5/13
 * Time: 1:34 AM
 */
public class RouteView extends TextView {

    private int color = Color.BLACK;

    public RouteView(Context context) {
        super(context);
    }

    public RouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RouteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Context context = this.getContext();

        int centerX = dp2Px(context, 7);
        int lineWidth = dp2Px(context, 7);
        int lineHeight = canvas.getHeight();
        int stopSeparation = this.getLineHeight();
        int stopRadius = dp2Px(context, 7);
        int padTop = dp2Px(context, 12), padBottom = stopSeparation;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(this.color);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(centerX, padTop, centerX, lineHeight - padBottom, paint);

        paint.setStrokeWidth(0);
        for (int i = padTop; i < lineHeight; i += stopSeparation) {
            canvas.drawCircle(centerX, i, stopRadius, paint);
        }
    }
}
