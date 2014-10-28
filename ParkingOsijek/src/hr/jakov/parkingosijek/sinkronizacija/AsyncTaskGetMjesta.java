package hr.jakov.parkingosijek.sinkronizacija;

import hr.jakov.parkingosijek.baza.Zona;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class AsyncTaskGetMjesta extends AsyncTask<String, Void, List<Zona>> {
	public static final String ACTION_SLOBODNA_MJESTA="hr.jakov.parkingosijek.sinkronizacija.slobodnamjesta";
	public static final String KEY_ZONE_SLOBODNA_MJESTA="hr.jakov.parkingosijek.sinkronizacija.slobodnamjesta.key";
	private String url;
	private Context c;
	private List<Zona> zone;

	public AsyncTaskGetMjesta(Context c,List<Zona> zone) {
		this.zone=zone;
		this.c = c;
		this.url = new String("http://jakov-videkovic.from.hr/parking/mjesta.php");

	}

	@Override
	protected void onPostExecute(List<Zona> zone) {
		super.onPostExecute(zone);
		Intent i= new Intent();
		i.setAction(ACTION_SLOBODNA_MJESTA);
		ArrayList<Zona> send= new ArrayList<Zona>(zone);
		i.putParcelableArrayListExtra(KEY_ZONE_SLOBODNA_MJESTA, send);
		c.sendBroadcast(i);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	protected List<Zona> doInBackground(String... parametri) {
		String html = "";

		if (!hasConnection()) {

			return zone;
		}
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response;
		html = "";
		InputStream in;
		try {
			response = client.execute(request);
			in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "windows-1252"));
			StringBuilder str = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				str.append(line);
			}
			in.close();
			html = str.toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		zone=parse(html);
		return zone;

	}

	public boolean hasConnection() {
		ConnectivityManager cm = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}

		return false;
	}
	
	public List<Zona> parse(String html){
		if(html.equals(""))
			return zone;
		try {
			JSONObject json= new JSONObject(html);
			JSONArray mjesta= json.getJSONArray("mjesta");
			if(mjesta!=null)
				for(int i=0;i<mjesta.length();i++){
					JSONObject obj=mjesta.getJSONObject(i);
					int zonaId=obj.getInt("zona_id");
					String vrijeme=obj.getString("vrijeme");
					int broj_mjesta=obj.getInt("broj_mjesta");
					for(int k=0;i<zone.size();k++){
						if(zone.get(k).getId()==zonaId){
							zone.get(k).setBrojMjesta(broj_mjesta);
							zone.get(k).setVrijeme(vrijeme);
							break;
						}
					}
				}
			return zone;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return zone;
	}
}
