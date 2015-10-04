/*
 * Comments by Freddy Martens (atstechlab.wordpress.com).
 * 1) I replaced the word 'nick' with the word 'notch' because 'notch' is a more
 *    common used word i.e. "Turn it up a notch". The word 'nick' is used by English
 *    people when someone is arrested i.e. "He has been nicked".
 * 2) A few functions and variables have the word 'degree' in them. This is confusing.
 *    'degree' can be seen as angle or as temperature. I replaced 'degree' with 'value'
 *    in places where the variable or function refers to a temperature.
 * 3) I added a few constants. Hard coded variables were used when drawing the notches and
 *    values on the scale. I added a constant for drawing the numbers (every 5 notches) and
 *    I added a constant for the value increment of every notch.
 * 4) I added properties so that the user can configure the scale color and the scale limits.
 * 5) The total number of notches are evenly spread across the scale. Half on the left-hand
 *    side and the other half on the right hand side. This may cause not all values to be 
 *    visible on the scale, due to the amount of notches available. Each notch has a value 
 *    increment of +/-2. If you have a centerValue of '0', then the values 120, 130 and so on
 *    are not displayed. The code drawing the scale is a candidate for a rewrite. If you make 
 *    the scale limits leadings, you need to adjust the number of notches. 
 * 6) I added a rectangular to print the title in. It prepares the placement of the title in
 *    the lower or upper half of the scale. It also serves as an example to add additional 
 *    titles to the dial. 
 * 7) I updated the code so that it does not draw notches outside the scale limits. It also
 *    draws a larger notch where the digits are shown.
 *     
 * The comments given above are issues I ran into when adjusting the widget to my own needs.
 * I believe that there are more developers believing that the result of this code is really
 * nice but that reading the code might cause some frowning of the eyebrows.
 * 
 * Adding custom properties has been done to help other android developers understanding the 
 * implementation of custom properties. It all comes down to create an attrs.xml file in 
 * /res/values/ and reading them when initializing the scale. You can set the properties in
 * the layout file named main.xml.
 * 
 * @Ivan: You did a great job creating this code!
 */

package com.domotica.raspimanager.shared;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.domotica.raspimanager.R;

public final class Thermometer extends View implements SensorEventListener {

	private static final String TAG = Thermometer.class.getSimpleName();
	
	@SuppressWarnings("unused")
	private Handler handler;

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	public boolean moving = false;
	private RectF faceRect;
	private Bitmap faceTexture;
	private Paint facePaint;
	private Paint rimShadowPaint;
	
	private Paint scalePaint;
	private RectF scaleRect;
	
	private RectF titleRect;
	private Paint titlePaint;	
	private Path titleUpperPath;
	private Path titleLowerPath;

	private Paint logoPaint;
	private Bitmap logo;
	private Matrix logoMatrix;
	private float logoScale;
	
	private Paint handPaint;
	private Path handPath;
	private Paint handScrewPaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// Scale configuration: totalNotches, centerValue, minScaleValue, maxScaleValue and
	// scaleTitle can be given as property.
	// It's better to call this notch i.s.o. nick. There is a saying... "Turn it up a notch"
	public int totalNotches = 210;
	public int incrementPerLargeNotch = 20;
	private int incrementPerSmallNotch = 2;
	public float degreesPerNotch = 360.0f / totalNotches;	
	
	public int scaleCenterValue = 160; // the one in the top center (12 o'clock)
	public int scaleMinValue = 0;
	public int scaleMaxValue = 330;
	public String scaleUpperTitle = "UPS";
	public String scaleLowerTitle = "consuming UPS Watt";
	private int scaleColor = 0x9f004d0f;
	
	// hand dynamics -- all are angular expressed in F degrees
	public boolean handInitialized = false;
	public float handPosition = scaleCenterValue;
	private float handTarget = scaleCenterValue;
	private float handVelocity = 0.0f;
	private float handAcceleration = 0.0f;
	private long lastHandMoveTime = -1L;
	public static int consumi ;
	public static int alimetazione ;
	public static int carica ;
	public static String aggiornamento ;
	public static float EG ;
	public static float lastEG ;
	public static float EGMonths ;
	public static float lastEGMonths ;
	public Thermometer(Context context) {
		super(context);
		init(context, null);
	}

	public Thermometer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public Thermometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		attachToSensor();
	}

	@Override
	protected void onDetachedFromWindow() {
		detachFromSensor();
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		handInitialized = bundle.getBoolean("handInitialized");
		handPosition = bundle.getFloat("handPosition");
		handTarget = bundle.getFloat("handTarget");
		handVelocity = bundle.getFloat("handVelocity");
		handAcceleration = bundle.getFloat("handAcceleration");
		lastHandMoveTime = bundle.getLong("lastHandMoveTime");
		
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putBoolean("handInitialized", handInitialized);
		state.putFloat("handPosition", handPosition);
		state.putFloat("handTarget", handTarget);
		state.putFloat("handVelocity", handVelocity);
		state.putFloat("handAcceleration", handAcceleration);
		state.putLong("lastHandMoveTime", lastHandMoveTime);
		return state;
	}


	@SuppressLint("Recycle")
	private void init(Context context, AttributeSet attrs) {
		handler = new Handler();
		// Get the properties from the resource file.
		if (context != null && attrs != null){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Temperature);
			totalNotches           = a.getInt(R.styleable.Temperature_totalNotches,           totalNotches);
			incrementPerLargeNotch = a.getInt(R.styleable.Temperature_incrementPerLargeNotch, incrementPerLargeNotch);
			incrementPerSmallNotch = a.getInt(R.styleable.Temperature_incrementPerSmallNotch, incrementPerSmallNotch);
			scaleCenterValue       = a.getInt(R.styleable.Temperature_scaleCenterValue,       scaleCenterValue);
			scaleMinValue          = a.getInt(R.styleable.Temperature_scaleMinValue,          scaleMinValue);
			scaleMaxValue          = a.getInt(R.styleable.Temperature_scaleMaxValue,          scaleMaxValue);
			scaleColor             = a.getInt(R.styleable.Temperature_scaleColor,             scaleColor);
			String scaleUpperTitle = a.getString(R.styleable.Temperature_scaleUpperTitle);
			String scaleLowerTitle = a.getString(R.styleable.Temperature_scaleLowerTitle);

			if (scaleUpperTitle != null) this.scaleUpperTitle = scaleUpperTitle;
			if (scaleLowerTitle != null) this.scaleLowerTitle = scaleLowerTitle;
		}		

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		initDrawingTools();
	}

	
	private void attachToSensor() {
	
	
	}
	
	private void detachFromSensor() {
	
	}

	private void initDrawingTools() {
		rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);
		// the linear gradient is a bit skewed for realism
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, 
										   Color.rgb(0xf0, 0xf5, 0xf0),
										   Color.rgb(0x30, 0x31, 0x30),
										   Shader.TileMode.CLAMP));		

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
		rimCirclePaint.setStrokeWidth(0.005f);

		float rimSize = 0.02f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);		

		faceTexture = BitmapFactory.decodeResource(getContext().getResources(), 
				   R.drawable.brushed_alu);
		BitmapShader paperShader = new BitmapShader(faceTexture, 
												    Shader.TileMode.MIRROR, 
												    Shader.TileMode.MIRROR);
		Matrix paperMatrix = new Matrix();
		facePaint = new Paint();
		facePaint.setFilterBitmap(true);
		paperMatrix.setScale(1.0f / faceTexture.getWidth(), 
							 1.0f / faceTexture.getHeight());
		paperShader.setLocalMatrix(paperMatrix);
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setShader(paperShader);

		rimShadowPaint = new Paint();
		rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect.width() / 2.0f, 
				   new int[] { 0x00000000, 0x00000500, 0x50000500 },
				   new float[] { 0.96f, 0.96f, 0.99f },
				   Shader.TileMode.MIRROR));
		rimShadowPaint.setStyle(Paint.Style.FILL);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(scaleColor);
		scalePaint.setStrokeWidth(0.005f);
		scalePaint.setAntiAlias(true);
		scalePaint.setLinearText(true);
		scalePaint.setTextSize(0.045f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextScaleX(0.8f);
		scalePaint.setTextAlign(Paint.Align.CENTER);		
		
		// The scale rectangular is located .10 from the outer rim.
		float scalePosition = 0.10f;

		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

		// The title rectangular is located .25 from the outer rim.
		float titlePosition = 0.130f;
		
		titleRect = new RectF();
		titleRect.set(faceRect.left  + titlePosition, faceRect.top    + titlePosition,
					  faceRect.right - titlePosition, faceRect.bottom - titlePosition);

		titlePaint = new Paint();
		titlePaint.setColor(0xaf946109);
		titlePaint.setAntiAlias(true);
		titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
		titlePaint.setTextAlign(Paint.Align.CENTER);
		titlePaint.setTextSize(0.05f);
		titlePaint.setTextScaleX(0.8f);

		//titlePath = new Path();
		//titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f, -180.0f);
		// Put the title in a rectangular and not as stated above. The code above is 
		// the original code and it is hard to maintain these values. Setting the start
		// angle and sweep to a positive value causes the title to be displayed in the
		// top half of the scale i.e.: titlePath.addArc(titleRect, 180.0f, 180.0f);
		titleUpperPath = new Path();
		titleUpperPath.addArc(titleRect, 180.0f, 180.0f);
		titleLowerPath = new Path();
		titleLowerPath.addArc(titleRect, -180.0f, -180.0f);
		logoPaint = new Paint();
		logoPaint.setFilterBitmap(true);

		Bitmap bmp = Bitmap.createBitmap(350, 350,  Bitmap.Config.ARGB_8888);
//		Bitmap bmp = Bitmap.createBitmap(1500, 1500,  Bitmap.Config.ARGB_8888);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		Canvas c = new Canvas(bmp);
		c.drawCircle(60,50,25, paint);
		logo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.led_green);
//		logo = bmp;
		logoMatrix = new Matrix();
		logoScale = (0.5f / logo.getWidth()) * 0.3f;;
		logoMatrix.setScale(logoScale, logoScale);

		handPaint = new Paint();
		handPaint.setAntiAlias(true);
		handPaint.setColor(0xff392f2c);		
		handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
		handPaint.setStyle(Paint.Style.FILL);	
		
		// This code draws the hand with the tip facing north. When the hand is
		// not rotated, it points to the center value.
		handPath = new Path();
		handPath.moveTo(0.5f, 0.5f + 0.2f);
		handPath.lineTo(0.5f - 0.010f, 0.5f + 0.2f - 0.007f);
		handPath.lineTo(0.5f - 0.002f, 0.5f - 0.32f);
		handPath.lineTo(0.5f + 0.002f, 0.5f - 0.32f);
		handPath.lineTo(0.5f + 0.010f, 0.5f + 0.2f - 0.007f);
		handPath.lineTo(0.5f, 0.5f + 0.2f);
		handPath.addCircle(0.5f, 0.5f, 0.025f, Path.Direction.CW);
		
		handScrewPaint = new Paint();
		handScrewPaint.setAntiAlias(true);
		handScrewPaint.setColor(0xff493f3c);
		handScrewPaint.setStyle(Paint.Style.FILL);
		
		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		setMeasuredDimension(chosenDimension, chosenDimension);
	}
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		} 
	}
	
	// in case there is no size specified
	private int getPreferredSize() {
		return 300;
	}

	private void drawRim(Canvas canvas) {
		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
		// now the outer rim circle
		canvas.drawOval(rimRect, rimCirclePaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
		// draw the rim shadow inside the face
		canvas.drawOval(faceRect, rimShadowPaint);
	}

	private void drawScale(Canvas canvas) {
		// Draw a large notch every large increment, and a small
		// notch every small increment.
		
		canvas.drawOval(scaleRect, scalePaint);

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		for (int i = 0; i < totalNotches; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 - 0.015f;
			float y3 = y1 - 0.025f;
			
			int value = notchToValue(i);

			if (i % (incrementPerLargeNotch/incrementPerSmallNotch) == 0) {
				if (value >= scaleMinValue && value <= scaleMaxValue) {
					// draw a nick
					canvas.drawLine(0.5f, y1, 0.5f, y3, scalePaint);

					String valueString = Integer.toString(value);
					// Draw the text 0.15 away from y3 which is the long nick.
					canvas.drawText(valueString, 0.5f, y3 - 0.015f, scalePaint);
				}
			}
			else{
				if (value >= scaleMinValue && value <= scaleMaxValue) {
					// draw a nick
					canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);
				}
			}
			
			canvas.rotate(degreesPerNotch, 0.5f, 0.5f);
		}
		canvas.restore();		
	}
	
	private int notchToValue(int value) {
		int rawValue = ((value < totalNotches / 2) ? value : (value - totalNotches)) * incrementPerSmallNotch;
		int shiftedValue = rawValue + scaleCenterValue;
		return shiftedValue;
	}
	
	private float valueToAngle(float value) {
		return (value - scaleCenterValue) / 2.0f * degreesPerNotch;
	}
	
	private void drawTitle(Canvas canvas) {
		// Use a vertical offset when printing the title in the upper half of the scale. 
		// The upper and lower half use the same rectangular but the spacing  between the 
		// title and the scale is not equal for the upper and lower half of the dial and 
		// therefore you should move the upper title downwards.
		canvas.drawTextOnPath(scaleUpperTitle, titleUpperPath, 0.0f, 0.02f, titlePaint);				

		// We print the title on the lower half of the scale.
		canvas.drawTextOnPath(scaleLowerTitle, titleLowerPath, 0.0f,0.0f, titlePaint);				
	}
	
	private void drawLogo(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(0.5f - logo.getWidth() * logoScale / 2.0f, 
						 0.5f - logo.getHeight() * logoScale / 2.0f);

		// These 10 lines of code cause the logo to change color.
		int color = 0x00000000;
		float position = getRelativeTemperaturePosition();
		if (position < 0) {
			color |= (int) ((0xf0) * -position); // blue
		} else {
			color |= ((int) ((0xf0) * position)) << 16; // red			
		}
		//Log.d(TAG, "*** " + Integer.toHexString(color));
		LightingColorFilter logoFilter = new LightingColorFilter(0xff338822, color);
		logoPaint.setColorFilter(logoFilter);
		
		canvas.drawBitmap(logo, logoMatrix, logoPaint);
		canvas.restore();		
	}

	private void drawHand(Canvas canvas) {
		if (handInitialized) {
			float handAngle = valueToAngle(handPosition);
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(handAngle, 0.5f, 0.5f);
			canvas.drawPath(handPath, handPaint);
			canvas.restore();
			
			canvas.drawCircle(0.5f, 0.5f, 0.01f, handScrewPaint);
		}
	}

	private void drawBackground(Canvas canvas) {
		if (background == null) {
			Log.w(TAG, "Background not created");
		} else {
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		drawBackground(canvas);

		float scale = getWidth();		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);

		drawLogo(canvas);
		drawHand(canvas);
		
		canvas.restore();
	
		if (handNeedsToMove()) {
			moveHand();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "Size changed to " + w + "x" + h);
		
		regenerateBackground();
	}
	
	private void regenerateBackground() {
		// free the old bitmap
		if (background != null) {
	
			background.recycle();
		background = null;
		}
		if(getWidth() <= 0 && getHeight() <= 0)return;
		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = getWidth();		
		backgroundCanvas.scale(scale, scale);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
    	drawScale(backgroundCanvas);
		drawTitle(backgroundCanvas);		
	}

	private boolean handNeedsToMove() {
		return Math.abs(handPosition - handTarget) > 0.01f;
	}
	
	private void moveHand() {
		if (! handNeedsToMove()) {
			return;
		}
		
		if (lastHandMoveTime != -1L) {
			long currentTime = System.currentTimeMillis();
			float delta = (currentTime - lastHandMoveTime) / 1000.0f;

			float direction = Math.signum(handVelocity);
			if (Math.abs(handVelocity) < 90.0f) {
				handAcceleration = 5.0f * (handTarget - handPosition);
			} else {
				handAcceleration = 0.0f;
			}
			handPosition += handVelocity * delta;
			handVelocity += handAcceleration * delta;
			if ((handTarget - handPosition) * direction < 0.01f * direction) {
				handPosition = handTarget;
				handVelocity = 0.0f;
				handAcceleration = 0.0f;
				lastHandMoveTime = -1L;
			} else {
				lastHandMoveTime = System.currentTimeMillis();				
			}
			invalidate();
		} else {
			lastHandMoveTime = System.currentTimeMillis();
			moveHand();
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
	
	}

	public Thermometer getView(){
		return this;
	}
	
	
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.values.length > 0) {
			float temperatureC = sensorEvent.values[0];
			//Log.i(TAG, "*** Temperature: " + temperatureC);
			
			float temperatureF = (9.0f / 5.0f) * temperatureC + 32.0f;
			setHandTarget(temperatureF);
		} else {
			Log.w(TAG, "Empty sensor event received");
		}
	}

	
	
	public void change(float temp){
	
		regenerateBackground();
		setHandTarget(temp);
	
}	
	private float getRelativeTemperaturePosition() {
		if (handPosition < scaleCenterValue) {
			return - (scaleCenterValue - handPosition) / (scaleCenterValue - scaleMinValue);
		} else {
			return (handPosition - scaleCenterValue) / (scaleMaxValue - scaleCenterValue);
		}
	}
	
	private void setHandTarget(float temperature) {
		if (temperature < scaleMinValue) {
			temperature = scaleMinValue;
		} else if (temperature > scaleMaxValue) {
			temperature = scaleMaxValue;
		}
		handTarget = temperature;
		handInitialized = true;
		
	}
}
