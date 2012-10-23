package org.hikst.Crawler;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hikst.Commons.Exceptions.CollectorRequestNotFoundException;
import org.hikst.Commons.Exceptions.StatusIdNotFoundException;
import org.hikst.Commons.Services.AliveMessenger;
import org.hikst.Commons.Services.Settings;
import org.hikst.Commons.Statics.Status;

public class Collector extends KeyAdapter {

	public static final String Collector_Work_Status_High = "High";
	public static final String Collector_Work_Status_Medium = "Medium";
	public static final String Collector_Work_Status_Low = "Low";
	public static final String Collector_Off = "Off";

	private final int Collecting_High_Limit;
	private final int Collecting_Low_Limit;
	private final int Number_Of_Threads_Per_Processor = 10;
	
	private boolean active = true;
	
	public Collector()
	{
		Runtime runTime = Runtime.getRuntime();
		this.Collecting_High_Limit = runTime.availableProcessors()*Number_Of_Threads_Per_Processor;
		this.Collecting_Low_Limit = Collecting_High_Limit*(int)(3.0f/4.0f);
		
		System.out.println("Starting collector-thread...");
		//new Thread(new Collecting()).start();
		
		WeatherParser.getWeatherData("Norway", "Nordland", "Narvik", "Narvik");
	}
	
	private void sleep()
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
	
	public static void main(String[] args)
	{
		new Settings();
		AliveMessenger.getInstance();
		new Collector();
	}
	
	public ArrayList<CollectingRequest> getCollectingRequests(int limit)
	{
		ArrayList<CollectingRequest> crawlerRequests = new ArrayList<CollectingRequest>();
		
		Connection connection = Settings.getDBC();
		
		try
		{
			int status_id = Status.getInstance().getStatusID(CollectingRequest.Request_Pending);
			
			String query = "SELECT ID FROM CRAWLER_QUEUE_OBJECTS WHERE STATUS_ID = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, status_id);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int id = set.getInt(1);
				
				crawlerRequests.add(new CollectingRequest(id));
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		catch(StatusIdNotFoundException ex)
		{
			ex.printStackTrace();
		}
		catch(CollectorRequestNotFoundException ex)
		{
			ex.printStackTrace();
		}
		
		return crawlerRequests;
	}
	
	private void validateCollectorStatus(int collectingId)
	{
		System.out.println("Validating status...");
		
		ThreadGroup threadRoot = Thread.currentThread().getThreadGroup();
		
		int activeThreadCount = threadRoot.activeCount();
		
		if(activeThreadCount > this.Collecting_High_Limit)
		{
			System.out.print("The workload is too high, waiting with processing new collection tasks");
		}
		else if(activeThreadCount <= this.Collecting_High_Limit && activeThreadCount > this.Collecting_Low_Limit)
		{
			System.out.println("The workload is average, will process new tasks");
			//this.updateSimulatorStatus(Collector.Collector_Work_Status_Medium,simulatorId);
		}
		else
		{
			System.out.println("The workload is low, will process new tasks");

		}
	}
	
	private class Collecting implements Runnable
	{
		public void run()
		{		
			int collecting_id = Settings.getCollectorID();
			System.out.println("My collector-id is :"+collecting_id);
			
			while(active)
			{				
				validateCollectorStatus(collecting_id);
				sleep();
				collectData();
			}
			
			System.out.println("Turning off collector...");
			
		}
		
		public void collectData()
		{
			System.out.println("Number of collecting threads running: "+Math.max(Thread.activeCount()-2,0));
			int limit = Math.max(0,Collecting_High_Limit - (Thread.activeCount() - 2));
			
			if(limit > 0)
			{	
				System.out.println("Can receive a workload of "+limit+" requests from database");
				System.out.println("Downloads a maximum of "+limit+" requests from database...");
				ArrayList<CollectingRequest> requests = getCollectingRequests(limit);
				
				if(requests.size() > 0)
				{
					System.out.println("Number of collecting requests recieved from database: "+requests.size());
					System.out.println("Starting "+requests.size()+" new collecting threads..");
					ExecutorService service = Executors.newFixedThreadPool(requests.size());
					
					for(int i = 0; i<requests.size(); i++)
					{
						CollectingRequest simulation = requests.get(i);
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
	}
	
	public void keyReleased(KeyEvent event)
	{
		if(event.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			active = false;
		}
	}

}
