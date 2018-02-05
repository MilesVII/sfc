package com.milesseventh.flightcontrols.sendman;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.Log;

public class Game {	
	public abstract class UI {
		public abstract void render(Canvas c);
		public float x, y, w, h, px, py;
		
		public UI(float _px, float _py, float _w, float _h){
			px = _px;
			py = _py;
			w = _w;
			h = _h;
			reset();
		}
		
		public void reset(){
			x = w / 2f + px;
			y = h / 2f + py;
		}

		public void touch(float tx, float ty){
			x = clamp(tx, px, px + w);
			y = clamp(ty, py, py + h);
		}
		
		public boolean hovered(float tx, float ty){
			return tx >= px && tx <= (px + w) && ty >= py && ty <= (py + h);
		}
		
		public float normalizedX(){
			return (x - px) / w * 2f - 1f;
		}
		
		public float normalizedY(){
			return (y - py) / h * 2f - 1f;
		}
		
	}
	public class Mushroom extends UI {
		public Mushroom(float _px, float _py, float _w, float _h){
			super(_px, _py, _w, _h);
		}
		
		@Override
		public void render(Canvas c){
			pain.setStyle(Style.STROKE);
			c.drawRect(px, py, px + w, py + h, pain);
			pain.setStyle(Style.FILL);
			c.drawLine(px, py + h / 2f, px + w, py + h / 2f, pain);
			c.drawLine(px + w / 2f, py, px + w / 2, py + h, pain);
			c.drawCircle(x, y, 17, pain);
		}
	}
	public class Slider extends UI {
		public boolean isV, isSticky;
		
		public Slider(float _px, float _py, float _w, float _h, boolean _isV, boolean _isSticky) {
			super(_px, _py, _w, _h);
			isV = _isV;
			isSticky = _isSticky;
		}
		
		@Override
		public void reset(){
			if (!isSticky)
				super.reset();
		}
		
		@Override
		public float normalizedX(){
			if (isV)
				return 0;
			else 
				return super.normalizedX();
		}

		@Override
		public float normalizedY(){
			if (isV)
				return super.normalizedY();
			else
				return 0;
		}

		public float normalized(){
			if (isV)
				return super.normalizedY();
			else
				return super.normalizedX();
		}
		
		@Override
		public void render(Canvas c) {
			pain.setStyle(Style.STROKE);
			c.drawRect(px, py, px + w, py + h, pain);
			pain.setStyle(Style.FILL);
			if (isV)
				c.drawLine(px, py + y, px + w, py + y, pain);
			else
				c.drawLine(px + x, py, px + x, py + h, pain);
		}
		
	}
	
	public Paint pain      = new Paint(), 
	             titlePain = new Paint();
	public float touchX = -1, touchY;
	public boolean justTouched = false,
	               justReleased = true;
	public UI active = null;
	public UI[] ui = new UI[3];
	private DatagramSocket socket;
	InetAddress satellite;
	public Game(int w, int h){
		pain.setColor(Color.BLACK);
		pain.setTextAlign(Align.LEFT);
		pain.setTextSize(20);
		
		/* Multitouch unsupported
		ui[0] = new Slider(w * .9f,       0, w * .1f,       h, true, false);//Pitch control
		ui[1] = new Slider(0      , h * .8f, w * .5f, h * .2f, false, false);//Roll control
		ui[2] = new Slider(0      ,       0, w * .1f, h * .8f, true, true);//Thrust control*/
		if (w > h){
			ui[0] = new Mushroom((w - h) / 2, 0, h, h);
			ui[1] = new Mushroom(w - (w - h) / 2, 0, (w - h) / 2, (w - h) / 2);
			ui[2] = new Slider(0, 0, (w - h) / 4, h, true, true);
		} else {
			ui[0] = new Mushroom(0, (h - w) / 2, w, w);
			ui[1] = new Mushroom(w - (h - w) / 2, h - (h - w) / 2, (h - w) / 2, (h - w) / 2);
			ui[2] = new Slider(0, 0, w, (h - w) / 4, false, true);
		}
		
		try {
			socket = new DatagramSocket();
			satellite = InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, (byte)43, (byte)67});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update(Canvas canvas){
		
		//Controller positioning
		if (touchX >= 0){
			if (justTouched){
				for (UI x: ui)
					if (x.hovered(touchX, touchY)){
						active = x;
						break;
					}
				justTouched = false;
			}
			
			if (active != null){
				active.touch(touchX, touchY);
			}
			touchX = -1;
		} else if (justReleased){
			if (active != null)
				active.reset();
			active = null;
			justReleased = false;
		}

		for (UI x: ui)
			x.render(canvas);
		
		//Status bar
		//canvas.drawText(String.format(""), 5, canvas.getHeight() - 5, pain);
		
		//Sending
		try{
			byte[] z = ByteBuffer.allocate(Float.SIZE / 8 * 5)
			                     .putFloat(ui[0].normalizedX())//Main
			                     .putFloat(ui[0].normalizedY())
			                     .putFloat(ui[1].normalizedX())//POW hat
			                     .putFloat(ui[1].normalizedY())
			                     .putFloat(((Slider)ui[2]).normalized())//Thrust control
			                     .array();
			DatagramPacket dp = new DatagramPacket(z, z.length, satellite, 10777);
			socket.send(dp);
		} catch (Exception e){
			Log.d("FAILED", e.getMessage());
		}
	}
	
	public float clamp(float x, float min, float max){
		return Math.min(Math.max(min, x), max);
	}
	
	SharedPreferences settings;
	
	public void save(){
		Editor e = settings.edit();
		e.clear();
		
		e.commit();
	}
	
	public boolean load(){
		int l = settings.getInt("LVL", -1);
		
		if (l == -1)
			return false;
		
		return true;
	}
	
	public void resetSave(){
		Editor e = settings.edit();
		e.clear();
		e.commit();
	}
}
