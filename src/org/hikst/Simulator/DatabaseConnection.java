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
	
	
	public ArrayList<SimulationRequest> getSimulationRequests(String status)
	{
		ArrayList<SimulationRequest> requests = new ArrayList<SimulationRequest>();
		
		try
		{
			PreparedStatement statement = connection.prepareStatement("select simulator_queue_object.queue_id," +
					" simulator_queue_object.simulation_id," +
					"sim_descriptions_id, sim_parts_queue_id, crawler_queue_id from simulator_queue_object,crawler_dependency, simulator_dependency" +
					"where queue_status=? and crawler_dependency.sim_queue_id = queue_id and simulations_dependency.sim_queue_id = queue_id");
			statement.setString(1, status);
			
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int queue_id = set.getInt(1);
				int simulation_id = set.getInt(2);
				int simulation_descriptions_id = set.getInt(3);
				int simulation_dependency = set.getInt(4);
				int crawler_dependency = set.getInt(5);
				
				SimulationRequest request = new SimulationRequest(queue_id,simulation_id,simulation_dependency,crawler_dependency,simulation_descriptions_id);
				requests.add(request);
			}
		}
		catch(SQLException ex)
		{
			
		}
		
		return requests;
	}
	
	public ArrayList<SimulationRequest> getSimulationRequests(String status,int simulatorID)
	{
		ArrayList<SimulationRequest> requests = new ArrayList<SimulationRequest>();
		
		try
		{
			PreparedStatement statement = connection.prepareStatement("select simulator_queue_object.queue_id," +
					" simulator_queue_object.simulation_id," +
					"sim_descriptions_id, sim_parts_queue_id, crawler_queue_id from simulator_queue_object,crawler_dependency, simulator_dependency" +
					"where queue_status=? and simulator_id=? and crawler_dependency.sim_queue_id = queue_id and simulations_dependency.sim_queue_id = queue_id");
			statement.setString(1, status);
			statement.setInt(2, simulatorID);
			
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int queue_id = set.getInt(1);
				int simulation_id = set.getInt(2);
				int simulation_descriptions_id = set.getInt(3);
				int simulation_dependency = set.getInt(4);
				int crawler_dependency = set.getInt(5);
				
				SimulationRequest request = new SimulationRequest(queue_id,simulation_id,simulation_dependency,crawler_dependency,simulation_descriptions_id);
				requests.add(request);
			}
		}
		catch(SQLException ex)
		{
			
		}
		
		return requests;
	}
	
	public ArrayList<CrawlerRequest> getCrawlerRequests(String status)
	{
		ArrayList<CrawlerRequest> requests = new ArrayList<CrawlerRequest>();
		
		try
		{
			PreparedStatement statement = connection.prepareStatement("select queue_id, queue_type, latitude, longitude, timeframe from crawler_queue_object where queue_status=?");
			statement.setString(1,status);
			
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				//get data..
				//first alter column timeframe from bigint to date in phpPhAdmin
			}
			
		}catch(SQLException ex)
		{
			
		}
		
		return requests;
	}

	/*public SimulationDescription getSimulationDescription(int simulationDescriptionID)
	{
		try {
			PreparedStatement statement = connection.prepareStatement("select id, object_id, impact_id, timeIntervall, minimumTime,maximumTime ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
