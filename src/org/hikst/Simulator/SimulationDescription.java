package org.hikst.Simulator;

public class SimulationDescription
{	
	private float timeStart;
	private float timeEnd;
	private float interval;
	private SimulatorObject simulatorObject;
	private int ID;
	
	public int getID() {
		return ID;
	}

	public float getTimeStart() {
		return timeStart;
	}

	public float getTimeEnd() {
		return timeEnd;
	}

	public float getInterval() {
		return interval;
	}

	public SimulatorObject getSimulatorObject() {
		return simulatorObject;
	}
	
	public SimulationDescription(float timeStart,float timeEnd,float interval,SimulatorObject simulatorObject) {
		super();
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
		this.interval = interval;
		this.simulatorObject = simulatorObject;
	}
	
	
}
