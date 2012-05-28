package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class Simulation implements Runnable
{
	private SimulationRequest request;
	private SimulationDescription description;
	private SimulationDependency simulationDependency;
	private CrawlerDependency crawlerDependency;
	
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
			//print out error message
		}
	}
	
	public void run()
	{
		
		
		boolean readyToSimulate = false;
		
		while(!readyToSimulate)
		{
			this.request.setStatusToProcessing();
			
			//are we ready to simulate?
			if(checkCrawlerDependencies() && checkSimulationDependencies())
			{
				readyToSimulate = true;
			}
			else
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
				while(time.before(endTime))
				{
				//get the from the finished simulation from the sons and multiply them together
				
					long longTime = time.getTime();
					long newTime = longTime + intervall;
					time = new Date(newTime);
				}
			}
			else
			{	
				while(time.before(endTime))
				{
					float effect = simulatorObject.getEffect();
					float current = simulatorObject.getCurrent();
					float voltage = simulatorObject.getVoltage();
				
					float power_consumption = effect;
					
					this.saveResults(startTime, effect, power_consumption, voltage, current, simulation_descriptions_id);
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
				
				this.request.setStatusToFinished();
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
			ex.printStackTrace();
		} catch (StatusIdNotFoundException e) {
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
			e.printStackTrace();
		}
	}
	
	/*boolean finished;
	SimulationDescription runningSimulationDescription;
	SimulationResult result;
	public boolean isFinished() {
		return finished;
	}

	public SimulationResult getResults()
	{
		return result;
	}
	
	public void start(SimulationDescription description)
	{
		this.runningSimulationDescription = description;
		Thread simulationThread = new Thread(this);
		simulationThread.start();
	}

	@Override
	public void run() {
		
		float timeStart = runningSimulationDescription.getTimeStart();
		float timeEnd = runningSimulationDescription.getTimeEnd();
		float timeInterval = runningSimulationDescription.getInterval();
		
		int numberOfSimulations = (int)((timeEnd - timeStart)/timeInterval);
		
		float[] simulations = new float[numberOfSimulations];
		
		for(int i = 0; i<numberOfSimulations; i++)
		{
			float t = timeStart +timeInterval*i;
			simulations[i] = simulate(t);
		}
		
		SimulatorObject simulatorObject = runningSimulationDescription.getSimulatorObject();
		
		result = new SimulationResult(runningSimulationDescription,simulations);
		finished = true;	
	}
	
	private float simulate(float time)
	{
		return runningSimulationDescription.getSimulatorObject().getEffect();
	}
	*/
	
	
}
