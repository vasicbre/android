package rs.ac.bg.etf.vn110012d; 

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class Shaker implements SensorEventListener {
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private long lastUpdate = 0;
	private float last_x, last_y, last_z;
	private int shakeTreshold = 400;
	private long lastShake;
	private long timeGap;
	private Shaker.Callback cb;
	private Context context;

	public Shaker(Context context, int shakeTreshold, long timeGap,
			Shaker.Callback cb) {
		this.shakeTreshold = shakeTreshold;
		this.timeGap = timeGap;
		this.cb = cb;
		this.context = context;
		
		register();
		
	}

	public interface Callback {
		void shakingStarted();

		void shakingStopped();
	}
	
	public void register() {
		senSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = senSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		senSensorManager.registerListener(this, senAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void unregister() {
		senSensorManager.unregisterListener(this);
	}
	
	

	private void shaking() {
		long now = SystemClock.uptimeMillis();
			if (cb != null) {
				cb.shakingStarted();
			}
		lastShake = now;
	}

	private void notShaking() {
		long now = SystemClock.uptimeMillis();
		if (lastShake > 0) {
			if (now - lastShake > timeGap) {
				lastShake = 0;
				if (cb != null) {
					cb.shakingStopped();
				}
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {

		Sensor mySensor = sensorEvent.sensor;

		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = sensorEvent.values[0];
			float y = sensorEvent.values[1];
			float z = sensorEvent.values[2];

			// check whether more than 100 milliseconds have passed since the
			// last time onSensorChanged was invoked
			long curTime = System.currentTimeMillis();

			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				float speed = Math.abs(x + y + z - last_x - last_y - last_z)
						/ diffTime * 10000;

				if (speed > shakeTreshold) {
					shaking();
				} else {
					notShaking();
				}

				last_x = x;
				last_y = y;
				last_z = z;
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
