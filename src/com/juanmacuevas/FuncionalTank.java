package com.juanmacuevas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.util.DisplayMetrics;

public class FuncionalTank implements Renderable{
	/**
	 * Distance between the tank and the bottom of the screen
	 */
	private static final int TANK_BOTTOM_MARGIN = 7;
	/**
	 * Distance between the tank and the left margin of the screen
	 */
	private static final int TANK_LEFT_MARGIN = 0 ;
	/**
	 * vertical size of the tank
	 */
	private static final int TANK_HEIGHT = 55;
	/**
	 * horizontal size of the tank
	 */
	private static final int TANK_WIDTH = 106;
	/**
	 * gun barrel length
	 */
	private static final int GUNBARREL_LENGTH = 100;
	/**
	 * gun barrel width 
	 */
	private static final int GUNBARREL_WIDTH = 23; 

	/**
	 * status of the tank when inactive
	 */
	private static final int STATUS_IDLE=0;
	/**
	 * status of the tank while ready to shoot
	 */
	private static final int STATUS_POWERING=1;

	//private static final int STATUS_FIRE;
	/**
	 * keeps the status of the tank
	 */
	private int status;

	/**
	 * keeps the number of miliseconds since the tank started powering
	 */
	private long poweringTimer;
	private static final long POWERING_TIMER_LIMIT = 1200;

	public static float ShootOriginX;
	public static float ShootOriginY;

	private float tankLeft;
	private float tankTop;
	private float tankRight;

	public static float tankBottom;

	public static int gunBarrelEndX;
	public static  int gunBarrelEndY;



	private float angle;

	private int power;

	private Paint paint;
	private DisplayMetrics dm;
	private int lastBulletPower;
	public static float scale;

	public FuncionalTank(DisplayMetrics d){
		status = STATUS_IDLE;
		power = 0;
		lastBulletPower= 0;
		poweringTimer = 0;
		paint = new Paint();
		dm=d;		
		scale = (float) dm.densityDpi/160;
		tankLeft = TANK_LEFT_MARGIN * scale;
		tankTop = dm.heightPixels - (TANK_HEIGHT+TANK_BOTTOM_MARGIN)*scale;
		tankRight = (TANK_LEFT_MARGIN+TANK_WIDTH)*scale;
		tankBottom = dm.heightPixels - (TANK_BOTTOM_MARGIN * scale); 

		ShootOriginX = (float) (TANK_LEFT_MARGIN+60*scale);
		ShootOriginY = tankTop + 12*scale;

		setTarget(dm.widthPixels,0);
		GameThread.tankImg=GameThread.tankImg.createScaledBitmap(GameThread.tankImg,(int) (TANK_WIDTH*scale),(int) (TANK_HEIGHT*scale), true);
		GameThread.gunBarrelImg=GameThread.gunBarrelImg.createScaledBitmap(GameThread.gunBarrelImg,(int) (GUNBARREL_LENGTH*scale),(int) (GUNBARREL_WIDTH*scale), true);


	}

	/**
	 * Updates the status of the tank when it's powering
	 * 
	 * 
	 */
	public void update(long elapsedTime){		
		if (status == STATUS_POWERING){
			poweringTimer=poweringTimer+elapsedTime;
			if (poweringTimer>POWERING_TIMER_LIMIT)
				poweringTimer=0;


			power = (int) (((float)poweringTimer / POWERING_TIMER_LIMIT) *100);

			//logarithm to improve the powering control  high values
			//power = (int) (50 * Math.log10(power+1)); 
		}
	}

	/**
	 * draw the tank in the canvas surface
	 */
	public void draw(Canvas c) {

		/**	old graphic function
		paint.setStyle(Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(7*scale);
		c.drawLine(ShootOriginX, ShootOriginY, gunBarrelEndX, gunBarrelEndY, paint);
		 */

		// c.drawBitmap(GameThread.gunBarrelImg, tankLeft, tankTop, null);


		//draw the gun barrel
		Matrix m = new Matrix();
		m.postTranslate(tankLeft+40*scale, tankTop);
		m.postRotate((float) ((-angle)*180 /Math.PI),ShootOriginX,ShootOriginY);		   
		c.drawBitmap(GameThread.gunBarrelImg, m, null);		   
		//draw the Tank
		c.drawBitmap(GameThread.tankImg, tankLeft, tankTop, null);				
	}

	/**
	 * Sets the gun barrel to point the specified coordinate
	 * @param x
	 * @param y
	 */
	public void setTarget(int x, int y) {
		float previousAngle=angle;
		if (x<ShootOriginX) x = (int) ShootOriginX;
		if (y>ShootOriginY) y = (int) ShootOriginY;
		if (x==ShootOriginX && y==ShootOriginY) x=dm.widthPixels;

		angle = Math.abs((float) Math.atan((y-ShootOriginY)/(x-ShootOriginX)));
		//Log.i("angle","x: "+x+" y: "+y+" Angulo: "+Float.toString((float) (angle*180/Math.PI)));

		gunBarrelEndX = (int) (ShootOriginX + (Math.cos(angle) * (GUNBARREL_LENGTH-30) *scale));
		gunBarrelEndY = (int) (ShootOriginY - (Math.sin(angle) * (GUNBARREL_LENGTH-30) *scale));

		int aux = Math.abs( (int) ((previousAngle-angle)*180/Math.PI));
		if (aux>0) //only reproduces the sound when the angle changes at least one degree
			SoundManager.playMovegun();
		//Log.i("angle","gunBarrelEndX: "+gunBarrelEndX+" gunBarrelEndY: "+gunBarrelEndY+" Angulo: "+Float.toString((float) (angle*180/Math.PI)));

	}

	/**
	 * called when the user press the screen. It starts the powering process
	 */
	public void pressFire( ) {
		status = STATUS_POWERING;
		power = 0;		
		poweringTimer = 0;
	}

	/**
	 * called when the user release the finger and the shoot is performed
	 */
	public void releaseFire() {
		GameThread.shootBullet(angle,40+power*60/100,gunBarrelEndX,gunBarrelEndY);	
		lastBulletPower = power;
		status = STATUS_IDLE;
		power = 0;

	}




	/**
	 *  used to cancel the powering action
	 *  

	public void cancelFire() {
		status = STATUS_IDLE;
		power = 0;

	}
	 */
	
	/**
	 * gives information about the power of the tank
	 */
	public int getPower() {
		return power;
	}

	/**
	 * gives information about the angle of the gun barrel
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * used to show the power of the last shoot
	 * @return the power value of the last shoot
	 * 
	 */
	public int getLastBulletPower() {
		return lastBulletPower;
	}




}
