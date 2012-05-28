package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImpactFactor 
{
	public static final int IMPACT_WEATHER = 1;
	
	private int type_id;
	private String content;
	
	public ImpactFactor(int id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1,id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.type_id = set.getInt(1);
				this.content = set.getString(2);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	private class ImpactParser
	{
		public float parseScale(int type,String content)
		{
			//parse content here
			switch(type)
			{
				case IMPACT_WEATHER:
				{
					return parseWeatherInformation(content);
				}
				default:
				{
					return Float.NaN;
				}
			}
			
		}
		
		private float parseWeatherInformation(String content)
		{
			return Float.NaN;
		}
		
	}
}
