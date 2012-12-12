package edu.ucsb.geog;

import java.util.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class BurstSD {
	private Vector<JSONObject> vBurst;
	private double[] distribution;
	private double vSum;
	private double size;
	private double mean;
	private double sd;
	
	public BurstSD(Vector<JSONObject> vBurst) {
		this.distribution = new double[vBurst.size()];
		this.size = vBurst.size();
		this.vBurst = vBurst;
		for(int i=0;i<vBurst.size();i++) {
			JSONObject r = vBurst.get(i);
			try {
				double x = (Double) r.get("accelx");
				double y = (Double) r.get("accely");
				double z = (Double) r.get("accelz");
				double v = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
				this.distribution[i] = v;
				this.vSum += v;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		this.mean = this.vSum / this.size;
	}
	public double getMean() {
		return this.mean;
	}
	public double getSD() {
		double temp = 0;
		for(double a :distribution)
            temp += (mean-a)*(mean-a);
		this.sd = Math.sqrt(temp/this.size);
		return this.sd;
	}
}
