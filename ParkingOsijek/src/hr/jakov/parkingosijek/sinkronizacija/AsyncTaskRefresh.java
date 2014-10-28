package hr.jakov.parkingosijek.sinkronizacija;

import hr.jakov.parkingosijek.R;
import hr.jakov.parkingosijek.ViewPagerActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

public class AsyncTaskRefresh extends AsyncTask<String, Void, String> {

	private String url;
	private Context c;
	private ProgressDialog progress;
	private ViewPagerActivity a;
	

	public AsyncTaskRefresh(ViewPagerActivity a) {
		this.c = a;
		this.a = a;
		this.url = new String("http://jakov-videkovic.from.hr/parking/json.php");

	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if(result.equals("fail")){
			Toast.makeText(
					c,
					"Provjerite vezu s internetom te pokušajte ponovno!\nSinkronizacija neuspješna.",
					Toast.LENGTH_LONG).show();
		}
		progress.dismiss();
		a.postaviAdapter();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress = new ProgressDialog(c);
		progress.setMessage(c.getResources().getString(R.string.ucitavanje));
		progress.show();
	}

	protected String doInBackground(String... parametri) {
		// System.out.println(hasConnection());
		String html = "";
		if (parametri[0].equals("first")) {
			try {
				InputStream is=c.getAssets().open("json.txt");
				int size = is.available();
				byte[] buffer;buffer = new byte[size];
				is.read(buffer);
				is.close();
				html = new String(buffer);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (!hasConnection()) {
				
				return "fail";
			}
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response;
			html = "";
			InputStream in;
			try {
				response = client.execute(request);
				in = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, "windows-1252"));
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
		}
		JSONclass.parsaj(html, c);
		return html;

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
