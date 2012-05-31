package org.hikst.Simulator;

//import java.net.InetAddress;
//import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.ResultSet;
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
	
	private boolean active = true;
	//private DatabaseConnection connection;
	
	public Simulator()
	{
		Runtime runTime = Runtime.getRuntime();
		this.Simulation_High_Limit = runTime.availableProcessors()*Number_Of_Threads_Per_Processor;
		this.Simulation_Low_Limit = Simulation_High_Limit*3/4;
		
		System.out.println("Starting simulator-thread...");
		new Thread(new ImprovedSimulation()).start();
	}
	
	private class ImprovedSimulation implements Runnable
	{
		public void run()
		{	
			int simulator_id = Settings.getSimulatorID();
			System.out.println("My simulator id is :"+simulator_id);
			
			while(active)
			{				
				validateSimulatorStatus(simulator_id);
				sleep();
				doSimulations(simulator_id);
			}
			
			System.out.println("Turning off simulator...");
			updateSimulatorStatus(Simulator.Simulator_Off,simulator_id);
		}
		
	}
	
	public void doSimulations(int simulator_id)
	{
		System.out.println("Number of simulation threads running: "+Math.max(Thread.activeCount()-2,0));
		int limit = Math.max(0,this.Simulation_High_Limit - (Thread.activeCount() - 2));
		
		if(limit > 0)
		{	
			System.out.println("Can receive a workload of "+limit+" requests from database");
			System.out.println("Downloads a maximum of "+limit+" requests from database...");
			ArrayList<SimulationRequest> requests = getSimulationRequests(simulator_id, limit);
			
			if(requests.size() > 0)
			{
				System.out.println("Number of simulation requests recieved from database: "+requests.size());
				System.out.println("Starting "+requests.size()+" new simulation threads..");
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
			System.out.println("Workload too high.. Cancelling downloading of new requests");
		}
	}
	
	public void sleep()
	{
		//sleep 10000 milliseconds
		System.out.println("Waiting 1000 milliseconds...");
		long sleepTime = 1000l;
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
			System.out.println("Downloading...");
			ArrayList<Integer> requestIDs = new ArrayList<Integer>();
			
			int statusId = Status.getInstance().getStatusID(SimulationRequest.Request_Pending);
			
			Connection connection = Settings.getDBC();
			String query = "SELECT ID FROM " +
					"Simulator_Queue_Objects WHERE Status_ID=? AND Simulator_ID=? limit ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, statusId);
			statement.setInt(2, simulator_id);
			statement.setInt(3, limit);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				requestIDs.add(set.getInt(1));
			}
			
			statement.close();
			System.out.println("Received "+requestIDs.size()+" requests");
			
			for(int i = 0; i<requestIDs.size(); i++)
			{
				try {
					SimulationRequest request = new SimulationRequest(requestIDs.get(i));
					requests.add(request);
				} catch (ObjectNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("Download successful");
			
		}catch(SQLException ex)
		{	
			System.out.println("Download failed");
			ex.printStackTrace();
		} catch (StatusIdNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("The ID to the status \""+SimulationRequest.Request_Pending+"\" was not found");
			e.printStackTrace();
		}
		
		
		//get requests from database 
		
		return requests;
	}
	
	/*public boolean checkSimulatorIsRegistered(int simulator_id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{	
			String query = "SELECT ID FROM Simulator WHERE ID=?";
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
			
			String query = "INSERT INTO Simulator(ID,Status_ID,IP_Adress,Last_Seen_TS,Url) VALUES(?,?,?,?,?);";
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
	}*/
	
	private void validateSimulatorStatus(int simulatorId)
	{
		System.out.println("Validating simulator-status...");
		
		ThreadGroup threadRoot = Thread.currentThread().getThreadGroup();
		
		int activeThreadCount = threadRoot.activeCount();
		
		if(activeThreadCount > this.Simulation_High_Limit)
		{
			System.out.print("The workload is too high, update simulator-status to database..");
			this.updateSimulatorStatus(Simulator.Simulator_Work_Status_High,simulatorId);
		}
		else if(activeThreadCount <= this.Simulation_High_Limit && activeThreadCount > this.Simulation_Low_Limit)
		{
			System.out.println("The workload is average, update simulator-status to database..");
			this.updateSimulatorStatus(Simulator.Simulator_Work_Status_Medium,simulatorId);
		}
		else
		{
			System.out.println("The workload is low, update simulator-status to database..");
			this.updateSimulatorStatus(Simulator_Work_Status_Low,simulatorId);
		}
	}
	
	private void updateSimulatorStatus(String status, int simulatorId)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			System.out.println("Uploading status to database...");
			int statusId = Status.getInstance().getStatusID(status);
			String query = "UPDATE Simulator SET status_id=? WHERE ID=?;";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, statusId);
			statement.setInt(2, simulatorId);
			
			statement.executeUpdate();
			
		}catch(SQLException ex)
		{
			System.out.println("Uploading status to database failed");
			ex.printStackTrace();
		}
		catch(StatusIdNotFoundException ex)
		{
			System.out.println("Status id not found for the status: \""+status+"\" in the database");
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
			new Settings();
			AliveMessenger.getInstance();
			new Simulator();
	}
	
}
