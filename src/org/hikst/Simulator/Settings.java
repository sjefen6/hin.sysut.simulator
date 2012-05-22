package org.hikst.Simulator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {
	private Properties configFile;
	private File file;
	private String db_hostname, db_port, db_db, db_user, pd_pw, sim_id;
	
	public Settings(){
		this("Simulator.properties");
	}
	
	public Settings(String $filename){
		configFile = new Properties();
		file = new File($filename);
		
		load();
	}
	
	private void load(){
		try {
			configFile.load(new FileReader(file));
		} catch (IOException e) {
//			configFile.setProperty(key, value);
			configFile.store(new FileWriter(file), null)
			System.err.println("Klarte ikke Œ Œpne " + file.getAbsolutePath() + ".");
			e.printStackTrace();
		}
		
		db_hostname = configFile.getProperty("DB_HOSTNAME");
		db_port = configFile.getProperty("DB_PORT");
		db_db = configFile.getProperty("DB_DB");
		db_user = configFile.getProperty("DB_USER");
		pd_pw = configFile.getProperty("DB_PW");
		sim_id = configFile.getProperty("SIM_ID");
	}

}
