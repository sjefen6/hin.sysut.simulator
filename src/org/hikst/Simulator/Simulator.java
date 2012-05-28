package org.hikst.Simulator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulator 
{	
	public static final String Simulator_Work_Status_High = "High";
	public static final String Simulator_Work_Status_Medium = "Medium";
	public static final String Simulator_Work_Status_Low = "Low";
	public static final String Simulator_Off = "Off";
	
	//public static final String Simulation_Status_Finished = "Finished";
	//public static final String Simulation_Status_Pending = "Pending";

	private final int Simulation_High_Limit;
	private final int Simulation_Low_Limit;
	private final int Number_Of_Threads_Per_Processor = 10;
	
	private boolean active;
	//private DatabaseConnection connection;
	
	public Simulator()
	{
		Runtime runTime = Runtime.getRuntime();
		this.Simulation_High_Limit = runTime.availableProcessors()*Number_Of_Threads_Per_Processor;
		this.Simulation_Low_Limit = Simulation_High_Limit*3/4;
		
		//start simulation thread....
		new Thread(new ImprovedSimulation()).start();
	}
	
	private class ImprovedSimulation implements Runnable
	{
		public void run()
		{	
			Settings settings = new Settings();
		
			int simulator_id = settings.getSimulatorID();
			
			if(!checkSimulatorIsRegistered(simulator_id))
			{
				registerSimulator(simulator_id);
			}
			
			while(active)
			{				
				validateSimulatorStatus(simulator_id);
				sleep();
				doSimulations(simulator_id);
			}
			
			updateSimulatorStatus(Simulator.Simulator_Off,simulator_id);
		}
		
	}
	
	public void doSimulations(int simulator_id)
	{
		int limit = Math.min(0,this.Simulation_High_Limit - Thread.activeCount());
		
		if(limit > 0)
		{
			ArrayList<SimulationRequest> requests = getSimulationRequests(simulator_id, limit);
		
			if(requests.size() > 0)
			{
				ExecutorService service = Executors.newFixedThreadPool(requests.size());
				
				for(int i = 0; i<requests.size(); i++)
				{
					Simulation simulation = new Simulation(requests.get(i));
					service.execute(simulation);
				}
				service.shutdown();	
			}
		}
		else
		{
			//write out message that the simulator has enough to do
		}
	}
	
	public void sleep()
	{
		//sleep 10000 minutes
		long sleepTime = 10000l;
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<SimulationRequest> getSimulationRequests(int simulator_id, int limit)
	{
		ArrayList<SimulationRequest> requests = new ArrayList<SimulationRequest>();
		
		try
		{
			Connection connection = Settings.getDBC();
			String query = "";
			PreparedStatement statement = connection.prepareStatement(query);
		
		}catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		
		
		//get requests from database 
		
		return requests;
	}
	
	public boolean checkSimulatorIsRegistered(int simulator_id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ID WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, simulator_id);
			ResultSet set = statement.executeQuery();
			
			return set.next();
			
		}catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		
		return false;
	}
	
	private void registerSimulator(int simulator_id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			int statusId = Status.getInstance().getStatusID(Simulator.Simulator_Work_Status_Low);
			
			InetAddress address = InetAddress.getLocalHost();
			String IP_adress = address.getHostAddress(); 
			String url = IP_adress;
			long last_seen_time = new java.util.Date().getTime();
			
			String query = "INSERT INTO Simulator(ID,Status_ID,IP_Adress,Last_Seen_TS,Url) VALUES(?,?,?,?);";
			PreparedStatement statement = connection.prepareStatement(query);
		
			statement.setInt(1, simulator_id);
			statement.setInt(2, statusId);
			statement.setString(3, IP_adress);
			statement.setLong(4, last_seen_time);
			statement.setString(5,url);
			
			statement.executeUpdate();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		catch(UnknownHostException ex)
		{
			ex.printStackTrace();
		}
		catch(StatusIdNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void validateSimulatorStatus(int simulatorId)
	{
		ThreadGroup threadRoot = Thread.currentThread().getThreadGroup();
		
		int activeThreadCount = threadRoot.activeCount();
		
		if(activeThreadCount > this.Simulation_High_Limit)
		{
			this.updateSimulatorStatus(Simulator.Simulator_Work_Status_High,simulatorId);
		}
		else if(activeThreadCount <= this.Simulation_High_Limit && activeThreadCount > this.Simulation_Low_Limit)
		{
			this.updateSimulatorStatus(Simulator.Simulator_Work_Status_Medium,simulatorId);
		}
		else
		{
			this.updateSimulatorStatus(Simulator_Work_Status_Low,simulatorId);
		}
	}
	
	private void updateSimulatorStatus(String status, int simulatorId)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			int statusId = Status.getInstance().getStatusID(status);
			String query = "UPDATE TABLE SIMULATOR SET StatusID=? WHERE ID=?;";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, statusId);
			statement.setInt(2, simulatorId);
			
			statement.executeUpdate();
			
		}catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		catch(StatusIdNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
			new Simulator();
	}
	
}
