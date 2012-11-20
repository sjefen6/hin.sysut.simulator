package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.hikst.Commons.Datatypes.Object;

import org.hikst.Commons.Services.Settings;

public class ImpactDegrees
{
	float percent;
	int type;
	private Integer objectId;
	
	public float getPercent() {
		return percent;
	}
	
	public void setPercent(float percent) {
		this.percent = percent;
	}
	
	public int getType() {
		return type;
	}
	
	public int getObjectId()
	{
		return objectId;
	}	
	
	public void setType(int type) {
		this.type = type;
	}
	
	
	public ImpactDegrees (int type,float percent,int oid) {
		super();
		this.percent = percent;
		this.type = type;
		this.objectId = oid;
	}
	
	public ImpactDegrees(int id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Impact_Degrees WHERE object_id=?;";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();
		
			if(set.next())
			{
				this.type = set.getInt(1);
				this.percent = set.getFloat(2);
				this.objectId = set.getInt(3);
			}			
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public ArrayList<ImpactFactor> getFactors(int latitude, int longitude, int time_from, int time_to){
		Connection connection = Settings.getDBC();
		
		ArrayList<ImpactFactor> factors = new ArrayList<ImpactFactor> ();
		
		try {
			String query = "SELECT * FROM Impact_Factors WHERE latitude >= ?"
					+ "AND latitude <= ?" + "AND longitude >= ?"
					+ "AND longitude <= ?" + "AND Time_From <= ?"
					+ "AND Time_To >= ? AND Type_ID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, (int) latitude - 1000);
			statement.setInt(2, (int) latitude + 1000);
			statement.setInt(3, (int) longitude - 1000);
			statement.setInt(4, (int) longitude + 1000);
			statement.setInt(5, (int) time_to);
			statement.setInt(6, (int) time_from);
			statement.setInt(7, type);
			ResultSet set = statement.executeQuery();
			
			while (set.next()){
				Factor f = new Factor();
				
				
				factors.add(new ImpactFactor(set.getInt("id")));
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return factors;
	}

}
