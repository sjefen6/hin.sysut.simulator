package org.hikst.Simulator;

import java.util.Date;

public class sjefen6_tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Settings();
		// TODO Auto-generated method stub
		try {
			UsagePattern u = new UsagePattern(1);
			for (int i = 0; i < 6; i++) { 
				System.out.println(
						"Name: " + u.getName() +
						"\nId: " + u.getId() + 
						"\nPattern: " + u.getProbability(new Date())
						);
			}
		} catch (UsagePatternNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
