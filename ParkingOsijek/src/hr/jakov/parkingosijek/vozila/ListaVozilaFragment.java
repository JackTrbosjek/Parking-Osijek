package hr.jakov.parkingosijek.vozila;



import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import hr.jakov.parkingosijek.R;
import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Vozilo;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ListaVozilaFragment extends ListFragment {
	
	
	
	
	OnVoziloSelectedListener mCallback;
	
	public interface OnVoziloSelectedListener {
    	
        public void onVoziloSelected(Vozilo vozilo);
        public void onDodajClicked();
        
    }
	
	
	
	List<Vozilo> vozila;
	AdapterList adapter;
	DatabaseHelper dbHelper;
	
	
	public ListaVozilaFragment() {
		// Required empty public constructor
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dbHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		vozila=dbHelper.getVoziloDataDao().queryForAll();
		adapter=new AdapterList();
        setListAdapter(adapter);
    }

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root= inflater.inflate(R.layout.fragment_lista_vozila, container,false);
		
		Button dodajVozilo=(Button)root.findViewById(R.id.vozilaDodajVozilo);
		dodajVozilo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mCallback.onDodajClicked();
			}
		});
		return root;
	}

	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            mCallback = (OnVoziloSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnVoziloSelectedListener");
        }
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCallback.onVoziloSelected(vozila.get(position));
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setItemChecked(position, true);
    }
	
	public void removeVozila(Vozilo vozilo){
		vozila.remove(vozilo);
		dbHelper.getVoziloDataDao().delete(vozilo);
		adapter.notifyDataSetChanged();
		System.out.println("removing vozilo:"+vozilo.getNaziv());
		Toast.makeText(getActivity(), "Uspješno obrisano vozilo:"+vozilo.getNaziv(), Toast.LENGTH_SHORT).show();
	}
	public void addVozilo(Vozilo vozilo){ 
		
		try {
			dbHelper.getVoziloDao().create(vozilo);
		} catch (SQLException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Postoji veæ vozilo istog naziva ili registracije.", Toast.LENGTH_SHORT).show();
			return;
		}
		vozila.add(vozilo);
		adapter.notifyDataSetChanged();
		System.out.println("addding vozilo:"+vozilo.getNaziv());
		Toast.makeText( getActivity(), "Uspješno dodano vozilo:"+vozilo.getNaziv(), Toast.LENGTH_SHORT).show();
	}
	
	public void updateVozilo(Vozilo vozilo){
		for(int i=0;i<vozila.size();i++){
			if(vozila.get(i).getId()==vozilo.getId()){
				try {
					dbHelper.getVoziloDao().update(vozilo);
				} catch (SQLException e) {
					e.printStackTrace();
					Toast.makeText(getActivity(), "Postoji veæ vozilo istog naziva ili registracije.", Toast.LENGTH_SHORT).show();
					break;
				}
				vozila.get(i).setNaziv(vozilo.getNaziv());
				vozila.get(i).setRegistracija(vozilo.getRegistracija());
				vozila.get(i).setDefault_vozilo(vozilo.isDefault_vozilo());
				
				adapter.notifyDataSetChanged();
				System.out.println("updateing vozilo:"+vozilo.getNaziv());
				Toast.makeText(getActivity(), "Uspješno spremljene promjene za vozilo:"+vozilo.getNaziv(), Toast.LENGTH_SHORT).show();
				break;
				
			}
		}
	}

	public class AdapterList extends BaseAdapter{
		
				
		@Override
		public int getCount() {
			
			return vozila.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null)
			convertView= LayoutInflater.from(getActivity()).inflate(R.layout.vozilo_item,parent, false);
			
			TextView naziv=(TextView)convertView.findViewById(R.id.spinnerItemNaziv);
			TextView registracija=(TextView)convertView.findViewById(R.id.spinnerItemRegistracija);
			
			
			naziv.setText(vozila.get(position).getNaziv());
			registracija.setText(vozila.get(position).getRegistracija());
			return convertView;
		}

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
