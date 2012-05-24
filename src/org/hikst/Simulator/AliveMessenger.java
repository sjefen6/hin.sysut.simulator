package org.hikst.Simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Timer;

public class AliveMessenger extends AbstractAction {

	public AliveMessenger() {
		new Timer(2000, (ActionListener) this).start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println(getIp());
	}

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

	private void update(String ip){
		String query = "";
		if (Settings.getSimulatorID() == -1){
			// Insert
			query = "";
			// Will also need to
//			Settings.setSimulatorID(id);
		}else {
			query = "UPDATE ";
		}
		
//		Settings.getDBC()
	}
}
