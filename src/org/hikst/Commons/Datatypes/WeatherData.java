package org.hikst.Commons.Datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.hikst.Commons.Exceptions.AlreadyExistsInDatabaseException;
import org.hikst.Commons.Exceptions.TypeIdNotFoundException;
import org.hikst.Commons.Exceptions.WrongContentTypeException;
import org.hikst.Commons.JSON.JSONArray;
import org.hikst.Commons.JSON.JSONException;
import org.hikst.Commons.JSON.JSONObject;
import org.hikst.Commons.JSON.JSONParsable;
import org.hikst.Commons.Services.Settings;
import org.hikst.Commons.Statics.Type;

public class WeatherData implements JSONParsable {

	private String name;
	private String type;
	private String country;
	private String timezone;
	private int utc_offset;
	private double altitude;
	private double latitude;
	private double longitude;
	private String geobase;
	private int geobaseid;
	
	private Date lastUpdate;
	private Date nextUpdate;
	
	private Date timeSunrise;
	private Date timeSunset;
	
	private ArrayList<Forecast> forecasts;
	
	private boolean fromDatabase;
	
	
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getCountry() {
		return country;
	}

	public String getTimezone() {
		return timezone;
	}

	public int getUtc_offset() {
		return utc_offset;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getGeobase() {
		return geobase;
	}

	public int getGeobaseid() {
		return geobaseid;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public Date getNextUpdate() {
		return nextUpdate;
	}

	public Date getTimeSunrise() {
		return timeSunrise;
	}

	public Date getTimeSunset() {
		return timeSunset;
	}

	public ArrayList<Forecast> getForecasts() {
		return forecasts;
	}
	
	public WeatherData(int id) throws WrongContentTypeException
	{
		try {
			String query = "SELECT * FROM impact_factor WHERE id=?;";
			PreparedStatement preparedStatement = Settings.getDBC().prepareStatement(query);
			preparedStatement.setInt(1, id);
			
			ResultSet set = preparedStatement.executeQuery();
			
			set.next();
			
			int type_id = set.getInt("type_id");
			String content = set.getString("content");
			
			if(!(Type.getInstance().getTypeID("Weather") == type_id))
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
	
	public WeatherData(JSONObject jsonObject)
	{
		this.setData(jsonObject);
	}
	
	public WeatherData(String name, String type, String country,
			String timezone, int utc_offset, double altitude, double latitude,
			double longitude, String geobase, int geobaseid, Date lastUpdate,
			Date nextUpdate, Date timeSunrise, Date timeSunset) {
		super();
		this.name = name;
		this.type = type;
		this.country = country;
		this.timezone = timezone;
		this.utc_offset = utc_offset;
		this.altitude = altitude;
		this.latitude = latitude;
		this.longitude = longitude;
		this.geobase = geobase;
		this.geobaseid = geobaseid;
		this.lastUpdate = lastUpdate;
		this.nextUpdate = nextUpdate;
		this.timeSunrise = timeSunrise;
		this.timeSunset = timeSunset;
		
		forecasts = new ArrayList<Forecast>();
	}
	
	public void addForecast(Forecast forecast)
	{
		forecasts.add(forecast);
	}

	public int save() throws AlreadyExistsInDatabaseException
	{
		if(!fromDatabase)
		{
			try {
				
				JSONObject jsonObject = this.toJSONObject();
				int type_id = Type.getInstance().getTypeID("Weather");
				
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
		String weatherData = String.format("Name of place: %s\nType of place: %s\nCountry: %s\nTimezone: %s\nUTC-offset: %s minutes\nAltitude:"+
	" %s\nLatitude: %s\nLongitude: %s\nGeobase: %s\nGeobase-id: %s\n"+
				"Last update: %s\nNext update: %s\nTime of sunset: %s\nTime of sunrise: %s\n"
				, name,type,country,timezone,utc_offset,altitude,
				latitude,longitude,geobase,geobaseid
				,lastUpdate,nextUpdate,timeSunset,timeSunrise);
		
		weatherData += "\n\nForecasts: \n";
		
		for(int i = 0; i<forecasts.size(); i++)
		{
			weatherData += forecasts.get(i).toString()+"\n\n";
		}
		
		return weatherData;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		
		try 
		{
			jsonObject.put("name", name);
			jsonObject.put("type", type);
			jsonObject.put("country", country);
			jsonObject.put("timezone", timezone);
			jsonObject.put("utc_offset", utc_offset);
			jsonObject.put("altitude",altitude);
			jsonObject.put("latitude", latitude);
			jsonObject.put("longitude",longitude);
			jsonObject.put("geobase", geobase);
			jsonObject.put("geobaseid", geobaseid);
			jsonObject.put("lastUpdate", lastUpdate.getTime());
			jsonObject.put("nextUpdate", nextUpdate.getTime());
			jsonObject.put("timeSunrise", timeSunrise.getTime());
			jsonObject.put("timeSunset", timeSunset.getTime());
			
			JSONArray jsonArray = new JSONArray();
			
			for(int i = 0; i<forecasts.size(); i++)
			{
				jsonArray.put(forecasts.get(i).toJSONObject());
			}
		
			jsonObject.put("forecasts", jsonArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObject;
	}

	@Override
	public void setData(JSONObject jsonObject) {
		
		try {
			name = jsonObject.getString("name");
			type = jsonObject.getString("type");
			country = jsonObject.getString("country");
			timezone = jsonObject.getString("timezone");
			utc_offset = jsonObject.getInt("utc_offset");
			altitude = jsonObject.getInt("altitude");
			longitude = jsonObject.getDouble("longitude");
			latitude = jsonObject.getDouble("longitude");
			geobase = jsonObject.getString("geobase");
			geobaseid = jsonObject.getInt("geobaseid");
			lastUpdate = new Date(jsonObject.getLong("lastUpdate"));
			nextUpdate = new Date(jsonObject.getLong("nextUpdate"));
			timeSunrise = new Date(jsonObject.getLong("timeSunrise"));
			timeSunset = new Date(jsonObject.getLong("timeSunset"));

			JSONArray jsonArray = jsonObject.getJSONArray("forecasts");
			
			forecasts.clear();
			
			for(int index = 0; index<jsonArray.length(); index++)
			{
				JSONObject forecast = jsonArray.getJSONObject(index);
				
				forecasts.add(new Forecast(forecast));
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
