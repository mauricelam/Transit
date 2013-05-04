package com.mauricelam.transit;

import include.GeoPoint;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;

import com.mauricelam.moreviews.StopMapImageView;

public class ShadowMapImageView extends StopMapImageView {

	private GeoPoint center;
	
	public ShadowMapImageView(Context context) {
		super(context);
		initShadow();
	}

	public ShadowMapImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initShadow();
	}

	public ShadowMapImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initShadow();
	}
	
	private ShapeDrawable.ShaderFactory normalShadowShader = new ShapeDrawable.ShaderFactory() {
		@Override
		public Shader resize(int width, int height) {
			LinearGradient gradient = new LinearGradient(0, 0, 0, height, new int[] { 0x33000000, 0x00000000,
					0x00000000, 0x11000000 }, new float[] { 0.0f, 0.04f, 0.97f, 1.0f }, Shader.TileMode.CLAMP);
			return gradient;
		}
	};
	
	private ShapeDrawable.ShaderFactory hoverShadowShader = new ShapeDrawable.ShaderFactory() {
		@Override
		public Shader resize(int width, int height) {
			LinearGradient gradient = new LinearGradient(0, 0, 0, height, new int[] { 0x33000000, 0x11000000,
					0x11000000, 0x14000000 }, new float[] { 0.0f, 0.04f, 0.97f, 1.0f }, Shader.TileMode.CLAMP);
			return gradient;
		}
	};
	
	private void initShadow() {
		ShapeDrawable normalShadow = new ShapeDrawable(new RectShape());
		normalShadow.setShaderFactory(normalShadowShader);
	
		ShapeDrawable hoverShadow = new ShapeDrawable(new RectShape());
		hoverShadow.setShaderFactory(hoverShadowShader);
	
		StateListDrawable overlay = new StateListDrawable();
		overlay.addState(new int[] { -android.R.attr.state_pressed }, normalShadow);
		overlay.addState(new int[] { android.R.attr.state_pressed }, hoverShadow);
		this.setOverlay(overlay);
	}
	
	public GeoPoint getCenter() {
		return center;
	}
	
	@Override
	public void setCenter(int lat, int lng) {
		super.setCenter(lat, lng);
		this.center = new GeoPoint(lat, lng);
	}
}
