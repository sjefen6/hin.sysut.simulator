package org.hikst.Simulator;

/**
 * AliveMessenger
 * Reports to the database every minute that it is alive and running.
 * Make sure settings are correctly set up before using.
 * Make sure this class gets loaded by the main tread.
 * The use of timer seem to take care of the rest.
 * 
 * Example:
 * public static void main(String[] args) {
 * 	new Settings();
 * 	AliveMessenger.getInstance();
 * }
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class AliveMessenger extends AbstractAction {
	private static AliveMessenger _instance = new AliveMessenger();
	private final int INTERVAL = 60000;
	private int status_id;

	private AliveMessenger() {
		// Status can not be null. Assuming this is set before any simulations has started, therefor load = low
		try {
			status_id = Status.getInstance().getStatusID(Simulator.Simulator_Work_Status_Low);
		} catch (StatusIdNotFoundException e) {
			e.printStackTrace();
			// I am a vengeful bitch
			System.exit(0);
		}
		// Make sure update is run once before the timer starts
		update();
		// Start updating status on an INTERVAL
		new Timer(INTERVAL, (ActionListener) this).start();
	}
	
	// Returning the singleton
    public static AliveMessenger getInstance() {
        return _instance;
    }

    // Overriding for timer
	@Override
	public void actionPerformed(ActionEvent arg0) {
		update();
	}

	// Returns the current external ip determend by a 3rd party.
	private String getIp() {
		try {
			URL url = new URL("http://ip.goldclone.no/");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			return in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void update(){
		String query = "";
		if (Settings.getSimulatorID() == -1){
			// Insert
			query = "INSERT INTO simulator(" +
					"status_id, " + 
					"ip_adress, " +
					"last_seen_ts, " +
					"url) " +
					"VALUES(?,?,extract(epoch from now()),NULL) RETURNING *";
			try {
				PreparedStatement statement = Settings.getDBC().prepareStatement(query);
				statement.setInt(1, status_id);
				statement.setString(2, getIp());
				ResultSet res = statement.executeQuery();
				res.next();
				Settings.setSimulatorID(res.getInt("id"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else {
			// Update
			query = "UPDATE simulator SET " +
					"status_id = ?, " +
					"ip_adress = ?, " +
					"last_seen_ts = extract(epoch from now()), " +
					"url = NULL " + 
					"WHERE id = ?";
			try {
				PreparedStatement statement = Settings.getDBC().prepareStatement(query);
				statement.setInt(1, status_id);
				statement.setString(2, getIp());
				statement.setInt(3, Settings.getSimulatorID());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setStatus(String status) throws StatusIdNotFoundException{
		status_id = Status.getInstance().getStatusID(status);
	}
}
