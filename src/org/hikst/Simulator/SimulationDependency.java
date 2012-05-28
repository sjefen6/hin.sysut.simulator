package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SimulationDependency 
{
	private ArrayList<Integer> dependencies = new ArrayList<Integer>();
	
	public ArrayList<Integer> getDependencies() {
		return dependencies;
	}

	public SimulationDependency(int Simulator_Queue_Object_ID)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT Son_ID FROM Simulation_Dependency WHERE Father_ID=?;";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, Simulator_Queue_Object_ID);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int Son_ID = set.getInt(1);
				dependencies.add(Son_ID);
			}
		}
		catch(SQLException ex)
		{
			
		}
	}
}
