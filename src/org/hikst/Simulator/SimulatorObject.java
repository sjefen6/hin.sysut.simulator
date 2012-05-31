package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

//Objects used for simulations 
public class SimulatorObject 
{
	private int ID;
	private String name;
	private float effect;
	private float voltage;
	private float current;
	private int impact_degree_ID;
	private ArrayList<Integer> sons = new ArrayList<Integer>();
	
	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public float getEffect() {
		return effect;
	}
	
	public float getVoltage(){
		return voltage;
	}
	
	public float getCurrent(){
		return current;
	}
	
	public ArrayList<Integer> getSons()
	{
		return sons;
	}
	
	public boolean hasSons()
	{
		return sons.size() > 0;
	}
	
	public SimulatorObject(int iD, String name, float effect) {
		super();
		ID = iD;
		this.name = name;
		this.effect = effect;
	}
	
	public String toString()
	{
		return "\nName: "+name+"\nPower: "+this.effect+" W\nVoltage: "+voltage+" V\nCurrent: "+current+" A\n";
	}
	
	public SimulatorObject(int id) throws ObjectNotFoundException
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ID, Name, Effect, Voltage, Current, Impact_Degree_ID FROM Objects";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.ID = set.getInt(1);
				this.name = set.getString(2);
				this.effect = set.getFloat(3);
				this.voltage = set.getFloat(4);
				this.current = set.getFloat(5);
				this.impact_degree_ID = set.getInt(6);
				
				query = "SELECT Son_ID FROM Part_Objects WHERE Father_ID=?";
				PreparedStatement anotherStatement = connection.prepareStatement(query);
				anotherStatement.setInt(1, this.ID);
				ResultSet anotherSet = anotherStatement.executeQuery();
			
				while(anotherSet.next())
				{
					sons.add(anotherSet.getInt(1));
				}
			}
			else
			{
				throw new ObjectNotFoundException();
			}
		}catch(SQLException ex)
		{
			ex.printStackTrace();
			//throw new ObjectNotFoundException();
		}
	}
}
