package hr.jakov.parkingosijek.baza;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;
import com.sromku.polygon.Polygon.Builder;

public class Zona implements Parcelable,Comparable<Zona> {

	@DatabaseField(id=true)
	int id;
	@DatabaseField(unique=true)
	String naziv;
	@DatabaseField()
	int rank;
	@ForeignCollectionField
	ForeignCollection<Koordinata> koordinate;
	public String getNaziv() {
		return naziv;
	}
	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getId() {
		return id;
	}
	
	public ForeignCollection<Koordinata> getKoordinate() {
		return koordinate;
	}
	public Zona(int id, String naziv, int rank) {
		super();
		this.id = id;
		this.naziv = naziv;
		this.rank = rank;
	}
	public Zona() {
		udaljenost=Float.MAX_VALUE;
		broj_mjesta=-1;
		vrijeme="";
	}
	private float udaljenost;
	public float getUdaljenost(){
		return udaljenost;
	}
	private int broj_mjesta;
	public String getBrojMjesta(){
		if(broj_mjesta==-1){
			return "Nema podatak";
		}
		if(broj_mjesta==0){
			return "0";
		}
		if(broj_mjesta==15){
			return "1-5";
		}
		if(broj_mjesta==510){
			return "5-10";
		}
		if(broj_mjesta==1020){
			return "10-20";
		}
		if(broj_mjesta==20){
			return">20";
		}
		return "Nema podatak";
	}
	public void setBrojMjesta(int broj_mjesta){
		this.broj_mjesta=broj_mjesta;
	}
	public int getBrojMjestaInt(){
		return broj_mjesta;
	}
	//vraæa udaljenost od toèke ili -1 ako je toèka unutar zone
	public float odnosNaTocku(double lat,double lon){
		CloseableIterator<Koordinata> it= getKoordinate().closeableIterator();
		Builder builder= Polygon.Builder();
		float[] results = new float[1];
		float udaljenost=Float.MAX_VALUE;
		while(it.hasNext()){
			Koordinata k=it.next();
			Point p= new Point((float)k.getLat(),(float)k.getLon());
			builder.addVertex(p);
			Location.distanceBetween(lat,lon, k.getLat(), k.getLon(),results);
			if(udaljenost>results[0])
				udaljenost=results[0];
		}
		Polygon p=builder.build();
		boolean sadrzi=p.contains(new Point((float)lat,(float)lon));
		if(sadrzi){
			udaljenost=-1;
		}
		this.udaljenost=udaljenost;
		return udaljenost;
		
	}
	
	private String vrijeme;
	public void setVrijeme(String data){
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(data);
			Calendar c= Calendar.getInstance();
			c.setTime(date);
			vrijeme=c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE);
			
		} catch (ParseException e) {
			e.printStackTrace();
			vrijeme="";
		}
	}
	public String getVrijeme(){
		return vrijeme;
	}
	
	@Override
	public int compareTo(Zona another) {
		if(this.udaljenost>another.udaljenost)return 1;
		if(this.udaljenost<another.udaljenost)return -1;
		return 0;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(naziv);
		dest.writeInt(rank);
		dest.writeFloat(udaljenost);
		dest.writeInt(broj_mjesta);
	}
	public static final Parcelable.Creator<Zona> CREATOR = new Parcelable.Creator<Zona>() {
        public Zona createFromParcel(Parcel in) {
            return new Zona(in); 
        }

        public Zona[] newArray(int size) {
            return new Zona[size];
        }
    };
    public Zona(Parcel in){
        id=in.readInt();
        naziv=in.readString();
        rank=in.readInt();
        udaljenost=in.readFloat();
        broj_mjesta=in.readInt();
        vrijeme="";
    }

}
