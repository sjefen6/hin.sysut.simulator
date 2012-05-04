package org.hikst.Simulator;

public class SimulationResult
{
	private SimulationDescription description;
	private float[] plots;
	
	
	public SimulationDescription getDescription() {
		return description;
	}

	public float[] getSimulations() {
		return plots;
	}

	public SimulationResult(SimulationDescription description,
			float[] simulations) {
		super();
		this.description = description;
		this.plots = simulations;
	}
	
	public float getEffect(float time)
	{
		if(insideBounds(time))
		{
			int index = getIndex(time);
			
			return plots[index];
		}
		else
		{
			return Float.NaN;
		}
	}
	
	/*public float getCurrent(float time)
	{
		if(insideBounds(time))
		{
			int index = getIndex(time);
			float effect =  plots[index];
			
		}
		else
		{
			return Float.NaN;
		}
	}
	
	public float getVoltage(float time)
	{
		if(insideBounds(time))
		{
			int index = getIndex(time);
		}
		else
		{
			return Float.NaN;
		}
	}*/
	
	private boolean insideBounds(float time)
	{
		if(time <description.getTimeStart())
			return false;
		
		if(time > description.getTimeEnd())
			return false;
	
		return true;
	}
	
	private int getIndex(float time)
	{
		float x = time - description.getTimeStart();
		float floatingXIndex = (x/description.getInterval()); 
		int xIndex = Math.round((floatingXIndex));
		return xIndex;
	}
}
