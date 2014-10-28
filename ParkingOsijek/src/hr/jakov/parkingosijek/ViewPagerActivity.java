package hr.jakov.parkingosijek;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Zona;
import hr.jakov.parkingosijek.sinkronizacija.AsyncTaskRefresh;
import hr.jakov.parkingosijek.tools.GPS;
import hr.jakov.parkingosijek.vozila.VozilaActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ViewPagerActivity extends ActionBarActivity {
	ViewPager pager;
	PagerAdapter pagerAdapter;
	DatabaseHelper dbHelper;
	GPS gps;
	List<Zona> zone;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pager);
		getSupportActionBar();
		dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		
		pagerAdapter= new PagerAdapter(getSupportFragmentManager());
		
		pager=(ViewPager) findViewById(R.id.pager);
		provjeriZone();
		
		
	}

	private void provjeriZone() { 
		long broj = 0;
		try {
			broj = dbHelper.getZonaDao().countOf();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(broj==0){
			preuzimanjePodataka("first");
		}else{
			postaviAdapter();
		}
	}
	public void postaviAdapter(){
		zone=dbHelper.getZonaDataDao().queryForAll();
		pager.setAdapter(pagerAdapter); 
		gps = new GPS(this,new ArrayList<Zona>(zone));
		if(!gps.canGetLocation()){
			gps.showSettingsAlert();
		}
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_pager, menu);
		return true;
	}
	
	private void preuzimanjePodataka(String string){
		AsyncTaskRefresh async= new AsyncTaskRefresh(ViewPagerActivity.this);
		async.execute(string);
	}
	private void promptSettings() {
		new AlertDialog.Builder(this).setMessage("Problem prilikom provezivanja.\nProvjerite vezu s internetom").setNeutralButton("Uredu", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(i);
				finish();
			}
		}).setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		}).create().show();
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_vozila:
			Intent i= new Intent(this,VozilaActivity.class);
			startActivity(i);
			return true;
		case R.id.action_sinkronizacija:
			if(isConnected())
				preuzimanjePodataka("");
			else{
				promptSettings();
			}
			return true;
		case R.id.action_povijest:
			Intent in= new Intent(this,PovijestActivity.class);
			startActivity(in);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private boolean isConnected(){
		ConnectivityManager cm =
		        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}

	public class PagerAdapter extends FragmentPagerAdapter{

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if(position==0)
				return "Parking";
			if(position==1)
				return "Slobodna Mjesta";
			return "";
		}


		@Override
		public Fragment getItem(int position) {
			if(position==0){
				return new ParkingFragment();
			}
			if(position==1){
				return new SlobodnaMjestaFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(gps!=null){
		gps.stopUsingGPS();}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(gps!=null){
			gps.startUsingGPS();
			}
		
	}

}
