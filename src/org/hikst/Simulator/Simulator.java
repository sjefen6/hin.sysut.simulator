package org.hikst.Simulator;

import java.util.ArrayList;

public class Simulator 
{	
	private int id;
	private DatabaseConnection connection;
	
	public Simulator(int id)
	{
		this.id = id;
		connection = new DatabaseConnection();
	}
	
	public static void main(String[] args)
	{
		DatabaseConnection connection = new DatabaseConnection();
		SimulatorObject object = connection.getSimulatorObject(1);
		
		float timeStart = 0.0f;
		float timeEnd = 100.0f;
		float timeInterval = 1.0f;
		
		SimulationDescription description = new SimulationDescription(timeStart,timeEnd,timeInterval,object);
		
		Simulation simulation = new Simulation();
		simulation.start(description);
		
		while(!simulation.finished)
		{
			System.out.print("Waiting for simulation to be finished");
		}
		
		float[] results = simulation.getResults().getSimulations();
		
		SimulationDescription resultSimulationDescription = simulation.getResults().getDescription();
		System.out.println("Simulated object: "+resultSimulationDescription.getSimulatorObject().getName());
		
		for(int i = 0; i<results.length; i++)
		{
			float t = resultSimulationDescription.getTimeStart() + i*resultSimulationDescription.getInterval();
			System.out.println("T = "+t+", Effect ="+results[i]+"W");
		}
		
	}
	
	private boolean isSimulationDependencyFinished(SimulationRequest request)
	{
		int sim = request.getSimulatorDependencyID();
		return false;
		//connection.
	}
	
	private boolean isCrawlerDependencyFinished()
	{
		return true;
	}
	
	private float doSimulation(float time, SimulationDescription description)
	{
		return Float.NaN;
	}
	
	private class Simulate implements Runnable
	{

		@Override
		public void run() {
			
			ArrayList<SimulationRequest> requests = connection.getSimulationRequests("Pending", id);
			
			for(int i = 0; i<requests.size(); i++)
			{
				SimulationRequest request = requests.get(i);
				
				int requestID = request.getID();
				int simulatorDependency = request.getSimulatorDependencyID();
				int crawlerDependency = request.getCrawlerDependencyID();
				
				
				/*if(isSimulationDependencyFinished() && isCrawlerDependencyFinished())
				{	
					float simulation = doSimulation();
				}*/
			}
		}
		
	}
}
