package hr.jakov.parkingosijek.baza;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class Vozilo implements Serializable {

	
	private static final long serialVersionUID = 534115982776497403L;
	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField(unique=true)
	String naziv;
	@DatabaseField(unique=true)
	String registracija;
	@DatabaseField
	boolean default_vozilo;
	
	public Vozilo(){
		naziv="";
		registracija="";
		default_vozilo=false;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public String getRegistracija() {
		return registracija;
	}

	public void setRegistracija(String registracija) {
		this.registracija = registracija;
	}

	public boolean isDefault_vozilo() {
		return default_vozilo;
	}

	public void setDefault_vozilo(boolean default_vozilo) {
		this.default_vozilo = default_vozilo;
	}

	public int getId() {
		return id;
	}

}
