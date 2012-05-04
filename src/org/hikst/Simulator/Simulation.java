package org.hikst.Simulator;

public class Simulation implements Runnable
{
	boolean finished;
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
	
	
}
