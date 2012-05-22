package org.hikst.Simulator;

public class SimulatorOrganizer
{
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
	
	
}
