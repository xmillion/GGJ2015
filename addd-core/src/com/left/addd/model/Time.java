package com.left.addd.model;

public class Time {
	/**
	 * in game time units (hours)
	 */
	private long hours;
	/**
	 * Real time units (seconds)
	 */
	private float realTime;
	// Conversion factor:
	// 1 seconds = 1 hour in game
	private static final float CONVERSION = 1;
	
	public Time() {
		this(0);
	}
	
	public Time(long time) {
		this.hours = time;
		this.realTime = 0;
	}
	
	public long getTime() {
		return hours;
	}
	
	public long getHour() {
		return hours % 24;
	}
	
	public long getDay() {
		return hours / 24;
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
			hours++;
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
