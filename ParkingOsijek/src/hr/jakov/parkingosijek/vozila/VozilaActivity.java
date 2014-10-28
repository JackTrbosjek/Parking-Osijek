package hr.jakov.parkingosijek.vozila;


import hr.jakov.parkingosijek.R;
import hr.jakov.parkingosijek.baza.Vozilo;
import hr.jakov.parkingosijek.vozila.ListaVozilaFragment.OnVoziloSelectedListener;
import hr.jakov.parkingosijek.vozila.OpisVozilaFragment.OnSaveClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;

public class VozilaActivity extends  ActionBarActivity implements OnVoziloSelectedListener,OnSaveClickListener{
	ListaVozilaFragment fragmentListaVozila;
	OpisVozilaFragment fragmentOpisVozila;
	View nothingSelected;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vozila);
		// Show the Up button in the action bar.
		setupActionBar();
		if (isPortrait()) {
			fragmentOpisVozila= OpisVozilaFragment.newInstance(true, new Vozilo());
            fragmentListaVozila = new ListaVozilaFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                    .add(R.id.fragment_container, fragmentListaVozila).commit();
        }else{
        	fragmentListaVozila= (ListaVozilaFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentListaVozila);
        	fragmentOpisVozila= (OpisVozilaFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentOpisVozila);
        	getSupportFragmentManager().beginTransaction().hide(fragmentOpisVozila).commit();
        	nothingSelected=findViewById(R.id.vozilaTvNothingSelected);
        	nothingSelected.setVisibility(View.VISIBLE);
        }
	}
	private boolean isPortrait(){
		if(findViewById(R.id.fragment_container)!=null){
			return true;
		}
		return false;
	}

	private void setupActionBar() {
		getSupportActionBar();
	}

	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onVoziloSelected(Vozilo vozilo) {
		System.out.println("selected vozilo "+vozilo.getNaziv());
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isPortrait()) {
            fragmentOpisVozila= OpisVozilaFragment.newInstance(false, vozilo);
            transaction.replace(R.id.fragment_container,fragmentOpisVozila);
            transaction.commit();
        }else{
        	fragmentOpisVozila.promjeniVozilo(false, vozilo);
        	transaction.show(fragmentOpisVozila).commit();
        	nothingSelected.setVisibility(View.GONE);
        }
        
    }
	

	@Override
	public void onDodajClicked() {
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isPortrait()) {
        	fragmentOpisVozila= OpisVozilaFragment.newInstance(true, new Vozilo());
            transaction.replace(R.id.fragment_container, fragmentOpisVozila);
            transaction.commit();
        }else{
        	fragmentOpisVozila.promjeniVozilo(true, new Vozilo());
        	transaction.show(fragmentOpisVozila).commit();
        	nothingSelected.setVisibility(View.GONE);
        }
		
	}

	
	@Override
	public void onSpremiClicked(boolean flagDodaj, Vozilo vozilo) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if(flagDodaj){
			fragmentListaVozila.addVozilo(vozilo);
		}else{
			fragmentListaVozila.updateVozilo(vozilo);
		}
		if(isPortrait()){
			transaction.replace(R.id.fragment_container, fragmentListaVozila).commit();
		}else{
			transaction.hide(fragmentOpisVozila).commit();
			nothingSelected.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void onOdustaniClicked() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if(isPortrait()){
			transaction.replace(R.id.fragment_container, fragmentListaVozila).commit();
		}else{
			transaction.hide(fragmentOpisVozila).commit();
			nothingSelected.setVisibility(View.VISIBLE);
		}
		
	}
	@Override
	public void onObrisiClicked(Vozilo vozilo) {
		fragmentListaVozila.removeVozila(vozilo);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if(isPortrait()){
			transaction.replace(R.id.fragment_container, fragmentListaVozila).commit();
		}else{
			transaction.hide(fragmentOpisVozila).commit();
			nothingSelected.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
