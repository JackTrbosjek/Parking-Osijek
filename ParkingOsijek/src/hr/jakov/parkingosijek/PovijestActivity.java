package hr.jakov.parkingosijek;

import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Povjest;
import hr.jakov.parkingosijek.baza.Vozilo;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.table.TableUtils;

public class PovijestActivity extends ActionBarActivity {
	DatabaseHelper dbHelper;
	ListView list;
	List<Povjest> povjest;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_povijest);
		setupActionBar();
		dbHelper= OpenHelperManager.getHelper(getApplicationContext(), DatabaseHelper.class);
		list=(ListView)findViewById(R.id.povijestList);
		povjest=new ArrayList<Povjest>();
		try {
			povjest=dbHelper.getPovjestDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			povjest=new ArrayList<Povjest>();
		}
		setAdapter();
	}
	private void setAdapter(){
		list.setAdapter(new ListAdapter());
	}

	private void setupActionBar() {
		//setupActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.povjest, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_obrisi_povijest:
			brisanjePovijesti();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void brisanjePovijesti() {
		try {
			TableUtils.clearTable(dbHelper.getConnectionSource(), Povjest.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		povjest= new ArrayList<Povjest>();
		setAdapter();
	}
	
	private class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return povjest.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View view= inflater.inflate(R.layout.povijest_item, parent, false);
			TextView vrijemeTv=(TextView) view.findViewById(R.id.povijestVrijeme);
			TextView voziloTv=(TextView) view.findViewById(R.id.povijestVozilo);
			TextView datumTv=(TextView) view.findViewById(R.id.povijestDatum);
			
			int zona=povjest.get(position).getZona();
			Vozilo vozilo= povjest.get(position).getVozilo();
			Calendar vrijemeOd= Calendar.getInstance();
			vrijemeOd.setTimeInMillis(povjest.get(position).getVrijeme_od());
			Calendar vrijemeDo= Calendar.getInstance();
			vrijemeDo.setTimeInMillis(povjest.get(position).getVrijeme_do());
			DecimalFormat df=new DecimalFormat("00");
			String datum= df.format(vrijemeOd.get(Calendar.DAY_OF_MONTH))+"."+df.format((vrijemeOd.get(Calendar.MONTH)+1))+"."+vrijemeOd.get(Calendar.YEAR)+".";
			
			datumTv.setText(datum+"");
			vrijemeTv.setText("Zona "+zona+", "
			+df.format(vrijemeOd.get(Calendar.HOUR_OF_DAY))+":"+df.format(vrijemeOd.get(Calendar.MINUTE))+"-"
			+df.format(vrijemeDo.get(Calendar.HOUR_OF_DAY))+":"+df.format(vrijemeDo.get(Calendar.MINUTE)));
			voziloTv.setText("Vozilo: "+vozilo.getNaziv()+" ("+vozilo.getRegistracija()+")");
			return view;
		}
		
	}

}
