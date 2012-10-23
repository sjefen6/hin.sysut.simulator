package org.hikst.Commons.Datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hikst.Commons.Exceptions.AlreadyExistsInDatabaseException;
import org.hikst.Commons.Exceptions.TypeIdNotFoundException;
import org.hikst.Commons.Exceptions.WrongContentTypeException;
import org.hikst.Commons.JSON.JSONException;
import org.hikst.Commons.JSON.JSONObject;
import org.hikst.Commons.JSON.JSONParsable;
import org.hikst.Commons.Services.Settings;
import org.hikst.Commons.Statics.Type;

public class Forecast implements JSONParsable
{
	private Date from;
	private Date to;
	private int period;
	
	private double windDirectionValue;
	private String windDirectionUnit;
	
	private double windspeedValue;
	private String windspeedUnit;
	
	private double temperatureValue;
	private String temperatureUnit;
	
	private double pressureValue;
	private String pressureUnit;
	
	private boolean fromDatabase;
	
	
	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

	public int getPeriod() {
		return period;
	}

	public double getWindDirectionValue() {
		return windDirectionValue;
	}

	public String getWindDirectionUnit() {
		return windDirectionUnit;
	}

	public double getWindspeedValue() {
		return windspeedValue;
	}

	public String getWindspeedUnit() {
		return windspeedUnit;
	}

	public double getTemperatureValue() {
		return temperatureValue;
	}

	public String getTemperatureUnit() {
		return temperatureUnit;
	}

	public double getPressureValue() {
		return pressureValue;
	}

	public String getPressureUnit() {
		return pressureUnit;
	}

	public Forecast(int id) throws WrongContentTypeException
	{	
		try {
			String query = "SELECT * FROM impact_factor WHERE id=?;";
			PreparedStatement preparedStatement = Settings.getDBC().prepareStatement(query);
			preparedStatement.setInt(1, id);
			
			ResultSet set = preparedStatement.executeQuery();
			
			set.next();
			
			int type_id = set.getInt("type_id");
			String content = set.getString("content");
			
			if(!(Type.getInstance().getTypeID("Forecast") == type_id))
			{
				throw new WrongContentTypeException();
			}
			else
			{
				this.setData(new JSONObject(content));
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeIdNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.fromDatabase = true;
	}
	
	public Forecast(JSONObject jsonObject)
	{
		this.setData(jsonObject);
	}

	public Forecast(Date from, Date to, int period, double windDirectionValue,
			String windDirectionUnit,
			double windspeedValue, String windspeedUnit, double temperatureValue,
			String temperatureUnit, double pressureValue, String pressureUnit) {
		super();
		this.from = from;
		this.to = to;
		this.period = period;
		this.windDirectionValue = windDirectionValue;
		this.windDirectionUnit = windDirectionUnit;
		this.windspeedValue = windspeedValue;
		this.windspeedUnit = windspeedUnit;
		this.temperatureValue = temperatureValue;
		this.temperatureUnit = temperatureUnit;
		this.pressureValue = pressureValue;
		this.pressureUnit = pressureUnit;
	}
	
	public int save() throws AlreadyExistsInDatabaseException
	{
		if(!fromDatabase)
		{	
			try {
				
				JSONObject jsonObject = this.toJSONObject();
				int type_id = Type.getInstance().getTypeID("Forecast");
				
				String query = "insert into impact_factor(?,?); RETURNING *";
				
				PreparedStatement preparedStatement = Settings.getDBC().prepareStatement(query);
				preparedStatement.setInt(1, type_id);
				preparedStatement.setString(2, jsonObject.toString());
				ResultSet set = preparedStatement.executeQuery();
				set.next();
				return set.getInt("id");
				
			} catch (TypeIdNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Integer.MIN_VALUE;
		}
		else
		{
			throw new AlreadyExistsInDatabaseException();
		}
		
	}
	
	public String toString()
	{
		return String.format("From: %s\nTo: %s\nPeriod: %s\nWind-direction: %s %s\nWind-speed: %s %s\nTemperature: %s %s\nPressure: %s %s",
				from, to, period,windDirectionValue,windDirectionUnit,windspeedValue,windspeedUnit,temperatureValue,temperatureUnit,pressureValue,pressureUnit);
	}

	@Override
	public JSONObject toJSONObject() {
		
		JSONObject jsonObject = new JSONObject();
		
		try {
	
			jsonObject.put("from", from.getTime());
			jsonObject.put("to", to.getTime());
			jsonObject.put("period", period);
			jsonObject.put("windDirectionValue", this.windDirectionValue);
			jsonObject.put("windDirectionUnit", this.windDirectionUnit);
			jsonObject.put("windspeedValue", this.windspeedValue);
			jsonObject.put("windspeedUnit", this.windspeedUnit);
			jsonObject.put("temperatureValue", this.temperatureValue);
			jsonObject.put("temperatureUnit", this.temperatureUnit);
			jsonObject.put("pressureValue", this.pressureValue);
			jsonObject.put("pressureUnit", this.pressureUnit);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return jsonObject;
	}

	@Override
	public void setData(JSONObject jsonObject) {
		
		try {
			
			from = new Date(jsonObject.getLong("from"));
			to = new Date(jsonObject.getLong("to"));
			period = jsonObject.getInt("period");
			windDirectionValue = jsonObject.getDouble("windDirectionValue");
			windDirectionUnit = jsonObject.getString("windDirectionUnit");
			windspeedValue = jsonObject.getDouble("windspeedValue");
			windspeedUnit = jsonObject.getString("windspeedUnit");
			temperatureValue = jsonObject.getDouble("temperatureValue");
			temperatureUnit = jsonObject.getString("temperatureUnit");
			pressureValue = jsonObject.getDouble("pressureValue");
			pressureUnit = jsonObject.getString("pressureUnit");
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
