package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.hikst.Commons.Services.Settings;

public class CrawlerDependency 
{
	private ArrayList<Integer> crawlerDependencies = new ArrayList<Integer>();
	
	public ArrayList<Integer> getCrawlerDependencies() {
		return crawlerDependencies;
	}

	public CrawlerDependency(int Simulator_Queue_Object_ID)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT Crawler_Queue_ID, Sim_Queue_ID FROM Crawler_Dependency WHERE Sim_Queue_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, Simulator_Queue_Object_ID);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int Crawler_Queue_ID = set.getInt(1);
				
				crawlerDependencies.add(Crawler_Queue_ID);
			}
		}
		catch(SQLException ex)
		{
			//error message
		}
	}
}
