// Author: Grant McKenzie grant.mckenzie@geog.ucsb.edu
// Date: January 2013
// Project: Android Activity
// Client: UCSB Geography

package edu.ucsb.geog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BurstSD {
	private ArrayList<Double> setOfVectors;
	private double vSum;
	private int size;
	private float mean;
	
	// Incoming Burst of accel values
	public BurstSD(Vector<JSONObject> vBurst) {
		
		this.size = vBurst.size();
		this.setOfVectors = new ArrayList(this.size);
		
		double x, y, z, v = 0;
		
		for(JSONObject r: vBurst) {
			try {
				x = (Double) r.get("accelx");
				y = (Double) r.get("accely");
				z = (Double) r.get("accelz");

				v = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
				
				this.setOfVectors.add(v);
				this.vSum += v;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		this.mean = (float) this.vSum / this.size;
		
	}
	// return the Standard Deviation
	public double getSD() {
		double sumsquares = 0;
		for(int i = 0; i < this.setOfVectors.size(); i++) {
			sumsquares += Math.pow((this.setOfVectors.get(i) - this.mean),2); 
		}
		double result = sumsquares / (this.size - 1);
		
		return Math.sqrt(result);
	}
}







