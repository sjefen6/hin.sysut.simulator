package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hikst.Commons.Services.Settings;

public class ImpactDegrees
{
	float percent;
	int type;
	private Integer objectId;
	
	//TODO: Fix class to also use impactFactor	
	
	public float getPercent() {
		return percent;
	}
	
	public void setPercent(float percent) {
		this.percent = percent;
	}
	
	public float getType() {
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
	

}
