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
	// 1 second real time / 15 minutes in game
	private static final float CONVERSION = 1/15f;
	
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
	
	public long getMinutes() {
		return minutes % 60;
	}
	
	public long getTenMinutes() {
		return (minutes / 10) % 6;
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
	 * @return number of in game minutes passed within the real time.
	 */
	public int update(float delta) {
		realTime += delta;
		int ticks = 0;
		if(realTime > CONVERSION) {
			minutes++;
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
