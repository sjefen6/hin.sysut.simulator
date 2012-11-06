package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.hikst.Commons.Datatypes.Object;
import org.hikst.Commons.Datatypes.UsagePattern;
import org.hikst.Commons.Exceptions.ObjectNotFoundException;
import org.hikst.Commons.Exceptions.StatusIdNotFoundException;
import org.hikst.Commons.Exceptions.TypeIdNotFoundException;
import org.hikst.Commons.Exceptions.UsagePatternNotFoundException;
import org.hikst.Commons.Services.Settings;
import org.hikst.Commons.Statics.Status;
import org.hikst.Commons.Statics.Type;

public class Simulation implements Runnable
{
	private QueueObjects request;
	private SimulationDescription description;
	private SimulationDependency simulationDependency;
	private CrawlerDependency crawlerDependency;
	private ArrayList <ImpactFactor> impactFactor;
	private ArrayList <Factor> factors;
	
	/*
	 * Hour length is 3,6 million milliseconds. This is critical in order to properly 
	 * scale correct heating degree and heating requirement results when interval is
	 * modified by the user.
	 */		
	private final double hourlengthmillisecs = 3600000.0f; 
	
	private double placeholderforoutsidetemp = 12.0f; //Placeholder for outside temperature to be aquired from weather data											 
													  //in later versions.
	private double tempHDD;										 
	
	/***
	 * @param request
	 * 
	 * Creates a new simulation on request
	 */
	public Simulation(QueueObjects request)
	{
		this.request = request;
		this.factors = new ArrayList<Factor>();
		
		try
		{
			this.description = new SimulationDescription(request.getSimulationDescriptionsID());
			this.simulationDependency = new SimulationDependency(request.getID());
			this.crawlerDependency = new CrawlerDependency(request.getID());
			getImpactFactors(request.getSimulationDescriptionsID());
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
					System.out.println("Request \""+this.request.getID()+"\": Unable to sleep");
					e.printStackTrace();
				}
			}
		}
		
			//the things that interacts on the simulation like the weather and so forth..
			//ArrayList<ImpactFactor> impactFactor = description.getImpactFactors();
			
			for( Factor ifactor: description.getImpactFactors())
			{
				factors.add(ifactor);
			}
		
			Date startTime  = description.getTimeStart();
			Date endTime = description.getTimeEnd();
			
			long intervall = (long)description.getInterval();
			int simulation_descriptions_id = description.getID();
			Object simulatorObject = description.getSimulatorObject();
			
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
				
				double effect = simulatorObject.getEffect();
				double current = simulatorObject.getCurrent();
				double voltage = simulatorObject.getVoltage();
				double power_consumption = effect;
				UsagePattern usagePattern = null;
				
				double latitude = simulatorObject.getLatitude();
				double longitude = simulatorObject.getLongitude();
				double self_temperature = simulatorObject.getSelfTemperature();
				double target_temperature = simulatorObject.getTargetTemperature();
				double base_area = simulatorObject.getBaseArea();
				double base_height = simulatorObject.getBaseHeight();
				double heat_loss_rate = simulatorObject.getHeatLossRate();
				
				//If the object has a base area greater than zero and the inside temperature is higher than 
				//the outside temperature, calculate the heating demand using the heating degree day formula
				//before the usage pattern is applied.
				
				//Placeholder calculation for heating demand.
//				if (base_area > 0 && target_temperature > placeholderforoutsidetemp)
//				{					
//					effect = (base_area * (heat_loss_rate * (target_temperature - placeholderforoutsidetemp)))/(hourlengthmillisecs/intervall);
//				}
				
				//Using the methods from ImpactFactor to calculate heating degree days and calculate heating demand.
				if (base_area > 0 && target_temperature > placeholderforoutsidetemp)
				{
					for (Factor temp : factors)
					{
						tempHDD = temp.getTemperatureDD();
					}
					effect = (base_area * heat_loss_rate * tempHDD)/(hourlengthmillisecs/intervall);
				}
				
				
				
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
					int probability = usagePattern.getUsage(time);
					
					double simulatedEffect = (effect * (float)probability)/100.0f;
					double simulatedCurrent = (current * (float)probability)/100.0f;
					double simulatedVoltage = (voltage * (float)probability)/100.0f;
					double simulatedConsumption = (power_consumption * (float)probability)/100.0f;
					
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

	
	private double calculateEffect(ImpactFactor factor, ImpactDegrees degree, ImpactInfluence influence, Object object)
	{
//		try {
//			if(factor.getTypeID() == Type.getInstance().getTypeID(factor.IMPACT_SUN_STRING))
//			{
//				
//			}
//		} catch (TypeIdNotFoundException e) {
//			e.printStackTrace();
//		}
		
		
		
		
		return Double.NaN;
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
			
			if(statusId == Status.getInstance().getStatusID(CrawlerQueueObjects.Request_Finished))
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
			
			if(statusId == Status.getInstance().getStatusID(QueueObjects.Request_Finished))
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
			System.out.println("Request\""+this.request.getID()+"\": No status id to the status \""+QueueObjects.Request_Finished+"\" was found");
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void saveResults(Date time, double simulatedEffect, double simulatedConsumption, double simulatedVoltage, double simulatedCurrent, int description_id)
	{
		Connection connection = Settings.getDBC();
		
		String query = "insert into Simulations(Time,Effect,Power_Consumption,Voltage,Current,Sim_Description_ID) VALUES(?,?,?,?,?,?);";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			statement.setLong(1, time.getTime());
			statement.setDouble(2, simulatedEffect);
			statement.setDouble(3, simulatedConsumption);
			statement.setDouble(4, simulatedVoltage);
			statement.setDouble(5,simulatedCurrent);
			statement.setInt(6,description_id);
			statement.executeUpdate();	
		} catch (SQLException e) {
			System.out.println("Request\""+this.request.getID()+"\": Unable to save result");
			e.printStackTrace();
		}
	}
	
	private void getImpactFactors(int id)
	{
		Connection connection = Settings.getDBC();
		
		String query = "select Impact_Factor_ID from Impact_Factors_In_Simulation where Sim_Description_ID=?";
		PreparedStatement statement;
		try
			{
			statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			statement.executeQuery();
			}
		catch (SQLException e)
			{			
			e.printStackTrace();
			}
		
	}
}
