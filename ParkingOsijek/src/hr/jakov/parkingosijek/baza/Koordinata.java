package hr.jakov.parkingosijek.baza;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class Koordinata implements Serializable {

	private static final long serialVersionUID = 8761649401626489523L;
	@DatabaseField(id=true)
	int id;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
    Zona zona;
	@DatabaseField()
	double lat;
	@DatabaseField()
	double lon;
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public int getId() {
		return id;
	}
	public Zona getZona() {
		return zona;
	}
	
	public Koordinata(int id, Zona zona, double lat, double lon) {
		super();
		this.id = id;
		this.zona = zona;
		this.lat = lat;
		this.lon = lon;
	}
	public Koordinata() {
	}
	
}
