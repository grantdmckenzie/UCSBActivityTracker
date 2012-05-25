package edu.ucsb.geog;

public class Fix {
	private double latitude;
	private double longitude;
	private long timestamp;
	private double accuracy;

	public Fix(double latitude2, double longitude2, long timestamp2) {
		latitude = latitude2;
		longitude = longitude2;
		timestamp = timestamp2;
	}
}
