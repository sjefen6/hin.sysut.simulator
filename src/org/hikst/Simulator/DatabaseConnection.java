package org.hikst.Simulator;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection 
{
	private Connection connection;
	
	public DatabaseConnection() 
	{
		connect();
	}
	
	private void connect()
	{	
		String brukernavn = "sysut";
		String passord = "password";
		String url = "jdbc:postgresql://localhost:5432/db";
	
		try {
			
			try {
				Class.forName("org.postgresql.Driver").newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			
			connection = 
				DriverManager.getConnection(url,brukernavn,passord);
			
			if (!connection.isClosed())
                System.out.println("Hooked up to database-server");
			else
				System.out.println("Not hooked up to database-server");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SimulatorObject getSimulatorObject(int ID)
	{
		try
		{
			PreparedStatement statement = connection.prepareStatement("select object_name, effect, impact_degree_id from objects where ID=?");
			statement.setInt(1, ID);
			ResultSet resultSet = statement.executeQuery();
			
			String name = "Empty";
			String effect = "0.0";
			
			if(resultSet.next())
			{
			    name = resultSet.getString(1);
				effect = resultSet.getString(2);
				//int impact_id = resultSet.getInt(3);
			}
			
			
			return new SimulatorObject(ID,name,Float.parseFloat(effect));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}
	


	public boolean addResults(SimulationResult results)
	{
		try
		{			
			float[] result = results.getSimulations();
			
			for(int i = 0; i<result.length; i++)
			{
				PreparedStatement statement = connection.prepareStatement("insert into simulations VALUES(time,effect, sim_descriptions_id)(?,?,?);");
				
				float time = results.getDescription().getTimeStart() + results.getDescription().getInterval()*i;
				
				statement.setString(1, String.valueOf(results.getEffect(time)));
				statement.setString(2, String.valueOf(time));
				statement.setInt(3, results.getDescription().getID());
			
				statement.execute();
			}
			
			return true;
		}catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
		
		
	}
}
