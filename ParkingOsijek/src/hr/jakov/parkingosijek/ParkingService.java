package hr.jakov.parkingosijek;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import hr.jakov.parkingosijek.baza.DatabaseHelper;
import hr.jakov.parkingosijek.baza.Povjest;
import hr.jakov.parkingosijek.baza.Vozilo;
import hr.jakov.parkingosijek.tools.NotificationActivity;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class ParkingService extends Service {
	public static final String KEY_VOZILO = "hr.jakov.parkingosijek.key_vozilo";
	public static final String KEY_BROJ = "hr.jakov.parkingosijek.key_broj";
	public static final String KEY_ZONA = "hr.jakov.parkingosijek.key_zona";
	public static final String KEY_SAT = "hr.jakov.parkingosijek.key_sat";
	public static final String KEY_MIN = "hr.jakov.parkingosijek.key_min";
	public static final String KEY_ID = "hr.jakov.parkingosijek.key_id";
	public static final String ACTION_SMS_SEND = "hr.jakov.parkingosijek.action_sms_send";
	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_KRAJ = "hr.jakov.parkingosijek.kraj_parkiranja";
	private List<Vozilo> vozila;
	private NotificationManager nManager;
	private DatabaseHelper dbHelper;

	public ParkingService() {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getApplication().unregisterReceiver(receiver);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		dbHelper = OpenHelperManager.getHelper(getApplicationContext(),
				DatabaseHelper.class);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SMS_SEND);
		filter.addAction(ACTION_SMS_RECEIVED);
		filter.addAction(ACTION_KRAJ);
		getApplication().registerReceiver(receiver, filter);
		nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		try {
			vozila = dbHelper.getVoziloDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String broj = intent.getStringExtra(KEY_BROJ);
			Vozilo vozilo = (Vozilo) intent.getSerializableExtra(KEY_VOZILO);
			sendSMS(broj, vozilo);
		}

		return Service.START_NOT_STICKY;
	}

	private void sendSMS(String broj, Vozilo vozilo) {
		SmsManager send = SmsManager.getDefault();
		Intent i = new Intent(ACTION_SMS_SEND);
		i.putExtra(KEY_VOZILO, vozilo);
		PendingIntent piSend = PendingIntent.getBroadcast(
				getApplicationContext(), 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		send.sendTextMessage(broj, null, vozilo.getRegistracija(), piSend, null);
	}

	protected void checkSendSMS(int code, Vozilo vozilo) {
		if (code == Activity.RESULT_OK) {
			Toast.makeText(
					getApplicationContext(),
					getResources().getString(R.string.serviceSendOK)
							+ vozilo.getNaziv(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.serviceSendFail,
					Toast.LENGTH_SHORT).show();
			stopSelf();
		}
	}

	protected void smsReceived(String broj, String poruka) {
		String prvi = broj.substring(0, 5);
		int duzina = broj.length();
		if (prvi.equals("70831") && duzina == 18) {
			int i = poruka.indexOf(":");
			String satS = poruka.substring(i - 2, i);
			String minS = poruka.substring(i + 1, i + 3);
			int sat = -1, min = -1;
			int zona=getZona(broj);
			try {
				sat = Integer.parseInt(satS);
				min = Integer.parseInt(minS);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			Vozilo v = null;
			for (Vozilo z : vozila) {
				if (poruka.contains(z.getRegistracija() + ""))
					v = z;
			}
			if (v != null) {
				notifikacijaPocetak(sat, min, v, zona);
				setAlarm(sat, min, v, zona);
			}
		}
	}
	private int getZona(String broj){
		broj=broj.substring(0, 6);
		if(broj.equals("708311"))
			return 1;
		if(broj.equals("708312"))
			return 2;
		if(broj.equals("708313"))
			return 3;
		return 0;
	}

	private void setAlarm(int sat, int min, Vozilo vozilo, int zona) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, sat);
		c.set(Calendar.MINUTE, min);
		unesiPovjest(c, vozilo, zona);
		long when = System.currentTimeMillis();
		// parking završava danas
		if (c.compareTo(Calendar.getInstance()) == 1) {
			when = c.getTimeInMillis();
		} else {
			// parking završava drugi dan, kraj notifikacije za 30min
			when += 1000 * 60 * 30;
		}
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(ACTION_KRAJ);
		i.putExtra(KEY_ZONA, zona);
		i.putExtra(KEY_VOZILO, vozilo);
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),
				0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, when, pi);
	}

	private void unesiPovjest(Calendar c, Vozilo vozilo, int zona) {
		if (zona != 0) {
			try {
				dbHelper.getPovjestDao().create(
						new Povjest(vozilo, System.currentTimeMillis(), c
								.getTimeInMillis(), zona));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void notifikacijaPocetak(int sat, int min, Vozilo vozilo,
			int zona) {
		Intent i = new Intent(getApplicationContext(),
				NotificationActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(KEY_SAT, sat);
		i.putExtra(KEY_MIN, min);
		i.putExtra(KEY_ZONA, zona);
		i.putExtra(KEY_VOZILO, vozilo);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		DecimalFormat df= new DecimalFormat("00");
		NotificationCompat.Builder build = new NotificationCompat.Builder(
				getApplicationContext())
				.setContentTitle("Parking do " + df.format(sat) + ":" + df.format(min))
				.setContentText(
						"Vozilo: " + vozilo.getNaziv() + " ("
								+ vozilo.getRegistracija() + ")")
				.setWhen(System.currentTimeMillis()).setContentIntent(pi)
				.setOngoing(true).setSmallIcon(R.drawable.ic_launcher)
				.setTicker("Parking");
		nManager.notify(vozilo.getId(), build.build());

	}

	protected void krajParkinga(Vozilo vozilo, int zona) {
		Intent i = new Intent(getApplicationContext(),
				NotificationActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(KEY_ZONA, zona);
		i.putExtra(KEY_VOZILO, vozilo);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder build = new NotificationCompat.Builder(
				getApplicationContext())
				.setContentTitle("Parking završio")
				.setContentText(
						"Vozilo: " + vozilo.getNaziv() + " ("
								+ vozilo.getRegistracija() + ")")
				.setWhen(System.currentTimeMillis()).setContentIntent(pi)
				.setOngoing(false).setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setTicker("Kraj");
		nManager.notify(vozilo.getId(), build.build());
		zvuk();
	}

	private void zvuk() {
		SoundPool sp = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 1);
		sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				if (status == 0)
					soundPool.play(sampleId, 1, 0, 999, 0, 1);
			}
		});
		sp.load(getApplicationContext(), R.raw.sound, 999);
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_SMS_SEND)) {
				Vozilo v = (Vozilo) intent.getSerializableExtra(KEY_VOZILO);
				checkSendSMS(getResultCode(), v);
			}
			if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
				Bundle pudsBundle = intent.getExtras();
				Object[] pdus = (Object[]) pudsBundle.get("pdus");
				SmsMessage messages = SmsMessage
						.createFromPdu((byte[]) pdus[0]);
				smsReceived(messages.getOriginatingAddress(),
						messages.getMessageBody());
			}
			if (intent.getAction().equals(ACTION_KRAJ)) {
				Vozilo vozilo = (Vozilo) intent
						.getSerializableExtra(KEY_VOZILO);
				int zona = intent.getIntExtra(KEY_ZONA,0);
				krajParkinga(vozilo, zona);
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
