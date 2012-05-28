package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Impact_Degrees
{
	float percent;
	int type;
	
	public float getPercent() {
		return percent;
	}
	
	public void setPercent(float percent) {
		this.percent = percent;
	}
	
	public float getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	
	public Impact_Degrees(float percent, int type,String content) {
		super();
		this.percent = percent;
		this.type = type;
	}
	
	public Impact_Degrees(int id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT Percent, Type_ID FROM Impact_Degrees WHERE ID = ?;";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();
		
			if(set.next())
			{
				this.percent = set.getFloat(1);
				this.type = set.getInt(2);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	

}
