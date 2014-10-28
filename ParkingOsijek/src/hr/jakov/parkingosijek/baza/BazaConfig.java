package hr.jakov.parkingosijek.baza;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class BazaConfig  extends OrmLiteConfigUtil {
	private static final Class<?>[] classes = new Class[] {Vozilo.class,Koordinata.class,Zona.class,Povjest.class};
	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("ormlite_config.txt",classes);
	}
}
