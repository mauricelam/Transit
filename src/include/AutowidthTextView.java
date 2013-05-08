package include;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutowidthTextView extends TextView {
	private static final int maxSize = 30;
	private int targetWidth = 0;

	public AutowidthTextView(Context context) {
		super(context);
	}

	public AutowidthTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setTargetWidth(MeasureSpec.getSize(widthMeasureSpec) - 5);
	}

	public void setTargetWidth(int targetWidth) {
		if (this.targetWidth == targetWidth) {
			return;
		}
		this.targetWidth = targetWidth;
		fitToSize();
	}

	@Override
	public void setText(CharSequence text, TextView.BufferType type) {
		if (text.equals(this.getText())) {
			return;
		}
		super.setText(text, type);
		fitToSize();
	}

	private void fitToSize() {
		if (targetWidth == 0) {
			return;
		}
		String textString = this.getText().toString();
		TextPaint paint = this.getPaint();
		int upper = maxSize;
		int lower = 0;
		int guess = 0;
		for (int i = 0; i < 100; i++) {
			guess = (upper + lower) / 2;
			this.setTextSize(guess);
			float textwidth = paint.measureText(textString);
			if (guess == lower || ((targetWidth - textwidth) < 1 && (targetWidth - textwidth) > 0)) {
				break;
			}
			if (targetWidth < textwidth) {
				upper = guess;
			} else if (targetWidth > textwidth) {
				lower = guess;
			}
		}
	}
}
