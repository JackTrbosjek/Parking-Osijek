package hr.jakov.parkingosijek.baza;

import hr.jakov.parkingosijek.R;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	
	private static final String DATABASE_NAME = "parking.db";

	private static final int DATABASE_VERSION = 1;

	

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Vozilo.class);
			TableUtils.createTable(connectionSource, Zona.class);
			TableUtils.createTable(connectionSource, Koordinata.class);
			TableUtils.createTable(connectionSource, Povjest.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Vozilo.class, true);
			TableUtils.dropTable(connectionSource, Zona.class, true);
			TableUtils.dropTable(connectionSource, Koordinata.class, true);
			TableUtils.dropTable(connectionSource, Povjest.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Tablica registracija i naziva vozila
	 */
	private Dao<Vozilo, Integer> voziloDao = null;
	private RuntimeExceptionDao<Vozilo, Integer> voziloRuntimeDao = null;
	
	public Dao<Vozilo, Integer> getVoziloDao() throws SQLException {
		if (voziloDao == null) {
			voziloDao = getDao(Vozilo.class);
		}
		return voziloDao;
	}

	
	public RuntimeExceptionDao<Vozilo, Integer> getVoziloDataDao() {
		if (voziloRuntimeDao == null) {
			voziloRuntimeDao = getRuntimeExceptionDao(Vozilo.class);
		}
		return voziloRuntimeDao;
	}
	/**
	 * Tablica zona i koordinata
	 */
	private Dao<Zona, Integer> zonaDao = null;
	private RuntimeExceptionDao<Zona, Integer> zonaRuntimeDao = null;
	
	public Dao<Zona, Integer> getZonaDao() throws SQLException {
		if (zonaDao == null) {
			zonaDao = getDao(Zona.class);
		}
		return zonaDao;
	}

	public RuntimeExceptionDao<Zona, Integer> getZonaDataDao() {
		if (zonaRuntimeDao == null) {
			zonaRuntimeDao = getRuntimeExceptionDao(Zona.class);
		}
		return zonaRuntimeDao;
	}
	
	private Dao<Koordinata, Integer> koordinataDao = null;
	private RuntimeExceptionDao<Koordinata, Integer> koordinataRuntimeDao = null;
	
	public Dao<Koordinata, Integer> getKoordinataDao() throws SQLException {
		if (koordinataDao == null) {
			koordinataDao = getDao(Koordinata.class);
		}
		return koordinataDao;
	}

	public RuntimeExceptionDao<Koordinata, Integer> getKoordinataDataDao() {
		if (koordinataRuntimeDao == null) {
			koordinataRuntimeDao = getRuntimeExceptionDao(Koordinata.class);
		}
		return koordinataRuntimeDao;
	}
	
	/**
	 * Tablica povjesti parkinga
	 */
	private Dao<Povjest, Integer> povjestDao = null;
	private RuntimeExceptionDao<Povjest, Integer> povjestRuntimeDao = null;
	
	public Dao<Povjest, Integer> getPovjestDao() throws SQLException {
		if (povjestDao == null) {
			povjestDao = getDao(Povjest.class);
		}
		return povjestDao;
	}

	public RuntimeExceptionDao<Povjest, Integer> getPovjestDataDao() {
		if (povjestRuntimeDao == null) {
			povjestRuntimeDao = getRuntimeExceptionDao(Povjest.class);
		}
		return povjestRuntimeDao;
	}
	/**
	 *Zatvaranje baze;
	 */
	@Override
	public void close() {
		super.close();
		voziloDao = null;
		voziloRuntimeDao = null;
	}
}
