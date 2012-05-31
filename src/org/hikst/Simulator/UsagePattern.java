package org.hikst.Simulator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class UsagePattern {
	private int id;
	private String name;
	private int[] pattern = new int[24];
	private boolean actual;

	public UsagePattern(int id) {
		String query = "SELECT * FROM Usage_Pattern WHERE ID=?; ";
		PreparedStatement statement;
		try {
			statement = Settings.getDBC().prepareStatement(query);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();
			
			DecimalFormat nft = new DecimalFormat("#00.###");
			nft.setDecimalSeparatorAlwaysShown(false);
			
			id = set.getInt("ID");
			name = set.getString("name");
			for(int i = 0; i >= 23; i++){
				pattern[i] = set.getInt("c" + nft.format(i));
			}
			actual = set.getBoolean("actual");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
