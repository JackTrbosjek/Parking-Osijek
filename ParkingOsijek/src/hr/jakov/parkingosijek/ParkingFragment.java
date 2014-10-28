package hr.jakov.parkingosijek;

import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Vozilo;
import hr.jakov.parkingosijek.baza.Zona;
import hr.jakov.parkingosijek.tools.GPS;
import hr.jakov.parkingosijek.vozila.VozilaActivity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ParkingFragment extends Fragment implements View.OnClickListener {
	Spinner registracija;
	List<Vozilo> vozila;
	Button zona1, zona2, zona3;
	TextView tvZona, tvUdaljenost, tvPreciznost;
	
	List<Zona> zone;
	Animation animScale;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		zone = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class)
				.getZonaDataDao().queryForAll();
		animScale = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		IntentFilter inf= new IntentFilter();
		inf.addAction(GPS.ACTION_ZONE_GPS);
		getActivity().registerReceiver(receiver, inf);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onPause() {
		super.onPause();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View main = inflater.inflate(R.layout.fragment_placanje, container,
				false);
		registracija = (Spinner) main.findViewById(R.id.parkingSpinner);
		dohvatiPodatkePostaviAdapter();
		zona1 = (Button) main.findViewById(R.id.parkingBtnZona1);
		zona1.setOnClickListener(this);
		zona2 = (Button) main.findViewById(R.id.parkingBtnZona2);
		zona2.setOnClickListener(this);
		zona3 = (Button) main.findViewById(R.id.parkingBtnZona3);
		zona3.setOnClickListener(this);
		tvZona = (TextView) main.findViewById(R.id.parkingZona);
		tvUdaljenost = (TextView) main.findViewById(R.id.parkingUdaljenost);
		tvPreciznost = (TextView) main.findViewById(R.id.parkingPreciznost);
		return main;
	}

	private void dohvatiPodatkePostaviAdapter() {
		DatabaseHelper dbHelper = OpenHelperManager.getHelper(getActivity(),
				DatabaseHelper.class);
		try {
			vozila = dbHelper.getVoziloDataDao().queryBuilder()
					.orderBy("default_vozilo", false).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		registracija.setAdapter(new AdapterSpinner());
	}

	@Override
	public void onResume() {
		super.onResume();
		if(registracija!=null)
			dohvatiPodatkePostaviAdapter();
		if(receiver!=null){
			IntentFilter inf= new IntentFilter();
			inf.addAction(GPS.ACTION_ZONE_GPS);
			getActivity().registerReceiver(receiver, inf);
		}
			
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(GPS.ACTION_ZONE_GPS)){
				ArrayList<Zona> primljeno =intent.getParcelableArrayListExtra(GPS.KEY_ZONE);
				float prec= intent.getFloatExtra(GPS.KEY_PRECIZNOST, -1);
				update(primljeno, prec);
			}
		}
	};
	
	public void update(List<Zona> zone, float accuracy) {
		
		int rank = zone.get(0).getRank();
		String ime = zone.get(0).getNaziv();
		float udaljenost = zone.get(0).getUdaljenost();
		// trenutno se nalazi u toj zoni
		if (udaljenost == -1) {
			if (rank == 1)
				zona1.startAnimation(animScale);
			if (rank == 2)
				zona2.startAnimation(animScale);
			if (rank == 3)
				zona3.startAnimation(animScale);
			tvZona.setText(R.string.parkingPredlozenaZona);
			tvZona.append("Zona " + rank + "\n" + ime);
			tvUdaljenost.setVisibility(View.GONE);
		} else {
			// najbliža zona (ne nalazi se u ni jednoj)
			tvZona.setText(R.string.parkingNajblizaZona);
			tvZona.append("Zona " + rank + ": " + ime); 
			tvUdaljenost.setVisibility(View.VISIBLE);
			tvUdaljenost.setText(R.string.parkingUdaljenost);
			tvUdaljenost.append(" " + Math.round(udaljenost) + "m");
		}
		tvPreciznost.setText(R.string.parkingPogreskaGPSa);
		tvPreciznost.append(" " + accuracy + "m");
	}

	@Override
	public void onClick(View v) {
		String broj = null;
		if (v == zona1) {
			broj = "708311";
		} else if (v == zona2) {
			broj = "708312";
		} else if (v == zona3) {
			broj = "708313";
		}
		final String fBroj= broj;
		if (vozila.size() > 0) {
			
			final Vozilo vozilo = vozila.get(registracija.getSelectedItemPosition());
			new AlertDialog.Builder(getActivity()).setTitle("Plaæanje").setMessage("Parking za vozilo:"+vozilo.getNaziv()).setPositiveButton("Uredu", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent i = new Intent(getActivity(), ParkingService.class);
					i.putExtra(ParkingService.KEY_VOZILO, vozilo);
					i.putExtra(ParkingService.KEY_BROJ, fBroj);
					getActivity().startService(i);
				}
			}).setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create().show();
			
		}else{
			Toast.makeText(getActivity(), R.string.parkingNemaVozila, Toast.LENGTH_SHORT).show();
		}
	}

	public class AdapterSpinner extends BaseAdapter {
		public AdapterSpinner() {
		}

		@Override
		public int getCount() {
			// ako nisu unesene registracije
			if (vozila.size() == 0) {
				return 1;
			}
			return vozila.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			convertView = LayoutInflater.from(getActivity()).inflate(
					R.layout.vozilo_item, parent, false);

			TextView naziv = (TextView) convertView
					.findViewById(R.id.spinnerItemNaziv);
			TextView registracija = (TextView) convertView
					.findViewById(R.id.spinnerItemRegistracija);

			// zadnji element
			if (vozila.size() == 0) {
				naziv.setText(R.string.urediVozila);
				naziv.setTextSize(20);
				registracija.setVisibility(View.GONE);
				convertView.setOnClickListener(listener);
				return convertView;
			}

			naziv.setText(vozila.get(position).getNaziv());
			registracija.setText(vozila.get(position).getRegistracija());
			return convertView;
		}

		private View.OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), VozilaActivity.class);
				startActivity(i);
			}
		};

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {

			return 0;
		}

	}


}
