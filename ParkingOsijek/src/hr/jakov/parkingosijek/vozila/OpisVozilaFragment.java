package hr.jakov.parkingosijek.vozila;

import java.util.Locale;

import hr.jakov.parkingosijek.R;
import hr.jakov.parkingosijek.baza.Vozilo;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Use the
 * {@link OpisVozilaFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 */
public class OpisVozilaFragment extends Fragment {
	private static final String ARG_DODAJ = "hr.jakov.parkingosijek.vozila.pozicija";
	private static final String ARG_VOZILO = "hr.jakov.parkingosijek.vozila.vozilo";

	OnSaveClickListener mCallback;

	public interface OnSaveClickListener {

		public void onSpremiClicked(boolean flagDodaj, Vozilo vozilo);

		public void onOdustaniClicked();

		public void onObrisiClicked(Vozilo vozilo);

	}

	private boolean flagDodaj;
	private Vozilo vozilo;

	EditText etNaziv;
	EditText etRegistracija;
	Button btnSpremi;
	Button btnObrisi;
	ToggleButton tglDefault;
	Button btnOdustani;

	public static OpisVozilaFragment newInstance(boolean flagDodaj,
			Vozilo vozilo) {
		OpisVozilaFragment fragment = new OpisVozilaFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_DODAJ, flagDodaj);
		args.putSerializable(ARG_VOZILO, vozilo);
		fragment.setArguments(args);
		return fragment;
	}

	public OpisVozilaFragment() {
		flagDodaj = true;
		vozilo = new Vozilo();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			flagDodaj = getArguments().getBoolean(ARG_DODAJ);
			vozilo = (Vozilo) getArguments().getSerializable(ARG_VOZILO);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_opis_vozila, container,
				false);

		etNaziv = (EditText) root.findViewById(R.id.opisVozilaEtNaziv);
		etRegistracija = (EditText) root
				.findViewById(R.id.opisVozilaEtRegistracija);
		if (etRegistracija.isFocused())
			etRegistracija.addTextChangedListener(toUpperCase);
		btnSpremi = (Button) root.findViewById(R.id.opisVozilaBtnSpremi);
		btnSpremi.setOnClickListener(saveListener);

		tglDefault = (ToggleButton) root
				.findViewById(R.id.opisVozilaDefaultVoziloToggle);
		tglDefault.setOnCheckedChangeListener(toggleListener);

		btnObrisi = (Button) root.findViewById(R.id.opisVozilaBtnObrisi);
		if (!flagDodaj) {
			btnObrisi.setOnClickListener(obrisiListener);
		} else {
			btnObrisi.setVisibility(View.GONE);
		}

		btnOdustani = (Button) root.findViewById(R.id.opisVozilaBtnOdustani);
		btnOdustani.setOnClickListener(odustaniListener);
		promjeniVozilo(flagDodaj, vozilo);

		return root;
	}

	public void promjeniVozilo(boolean flagDodaj, Vozilo vozilo) {
		this.vozilo = vozilo;
		this.flagDodaj = flagDodaj;
		if (flagDodaj) {
			etNaziv.setText("");
			etRegistracija.setText("");
			tglDefault.setChecked(false);
			return;
		}
		etNaziv.setText(vozilo.getNaziv());
		etRegistracija.setText(vozilo.getRegistracija());
		tglDefault.setChecked(vozilo.isDefault_vozilo());
		closeKeyboard();
	}

	private TextWatcher toUpperCase = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			etRegistracija.setText(s.toString()
					.toUpperCase(Locale.getDefault()));
		}

	};

	private View.OnClickListener odustaniListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCallback.onOdustaniClicked();
			closeKeyboard();
		}
	};
	private OnCheckedChangeListener toggleListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			vozilo.setDefault_vozilo(isChecked);
			closeKeyboard();
		}
	};
	private View.OnClickListener obrisiListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCallback.onObrisiClicked(vozilo);
			closeKeyboard();
		}
	};
	private View.OnClickListener saveListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String registracija = etRegistracija.getText().toString();
			if (!registracija.equals("")
					&& !etNaziv.getText().toString().equalsIgnoreCase("")) {
				registracija = registracija.replaceAll("[^\\p{L}\\p{Nd}]", "");
				registracija=registracija.replaceAll(" ", "");
				vozilo.setNaziv(etNaziv.getText().toString());
				vozilo.setRegistracija(registracija);
				mCallback.onSpremiClicked(flagDodaj, vozilo);
				closeKeyboard();
			}else{
				Toast.makeText(getActivity(), "Nije unesena registracija ili naziv!", Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnSaveClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSaveClickListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(ARG_DODAJ, flagDodaj);
		outState.putSerializable(ARG_VOZILO, vozilo);
	}

	private void closeKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etRegistracija.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

}
