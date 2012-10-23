package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.hikst.Commons.Datatypes.Object;
import org.hikst.Commons.Exceptions.ObjectNotFoundException;
import org.hikst.Commons.Services.Settings;

public class SimulationDescription
{	
	private Date timeStart;
	private Date timeEnd;
	private float interval;
	private Object simulatorObject;
	private int ID;
	private ArrayList<ImpactFactor> impactFactors = new ArrayList<ImpactFactor>();
	
	/**
	 * Returns the ID of this SimulationDescription
	 * 
	 * @return ID
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Returns the start time as a <I>Date</I>
	 * 
	 * @return timeStart 
	 */
	public Date getTimeStart() {
		return timeStart;
	}

	/**
	 * Returns the end time as a <I>Date</I>
	 * 
	 * @return timeEnd 
	 */
	public Date getTimeEnd() {
		return timeEnd;
	}

	/**
	 * Returns the interval of which to preform simulations in this simulation.
	 * 
	 * @return interval
	 */
	public float getInterval() {
		return interval;
	}
	
	/**
	 * Returns the attached <I>SimulatorObject</I>.
	 * 
	 * @return simulatorObject
	 */
	public Object getSimulatorObject() {
		return simulatorObject;
	}
	
	/**
	 * Returns an ArrayList of the attached <I>impactFactor</I>
	 * 
	 * @return impactFactors
	 */
	public ArrayList<ImpactFactor> getImpactFactors()
	{
		return impactFactors;
	}
	
	/*public SimulationDescription(Date timeStart,Date timeEnd,float interval,SimulatorObject simulatorObject) {
		super();
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
		this.interval = interval;
		this.simulatorObject = simulatorObject;
	}*/
	
	/**
	 * Constructor for the class. 
	 * 
	 * @exception ObjectNotFoundException
	 */
	public SimulationDescription(int id) throws ObjectNotFoundException
	{
		Connection connection = Settings.getDBC();

		try
		{
			String query = "SELECT ID, Object_ID, timeIntervall, minimumTime, maximumTime FROM Simulation_Descriptions WHERE ID=?";
			
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.ID = set.getInt(1);
				this.simulatorObject = new Object(set.getInt(2));
				this.interval = set.getFloat(3);
				this.timeStart = new Date(set.getLong(4));
				this.timeEnd = new Date(set.getLong(5));
				
				String anotherQuery = "SELECT Impact_Factor_ID FROM Impact_Factors_In_Simulation WHERE Sim_Description_ID=?";
				PreparedStatement anotherStatement = connection.prepareStatement(anotherQuery);
				anotherStatement.setInt(1, this.ID);
				ResultSet anotherSet = anotherStatement.executeQuery();
				
				while(anotherSet.next())
				{
					int impact_factor_id = anotherSet.getInt(1);
					
					this.impactFactors.add(new ImpactFactor(impact_factor_id));
				}
			}
			else
			{
				throw new ObjectNotFoundException();
			}
			
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	
}
