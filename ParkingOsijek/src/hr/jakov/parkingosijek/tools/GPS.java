package hr.jakov.parkingosijek.tools;

import hr.jakov.parkingosijek.baza.Zona;
import hr.jakov.parkingosijek.sinkronizacija.AsyncTaskGetMjesta;

import java.util.ArrayList;
import java.util.Collections;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class GPS extends Service implements LocationListener {
	public static final String ACTION_ZONE_GPS="hr.jakov.parkingosijek.gps.zone.action";
	public static final String KEY_ZONE="hr.jakov.parkingosijek.gps.zone.key";
	public static final String KEY_PRECIZNOST="hr.jakov.parkingosijek.gps.preciznost.key";
	private final Context mContext;

	boolean isGPSEnabled = false;

	boolean isNetworkEnabled = false;

	boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	float accuracy;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

	private static final long MIN_TIME_BW_UPDATES = 2000;

	protected LocationManager locationManager;

	private ArrayList<Zona> zone;

	public GPS(Context c,ArrayList<Zona> zone) {
		this.mContext = c;
		this.zone=zone;
		getLocation();
	}
	private BroadcastReceiver receiver= new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(AsyncTaskGetMjesta.ACTION_SLOBODNA_MJESTA)){
				ArrayList<Zona> primljeno=intent.getParcelableArrayListExtra(AsyncTaskGetMjesta.KEY_ZONE_SLOBODNA_MJESTA);
				for(Zona p:primljeno){
					for(Zona z:zone){
						if(p.getId()==z.getId()){
							z.setBrojMjesta(p.getBrojMjestaInt());
							break;
						}
					}
				}
			}
		}
	};

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				startUsingGPS();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void startUsingGPS() {
		System.out.println("GPS started");
		IntentFilter inf= new IntentFilter();
		inf.addAction(AsyncTaskGetMjesta.ACTION_SLOBODNA_MJESTA);
		mContext.registerReceiver(receiver, inf);
		try {
			if (isGPSEnabled){
				
				sendLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
						MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			else if (isNetworkEnabled){
				sendLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
				locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
				MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPS.this);
			try{
				mContext.unregisterReceiver(receiver);
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("GPS stopped");
		}
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		alertDialog.setTitle("GPS iskljuèen.");

		alertDialog
				.setMessage("Da biste u potpunosti mogli iskoristiti aplikaciju potrebno je ukljuèiti GPS.");

		alertDialog.setPositiveButton("Postavke",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});

		alertDialog.setNegativeButton("Odustani",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertDialog.show();
	}

	private void sendLocation(final Location location){
		Thread t= new Thread(){
			@Override
			public void run() {
				super.run();
				if(location==null)return;
				for (int i=0;i<zone.size();i++) {
					zone.get(i).odnosNaTocku(location.getLatitude(),location.getLongitude());
				}
				Collections.sort(zone);
				Intent i = new Intent();
				i.setAction(ACTION_ZONE_GPS);
				i.putExtra(KEY_ZONE, zone.toArray());
				i.putParcelableArrayListExtra(KEY_ZONE, zone);
				i.putExtra(KEY_PRECIZNOST, location.getAccuracy());
				mContext.sendBroadcast(i);
			}};
		try{
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (location != null) {
			sendLocation(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}