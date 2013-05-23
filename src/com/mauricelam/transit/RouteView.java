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
    private boolean noinfo = true;

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
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (text.length() == 0) {
            super.setText("No route information");
            noinfo = true;
        }
        noinfo = false;
        this.setHorizontallyScrolling(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (noinfo) return;
        Context context = this.getContext();

        float centerX = dp2Px(context, 7);
        float lineWidth = dp2Px(context, 7);
        float lineHeight = canvas.getHeight();
        float stopSeparation = this.getLineHeight();
        float stopRadius = dp2Px(context, 7);
        float padTop = dp2Px(context, 12), padBottom = stopSeparation;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(this.color);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(centerX, padTop, centerX, lineHeight - padBottom, paint);

        paint.setStrokeWidth(0);
        for (int i = (int) padTop; i < lineHeight; i += (int) stopSeparation) {
            canvas.drawCircle(centerX, i, stopRadius, paint);
        }
    }
}
