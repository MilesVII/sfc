package com.milesseventh.flightcontrols.sendman;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameActivity extends Activity{
	class HarvesterOfEyes extends SurfaceView implements SurfaceHolder.Callback{
		MainLoop ml;
		
		public HarvesterOfEyes(Context context) {
			super(context);
			setFocusable(true);
			getHolder().addCallback(this);
			//Input handling
			setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent me){
					if (ml != null){
						ml.game.touchX = me.getX();
						ml.game.touchY = me.getY();
						if (me.getAction() == MotionEvent.ACTION_DOWN)
							ml.game.justTouched = true;
						if (me.getAction() == MotionEvent.ACTION_UP)
							ml.game.justReleased = true;
					}
					return true;
				}
				
			});
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}

		@Override
		public void surfaceCreated(SurfaceHolder sh){
			//Start game when drawing surface is available
			ml = new MainLoop();
			ml.sh = sh;
			ml.sp = PreferenceManager.getDefaultSharedPreferences(getContext());
			ml.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0){
			ml.running = false;
		}
	}
	
	public HarvesterOfEyes eyeless;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		eyeless = new HarvesterOfEyes(this);
		setContentView(eyeless);
	}
	
	@Override
	public void onBackPressed(){
	}
}
