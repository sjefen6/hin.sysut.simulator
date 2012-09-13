package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.swing.text.DateFormatter;

public class Simulation implements Runnable
{
	private SimulationRequest request;
	private SimulationDescription description;
	private SimulationDependency simulationDependency;
	private CrawlerDependency crawlerDependency;
	
	
	//TODO: Fix comments
	/***
	 * @param request
	 * 
	 * Creates a new simulation on request
	 */
	public Simulation(SimulationRequest request)
	{
		this.request = request;
		
		try
		{
			this.description = new SimulationDescription(request.getSimulationDescriptionsID());
			this.simulationDependency = new SimulationDependency(request.getID());
			this.crawlerDependency = new CrawlerDependency(request.getID());
		}
		catch(ObjectNotFoundException ex)
		{
			System.out.println("No simulation description with the id \""+request.getSimulationDescriptionsID()+"\" was found");
			ex.printStackTrace();
		}
	}
	
	public void run()
	{
		boolean readyToSimulate = false;
		
		while(!readyToSimulate)
		{
			System.out.println("Request \""+this.request.getID()+"\": Sets simulation request to \"processing\"");
			this.request.setStatusToProcessing();
			
			//are we ready to simulate?
			System.out.println("Request \""+this.request.getID()+"\": Checking dependencies");
			if(checkCrawlerDependencies() && checkSimulationDependencies())
			{
				System.out.println("Request \""+this.request.getID()+"\": The dependencies are finished");
				System.out.println("Request \""+this.request.getID()+"\": Can now start simulation...");
				readyToSimulate = true;
			}
			else
			{
				System.out.println("Request \""+this.request.getID()+"\": The dependencies are not finished");
				System.out.println("Request \""+this.request.getID()+"\": Simulation has to wait a bit");
				System.out.println("Request \""+this.request.getID()+"\": Sleep for one second...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Request \""+this.request.getID()+"\": Unable to sleep");
					e.printStackTrace();
				}
			}
		}
		
			//the things that interracts on the simulation like the weather and so forth..
			ArrayList<ImpactFactor> impactFactors = description.getImpactFactors();
		
			Date startTime  = description.getTimeStart();
			Date endTime = description.getTimeEnd();
			
			long intervall = (long)description.getInterval();
			int simulation_descriptions_id = description.getID();
			SimulatorObject simulatorObject = description.getSimulatorObject();
			
			Date time = startTime;
			
			if(simulatorObject.hasSons())
			{	
				System.out.println("Request \""+this.request.getID()+"\": is dependent of one or more simulation(s)");
				
				System.out.println("Request\""+this.request.getID()+"\": Begins Simulations..");
				
				System.out.println("Request\""+this.request.getID()+"\": Simulation object: "+simulatorObject);
				while(time.before(endTime))
				{
				//get the from the finished simulation from the sons and multiply them together
					
					long longTime = time.getTime();
					long newTime = longTime + intervall;
					time = new Date(newTime);
				}
				
				System.out.println("Request\""+this.request.getID()+"\": Simulations finished");
			}
			else
			{	
				System.out.println("Request\""+this.request.getID()+"\": Is not dependent of one or more simulation(s)");
				
				System.out.println("Request\""+this.request.getID()+"\": Begins Simulation..");
				
				float effect = simulatorObject.getEffect();
				float current = simulatorObject.getCurrent();
				float voltage = simulatorObject.getVoltage();
				float power_consumption = effect;
				UsagePattern usagePattern = null;
				
				try
				{
					usagePattern = getUsagePattern(simulatorObject.getID());	
				}
				catch(UsagePatternNotFoundException ex)
				{
					System.out.println("Usage pattern for object with ID=\""+simulatorObject.getID()+"\" could not be determined");
					ex.printStackTrace();
				}
				
				while(time.before(endTime))
				{
					int probability = usagePattern.getProbability(time);
					
					float simulatedEffect = (effect * (float)probability)/100.0f;
					float simulatedCurrent = (current * (float)probability)/100.0f;
					float simulatedVoltage = (voltage * (float)probability)/100.0f;
					float simulatedConsumption = (power_consumption * (float)probability)/100.0f;
					
					System.out.println("Request\""+this.request.getID()+"\": Time = \""+time.toGMTString()+" Power = \""+simulatedEffect+" W\" Current = \""+simulatedCurrent+" A\" Voltage = \""+simulatedVoltage+" V\" Power Consumption = \""+simulatedConsumption+"\"");
					
					System.out.println("Request\""+this.request.getID()+"\": uploads result to database");
					this.saveResults(time, simulatedEffect, simulatedConsumption, simulatedVoltage, simulatedCurrent, simulation_descriptions_id);
					//simulate here and do something something here
					//calculate number of simulations
				
					//for each simulation
						//calculate time
						//
					//jumps to next time
					long longTime = time.getTime();
					long newTime = longTime + intervall;
					time = new Date(newTime);
				}
				
				System.out.println("Request\""+this.request.getID()+"\": Simulations finished");
				this.request.setStatusToFinished();
			}
	
	}
	
	private UsagePattern getUsagePattern(int object_id)throws UsagePatternNotFoundException
	{
		UsagePattern pattern = null;
		
		try
		{
			int usage_pattern_id = getUsagePatternID(object_id);
			pattern = new UsagePattern(usage_pattern_id);
		}
		catch(UsagePatternNotFoundException ex)
		{
			if(hasParent(object_id))
			{
				pattern = getUsagePattern(getParentID(object_id));
			}
			else
			{
				throw new UsagePatternNotFoundException();
			}
		}
		
		return pattern;
	}
	
	private boolean hasParent(int object_id)
	{
		try
		{
			Connection connection = Settings.getDBC();
			
			String query = "SELECT Father_ID from PartObjects WHERE Son_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, object_id);
			ResultSet set = statement.executeQuery();
			
			return set.next();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	private int getParentID(int object_id)
	{
		try
		{
			Connection connection = Settings.getDBC();
			
			String query = "SELECT Father_ID from PartObjects WHERE Son_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, object_id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				return set.getInt(1);
			}
			else
			{
				return Integer.MAX_VALUE;
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			return Integer.MAX_VALUE;
		}
	}
	
	private int getUsagePatternID(int object_id)
	{
		try
		{
			Connection connection = Settings.getDBC();
			
			String query = "SELECT Usage_Pattern_ID FROM Objects WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, object_id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				return set.getInt(1);
			}
			else
			{
				return Integer.MAX_VALUE;
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			return Integer.MAX_VALUE;
		}
	}
	
	private boolean checkCrawlerDependencies()
	{
		ArrayList<Integer> crawlerDependencies = this.crawlerDependency.getCrawlerDependencies();
		
		for(int dependency = 0; dependency<crawlerDependencies.size(); dependency++)
		{
			int crawler_data_id = crawlerDependencies.get(dependency);
			boolean finished = checkCrawlerDependency(crawler_data_id);
			
			if(!finished)
				return false;
		}
		
		return true;
	}
	
	private boolean checkCrawlerDependency(int crawler_data_id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "Status_ID FROM Crawler_Queue_Objects WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, crawler_data_id);
			
			int statusId = statement.executeQuery().getInt(1);
			
			if(statusId == Status.getInstance().getStatusID(CrawlerRequest.Request_Finished))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		} catch (StatusIdNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean checkSimulationDependencies()
	{
		ArrayList<Integer> simulationDependencies = this.simulationDependency.getDependencies();
		
		for(int dependency = 0; dependency<simulationDependencies.size(); dependency++)
		{
			 int simulation_id = simulationDependencies.get(dependency); 
			 boolean finished = checkSimulationDependency(simulation_id);
			 
			 if(!finished)
				 return false;
		}
		
		return true;
		
	}
	
	private boolean checkSimulationDependency(int dependency_id)
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "Status_ID FROM Simulator_Queue_Objects WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, dependency_id);
			
			int statusId = statement.executeQuery().getInt(1);
			
			if(statusId == Status.getInstance().getStatusID(SimulationRequest.Request_Finished))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(SQLException ex)
		{
			System.out.println("Request\""+this.request.getID()+"\": Unable to check simulation dependencies");
			ex.printStackTrace();
		} catch (StatusIdNotFoundException e) {
			System.out.println("Request\""+this.request.getID()+"\": No status id to the status \""+SimulationRequest.Request_Finished+"\" was found");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void saveResults(Date time, float effect, float power_consumption, float voltage, float current, int description_id)
	{
		Connection connection = Settings.getDBC();
		
		String query = "insert into Simulations(Time,Effect,Power_Consumption,Voltage,Current,Sim_Description_ID) VALUES(?,?,?,?,?,?);";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			statement.setLong(1, time.getTime());
			statement.setFloat(2, effect);
			statement.setFloat(3, power_consumption);
			statement.setFloat(4, voltage);
			statement.setFloat(5,current);
			statement.setInt(6,description_id);
			statement.executeUpdate();	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Request\""+this.request.getID()+"\": Unable to save result");
			e.printStackTrace();
		}
	}	
}
