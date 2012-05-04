package org.hikst.Simulator;

public class SimulationRequest 
{
	private int ID;
	private int simID;
	private int simulatorDependencyID;
	private int crawlerDependencyID;
	private int simulationDescriptionsID;
	
	public int getID() {
		return ID;
	}
	public int getSimID() {
		return simID;
	}

	
	public int getSimulatorDependencyID() {
		return simulatorDependencyID;
	}
	public int getCrawlerDependencyID() {
		return crawlerDependencyID;
	}
	public int getSimulationDescriptionsID() {
		return simulationDescriptionsID;
	}
	public SimulationRequest(int iD, int simID, int simulatorDependencyID,
			int crawlerDependencyID, int simulationDescriptionsID) {
		super();
		ID = iD;
		this.simID = simID;
		this.simulatorDependencyID = simulatorDependencyID;
		this.crawlerDependencyID = crawlerDependencyID;
		this.simulationDescriptionsID = simulationDescriptionsID;
	}
	
}
