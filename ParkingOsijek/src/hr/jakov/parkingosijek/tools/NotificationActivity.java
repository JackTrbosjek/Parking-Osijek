package hr.jakov.parkingosijek.tools;

import java.text.DecimalFormat;

import hr.jakov.parkingosijek.ParkingService;
import hr.jakov.parkingosijek.R;
import hr.jakov.parkingosijek.ViewPagerActivity;
import hr.jakov.parkingosijek.baza.Vozilo;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends ActionBarActivity {
	TextView tvVrijeme,tvVozilo,tvZona;
	Button btnUkloniNotif,btnProduzi;
	Vozilo vozilo;
	int sat,min;
	int zona;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		getSupportActionBar();
		Intent i=getIntent();
		vozilo=(Vozilo) i.getSerializableExtra(ParkingService.KEY_VOZILO);
		sat=i.getIntExtra(ParkingService.KEY_SAT, -1);
		min=i.getIntExtra(ParkingService.KEY_MIN, -1);
		zona=i.getIntExtra(ParkingService.KEY_ZONA, 0);
		
		tvVrijeme=(TextView)findViewById(R.id.notifikacijaVrijemeTextView);
		tvVozilo=(TextView)findViewById(R.id.notifikacijaVoziloTextView);
		tvZona=(TextView)findViewById(R.id.notifikacijaZonaTextView);
		
		btnUkloniNotif=(Button)findViewById(R.id.notifikacijaUkloniBtn);
		btnUkloniNotif.setOnClickListener(ukloniListener);
		btnProduzi=(Button)findViewById(R.id.notifikacijaProduziBtn);
		btnProduzi.setOnClickListener(produziListener);
		setData();
	}
	private void setData() {
		tvVozilo.append(vozilo.getNaziv()+" ("+vozilo.getRegistracija()+")");
		String vrijeme="Parking Završen";
		if(sat!=-1&&min!=-1){
			vrijeme="Do "+new DecimalFormat("00").format(sat)+":"+new DecimalFormat("00").format(min);
		}else{
			btnUkloniNotif.setVisibility(View.GONE);
		}
		tvVrijeme.append(vrijeme+"");
		tvZona.setText("Zona "+zona+" - "+getKN());
	}
	private String getKN(){
		if(zona==1)
			return "4kn";
		if(zona==2)
			return "3kn";
		if(zona==3)
			return "2kn";
		return "";
	}
	private void ukloniNotifikaciju() {
		NotificationManager nm= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(vozilo.getId());
	}
	private void produziParking(){
		cancelExistingAlarm();
		Intent i = new Intent(this, ParkingService.class);
		i.putExtra(ParkingService.KEY_VOZILO, vozilo);
		i.putExtra(ParkingService.KEY_BROJ, getBroj());
		startService(i);
	}
	
	private String getBroj(){
		if(zona==1){
			return "8311";
		}
		if(zona==2){
			return "8312";
		}
		if(zona==3){
			return "8313";
		}
		return "";
	}
	private void cancelExistingAlarm() {
		AlarmManager am= (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(ParkingService.ACTION_KRAJ);
		i.putExtra(ParkingService.KEY_ZONA, zona);
		i.putExtra(ParkingService.KEY_VOZILO, vozilo);
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),
				0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(pi);		
	}
	private OnClickListener produziListener= new OnClickListener() {
		@Override
		public void onClick(View v) {
			produziParking();
		}
	};
	private OnClickListener ukloniListener= new OnClickListener() {
		@Override
		public void onClick(View v) {
			ukloniNotifikaciju();
		}

		
	};
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			startActivity(new Intent(this,ViewPagerActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
