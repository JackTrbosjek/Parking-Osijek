package hr.jakov.parkingosijek.sinkronizacija;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

public class AsyncTaskUnosMjesta extends AsyncTask<String, Void, String> {

	private String url;
	private Context c;
	

	public AsyncTaskUnosMjesta(Context c) {
		this.c = c;
		this.url = new String("http://jakov-videkovic.from.hr/parking/unos_mjesta.php");

	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if(result.equals("fail")){
			Toast.makeText(
					c,
					"Neuspješno poslan broj slobodnih mjesta.",
					Toast.LENGTH_LONG).show();
		}else if(result.equals("success")){
			Toast.makeText(
					c,
					"Uspješno poslan broj slobodnih mjesta.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	protected String doInBackground(String... parametri) {
			String odgovor="fail";
			if (!hasConnection()) {
				
				return odgovor;
			}
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post= new HttpPost(url);
			HttpResponse response;
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("zona_id", parametri[0]));
	        nameValuePairs.add(new BasicNameValuePair("mjesta", parametri[1]));
	        
			InputStream in;
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				in = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, "windows-1252"));
				StringBuilder str = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					str.append(line);
				}
				in.close();
				odgovor = str.toString();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return odgovor;

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
}