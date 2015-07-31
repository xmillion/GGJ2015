package com.left.addd.model;

public class Time {
	/**
	 * in game time units (minutes)
	 */
	private long minutes;
	/**
	 * Real time units (seconds)
	 */
	private float realTime;
	// Conversion factor:
	// 2 seconds = 3*10 minutes in game
	private static final float CONVERSION = 2/3f;
	private static final int TICKRATE = 10;
	
	public Time() {
		this(0);
	}
	
	public Time(long time) {
		this.minutes = time;
		this.realTime = 0;
	}
	
	public long getTime() {
		return minutes;
	}
	
	public long getMinute() {
		return minutes % 60;
	}
	
	public long getHour() {
		return (minutes / 60) % 24;
	}
	
	public long getDay() {
		return minutes / 1440;
	}
	
	/**
	 * Updates in game time.
	 * @param delta Real time passed in seconds.
	 * @return number of in game days passed within the real time.
	 */
	public int update(float delta) {
		realTime += delta;
		int ticks = 0;
		if(realTime > CONVERSION) {
			minutes+=TICKRATE;
			ticks++;
			realTime -= CONVERSION;
		}
		return ticks;
	}
	
	public static float getRealTimeFromTicks(int ticks) {
		return ticks * CONVERSION;
	}
	
	public static int getTicksFromRealTime(float realTime) {
		return (int) (realTime / CONVERSION);
	}
}
