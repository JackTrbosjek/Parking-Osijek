package hr.jakov.parkingosijek.sinkronizacija;

import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Koordinata;
import hr.jakov.parkingosijek.baza.Zona;

import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;



public class JSONclass {
	private Context c;
	private JSONObject json;
	private ArrayList<Zona> zone;
	private ArrayList<Koordinata> koordinate;
	
	public static JSONclass parsaj(String html,Context c){
		return new JSONclass(html,c);
	}
	public JSONclass(String html,Context c){
		this.c=c;
		zone= new ArrayList<Zona>();
		koordinate= new ArrayList<Koordinata>();
		html=html.replace("u00d0", "u0110");
		html=html.replace("u00f0", "u0111");
		html=html.replace("u008a", "u0160");
		html=html.replace("u009a", "u0161");
		html=html.replace("u008e", "u017D");
		html=html.replace("u009e", "u017E");
		html=html.replace("u00c6", "u0106");
		html=html.replace("u00e6", "u0107");
		html=html.replace("u00c8", "u010C");
		html=html.replace("u00e8", "u010D");
	     //Ð
		 //ð
		 //Š
		 //š
	     //Ž
		 //ž
		 //È
		 //è
		 //Æ
		 //æ
	
		try {
			json= new JSONObject(html);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		parsanje();
		try {
			popuniBazu();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parsanje(){ 
		try {
			JSONArray jZone= json.getJSONArray("zone"); 
			for(int i =0;i<jZone.length();i++){
				JSONObject obj= jZone.getJSONObject(i);
				zone.add(new Zona(obj.getInt("id"), obj.getString("naziv"), obj.getInt("rank")));
			}
			
			JSONArray jKoordinate= json.getJSONArray("koordinate"); 
			for(int i=0;i<jKoordinate.length();i++){
				JSONObject obj= jKoordinate.getJSONObject(i);
				int id_zone=obj.getInt("id_zone");
				for(Zona z:zone){
					if(z.getId()==id_zone)
						koordinate.add(new Koordinata(obj.getInt("id"), z, obj.getDouble("lat"), obj.getDouble("lon")));
				}
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void popuniBazu() throws SQLException{
		DatabaseHelper dbHelper = OpenHelperManager.getHelper(c, DatabaseHelper.class);
		TableUtils.clearTable(dbHelper.getConnectionSource(), Zona.class);
		TableUtils.clearTable(dbHelper.getConnectionSource(), Koordinata.class);
		for(Zona z: zone)
			dbHelper.getZonaDao().create(z);
		for(Koordinata k:koordinate)
			dbHelper.getKoordinataDao().create(k);
	}
	
}

