package hr.jakov.parkingosijek;

import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Zona;
import hr.jakov.parkingosijek.sinkronizacija.AsyncTaskGetMjesta;
import hr.jakov.parkingosijek.sinkronizacija.AsyncTaskUnosMjesta;
import hr.jakov.parkingosijek.tools.GPS;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class SlobodnaMjestaFragment extends ListFragment  {

	List<Zona> zone;
	AdapterList adapter = new AdapterList();
	

	public SlobodnaMjestaFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			zone = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class).getZonaDao().queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		setListAdapter(adapter);
		
		if (zone.size() > 0) {
			runRefresh();
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		IntentFilter inf= new IntentFilter();
		inf.addAction(GPS.ACTION_ZONE_GPS);
		inf.addAction(AsyncTaskGetMjesta.ACTION_SLOBODNA_MJESTA);
		getActivity().registerReceiver(receiver, inf);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		timer.cancel();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onPause() {
		super.onPause();
		timer.cancel();
	}

	Timer timer;
	public void runRefresh(){
		final Handler handler = new Handler();
	    timer = new Timer();
	    TimerTask doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                    	AsyncTaskGetMjesta getMjesta = new AsyncTaskGetMjesta(getActivity(),zone);
	                    	getMjesta.execute("");
	                    } catch (Exception e) {
	                    }
	                }
	            });
	        }
	    };
	    if(zone!=null)
	    	timer.schedule(doAsynchronousTask, 1000, 1000*20);
	    
	}

	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
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
				refresh();
			}
			if(intent.getAction().equals(GPS.ACTION_ZONE_GPS)){
				ArrayList<Zona> primljeno=intent.getParcelableArrayListExtra(GPS.KEY_ZONE);
				zone=primljeno;
				refresh();
			}
		}
	};

	public void refresh() {
		
		adapter.notifyDataSetChanged();
	}

	private class AdapterList extends BaseAdapter {

		@Override
		public int getCount() {
			return zone.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getActivity().getLayoutInflater().inflate(
					R.layout.slobodna_mjesta_item, parent, false);
			TextView zonaTv = (TextView) view
					.findViewById(R.id.slobodna_mjesta_item_zona);
			TextView mjestaTv = (TextView) view
					.findViewById(R.id.slobodna_mjesta_item_mjesta);
			Button unosBtn = (Button) view
					.findViewById(R.id.slobodna_mjesta_item_unos);
			final Zona z = zone.get(position);
			if (z.getUdaljenost() < 150) {
				unosBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogUnos(z);
					}
				});
			} else {
				unosBtn.setEnabled(false);
			}
			zonaTv.setText("Zona " + z.getRank() + "\n" + z.getNaziv());
			mjestaTv.setText("Broj slobodnih mjesta: " + z.getBrojMjesta());
			String vrijeme=z.getVrijeme();
			if(!vrijeme.equals("")){
				mjestaTv.append("\nVrijeme: "+vrijeme);
			}

			return view;
		}

	}

	public void dialogUnos(final Zona z) {
		CharSequence[] items = { "0", "1-5", "5-10", "10-20", ">20" };
		AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
		build.setTitle(R.string.dialog_unos_slobodnih_mjesta_naslov)
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AsyncTaskUnosMjesta unesi = new AsyncTaskUnosMjesta(getActivity());
						switch (which) {
						case 0:
							unesi.execute(z.getId() + "", "0");
							break;
						case 1:
							unesi.execute(z.getId() + "", "15");
							break;
						case 2:
							unesi.execute(z.getId() + "", "510");
							break;
						case 3:
							unesi.execute(z.getId() + "", "1020");
							break;
						case 4:
							unesi.execute(z.getId() + "", "20");
							break;
						}
					}
				}).create().show();
	}

}
