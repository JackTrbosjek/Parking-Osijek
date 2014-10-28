package hr.jakov.parkingosijek.baza;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class Povjest implements Serializable {

	private static final long serialVersionUID = -3928652018438573226L;
	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField(foreign=true,foreignAutoCreate=true,foreignAutoRefresh=true)
	Vozilo vozilo;
	@DatabaseField()
	long vrijeme_od;
	@DatabaseField()
	long vrijeme_do;
	@DatabaseField()
	int zona;
	public Vozilo getVozilo() {
		return vozilo;
	}
	public void setVozilo(Vozilo vozilo) {
		this.vozilo = vozilo;
	}
	public long getVrijeme_od() {
		return vrijeme_od;
	}
	public void setVrijeme_od(long vrijeme_od) {
		this.vrijeme_od = vrijeme_od;
	}
	public long getVrijeme_do() {
		return vrijeme_do;
	}
	public void setVrijeme_do(long vrijeme_do) {
		this.vrijeme_do = vrijeme_do;
	}
	public int getZona() {
		return zona;
	}
	public void setZona(int zona) {
		this.zona = zona;
	}
	public int getId() {
		return id;
	}
	public Povjest(Vozilo vozilo, long vrijeme_od, long vrijeme_do, int zona) {
		this.vozilo = vozilo;
		this.vrijeme_od = vrijeme_od;
		this.vrijeme_do = vrijeme_do;
		this.zona = zona;
	}
	public Povjest(){}
}
