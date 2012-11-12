package org.hikst.Commons.Datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.hikst.Commons.Exceptions.ObjectNotFoundException;
import org.hikst.Commons.Exceptions.UsagePatternNotFoundException;
import org.hikst.Commons.JSON.JSONArray;
import org.hikst.Commons.JSON.JSONException;
import org.hikst.Commons.JSON.JSONObject;
import org.hikst.Commons.Services.Settings;
import org.hikst.Simulator.ImpactDegrees;

//Objects used for simulations 
public class Object 
{
	private int ID;
	private String name;
	private double effect;
	private double voltage;
	private double current;
	//private int impact_degree_ID;
	private int usage_pattern_ID;
	private double latitude;
	private double longitude;
	private double self_temperature;
	private double target_temperature;
	private double base_area;
	private double base_height;
	private double heat_loss_rate;
	
	//TODO: implement in database
	private Integer energy_class_id;
	private Integer bulding_type_id;
	
	private ArrayList<Object> sons = new ArrayList<Object>();
	private ArrayList<ImpactDegrees> theDegrees = new ArrayList<ImpactDegrees>();
	private UsagePattern thePattern;
	
	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public double getEffect() {
		return effect;
	}
	
	public double getVoltage(){
		return voltage;
	}
	
	public double getCurrent(){
		return current;
	}
	
//	public int getImpact_degree_ID() {
//		return impact_degree_ID;
//	}

	public int getUsage_pattern_ID() {
		return usage_pattern_ID;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public double getSelfTemperature(){
		return self_temperature;
	}
	
	public double getTargetTemperature(){
		return target_temperature;
	}
	
	public double getBaseArea(){
		return base_area;
	}
	
	public double getBaseHeight(){
		return base_height;
	}
	
	public double getHeatLossRate(){
		return heat_loss_rate;
	}
	
	public int getEnergyClassId()
	{
		return energy_class_id;
	}
	
	public int getBuildingTypeId()
	{
		return bulding_type_id;
	}
	
	public ArrayList<Object> getSons()
	{
		return sons;
	}
	
	public UsagePattern getUsagePattern()
	{
		return thePattern;
	}
	
	public boolean hasSons()
	{
		return sons.size() > 0;
	}
	
	public String toString()
	{
		return "\nName: "+name+"\nPower: "+this.effect+" W\nVoltage: "+voltage+" V\nCurrent: "+current+" A\n";
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject jsonObject = new JSONObject();
		
		try 
		{
			jsonObject.put("id", ID);
			jsonObject.put("name", name);
			jsonObject.put("effect", effect);
			jsonObject.put("voltage", voltage);
			jsonObject.put("current", current);
			//jsonObject.put("impact_degree_ID", impact_degree_ID);
			jsonObject.put("usage_pattern_ID", usage_pattern_ID);
			jsonObject.put("latitude", latitude);
			jsonObject.put("longitude", longitude);
			jsonObject.put("self_temperature", self_temperature);
			jsonObject.put("target_temperature", target_temperature);
			jsonObject.put("base_area", base_area);
			jsonObject.put("base_height", base_height);
			jsonObject.put("heat_loss_rate", heat_loss_rate);
			jsonObject.put("energy_class_id", energy_class_id);
			jsonObject.put("bulding_type_id", bulding_type_id);

			if (hasSons())
			{
				JSONArray tempArray = new JSONArray();
				
				for (int i = 0; i < sons.size() - 1; i++)
				{
					tempArray.put(sons.get(i));
				}
				
				jsonObject.put("sons", tempArray);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	public Object(int id) throws ObjectNotFoundException
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Objects where ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.ID = set.getInt(1);
				this.name = set.getString(2);
				this.effect = set.getDouble(3);
				this.voltage = set.getDouble(4);
				this.current = set.getDouble(5);
				this.usage_pattern_ID = set.getInt(6);
				this.longitude = set.getDouble(7);
				this.latitude = set.getDouble(8);
				this.self_temperature = set.getDouble(9);
				this.target_temperature = set.getDouble(10);
				this.base_area = set.getDouble(11);
				this.base_height = set.getDouble(12);
				this.heat_loss_rate = set.getDouble(13);
				
				try {
					this.thePattern = new UsagePattern(this.usage_pattern_ID);
				} catch (UsagePatternNotFoundException e) {
					e.printStackTrace();
				}
				getImpactDegrees(this.ID);
				
				query = "SELECT Son_ID FROM Part_Objects WHERE Father_ID=?";
				PreparedStatement anotherStatement = connection.prepareStatement(query);
				anotherStatement.setInt(1, this.ID);
				ResultSet anotherSet = anotherStatement.executeQuery();
			
				while(anotherSet.next())
				{
					sons.add(new Object(anotherSet.getInt(1)));
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
	
	private void getImpactDegrees(int id)
	{
		Connection connection = Settings.getDBC();
		
		
		try {
			String thequerry = "SELECT * from Impact_Degrees where object_id=?";
			PreparedStatement theStatement = connection.prepareStatement(thequerry);
			theStatement.setInt(1, this.ID);
			ResultSet set = theStatement.executeQuery();
			
			while(set.next())
			{
				theDegrees.add(new ImpactDegrees(set.getInt(1), set.getFloat(2), set.getInt(3)));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
