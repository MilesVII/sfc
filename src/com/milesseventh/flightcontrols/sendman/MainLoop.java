package com.milesseventh.flightcontrols.sendman;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.SurfaceHolder;

public class MainLoop extends Thread {
	public SurfaceHolder sh;
	public SharedPreferences sp;
	public boolean running = true;
	public Game game;
	
	@Override
	public void run(){
		Rect r = sh.getSurfaceFrame();
		game = new Game(r.width(), r.height());
		game.settings = sp;
		while (running){
			Canvas c = sh.lockCanvas();
			if (c == null){
				running = false;
				break;
			}
			c.drawColor(Color.WHITE);
			game.update(c);
			sh.unlockCanvasAndPost(c);
		}
	}
}
