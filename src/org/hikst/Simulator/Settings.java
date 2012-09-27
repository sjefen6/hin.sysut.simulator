package org.hikst.Simulator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class Settings {
	private static Properties configFile;
	private static File file;
	private static String db_hostname, db_port, db_db, db_user, db_pw;
	private static Connection dbc;
	
	/**
	 * Constructor that creates a default name for the file.
	 */
	public Settings(){
		this("Simulator.properties");
	}
	
	/**
	 * @param $filename
	 * 
	 * Constructor with input on file name.
	 */
	public Settings(String $filename){
		configFile = new Properties();
		file = new File($filename);
		
		load();
	}
	
	/**
	 * Creates a default config file with blank properties.
	 */
	private void writeDefaultConfig(){
		configFile.setProperty("DB_HOSTNAME", "localhost");
		configFile.setProperty("DB_PORT", "5432");
		configFile.setProperty("DB_DB", "db");
		configFile.setProperty("DB_USER", "user");
		configFile.setProperty("DB_PW", "password");
		
		try {
			configFile.store(new FileWriter(file),null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Loads the config file that is specified in the Property <I>configFile</I>.
	 * If it cannot find the file, it will ask to create a new default file with <I>writeDefaultConfig()</I>.
	 */
	private void load(){
		try {
			configFile.load(new FileReader(file));
			
			db_hostname = configFile.getProperty("DB_HOSTNAME");
			db_port = configFile.getProperty("DB_PORT");
			db_db = configFile.getProperty("DB_DB");
			db_user = configFile.getProperty("DB_USER");
			db_pw = configFile.getProperty("DB_PW");
		} catch (IOException e) {
			// TODO: ¯nsker vi Œ kreve brukerinput i denne klassen?
			System.err.println("Unable to open " + file.getAbsolutePath() + ".\nCreate default config at this location? [y/n]");
			Scanner scan = new Scanner(System.in);
			if(scan.nextLine().equalsIgnoreCase("y")){
				writeDefaultConfig();
			}
			System.err.println("Retry loading config? [y/n]");
			if(scan.nextLine().equalsIgnoreCase("y")){
				load();
			} else {
				System.exit(0);
			}
		}
	}
	
	/**
	 * Get a connection to the database.
	 * 
	 * @return Database connection or null
	 */
	public static Connection getDBC(){
		if (openDatabaseConnection()){
			return dbc;
		}
		return null;
	}
	
	/**
	 * Opens connection to the database.
	 * 
	 * @return True if database connnection is up, false if connection is down.
	 */
	private static boolean openDatabaseConnection(){
		try {
			if (!dbc.isClosed()){
				return true;
			}
		} catch (Exception e) {
			// This is not unexpected and will always fail before the dbc is created.
			//Please just continue!
		}
		
		try {
			dbc = DriverManager.getConnection("jdbc:postgresql://" + db_hostname + ":" + db_port + "/" + db_db,db_user,db_pw);
			return !dbc.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Gets this simulator's ID.
	 * 
	 * @return The result given from the configFile. If this is a new simulator, a simulator without ID, it will return -1.
	 */
	public static int getSimulatorID(){
		int result;
		try {
			result = Integer.parseInt(configFile.getProperty("SIMULATOR_ID"));
		} catch (NumberFormatException ex) {
            return -1;
        }
		return result;
	}
	
	/**
	 * Sets this simulator's ID. Will only be called by the AliveMessenger class after the first reporting.
	 * 
	 * @param id The ID given to the simulator instance by the database.
	 */
	public static void setSimulatorID(int id){
		configFile.setProperty("SIMULATOR_ID", Integer.toString(id));
		try {
			configFile.store(new FileWriter(file),null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
