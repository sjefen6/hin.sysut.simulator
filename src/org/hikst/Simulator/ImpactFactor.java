package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.hikst.Commons.Datatypes.*;
import org.hikst.Commons.Datatypes.Object;
import org.hikst.Commons.Exceptions.TypeIdNotFoundException;
import org.hikst.Commons.JSON.*;
import org.hikst.Commons.Services.*;
import org.hikst.Commons.Statics.*;

public class ImpactFactor 
{
	public static final int IMPACT_SUN = 0;
	public static final int IMPACT_TEMPERATURE = 1;
	public static final int IMPACT_WEATHER = 2;
	public static final int IMPACT_BUILDING = 3;

	private int type_id;
	private String content;
	private Date currentTime;
	
	private Calendar calendar;
	private Object theObject;			//unsure if it's needed
	
	//Variables for sun related factors
	private Double sunLengthOfDay;
	private boolean sunLight;
	private Date sunDate;
	
	//Variables and constants related to temperatures and building type
	private Double temperatureElasticity;
	private Double temperatureAverage;	//average temperature given in the forecasts
	private Double temperatureMin;		//min temperature given in the forecasts
	private Double temperatureMax;		//max temperature given in the forecasts 
	private Double temperatureDD;		//temperature degree days
	private Float temperatureHLC;		//temperature heat-loss coefficency
	private Double temperatureBase;		//desired temperature for object in this impact factor.
	private boolean temperatureHeat;	//TODO: implement
	public static final double temperatureBaseResidential = 18.0;
	public static final double temperatureBaseOffice = 15.0;
	public static final double temperatureBaseIndustry = 15.0;
	
	private Double weatherTemperature;
	private Double weatherWindSpeed;
	private Double weatherEffectiveTemperature;
	private Double weatherhPa;	
	// http://www.vesma.com/ddd/index.htm
	
	/**
	 * Identifier for energy class A, 
	 */
	public static final int ENERGY_CLASS_A = 100;
	/**
	 * Identifier for energy class B
	 */
	public static final int ENERGY_CLASS_B = 101;
	/**
	 * Identifier for energy class C
	 */
	public static final int ENERGY_CLASS_C = 102;
	/**
	 * Identifier for energy class D
	 */
	public static final int ENERGY_CLASS_D = 103;
	/**
	 * Identifier for energy class E
	 */
	public static final int ENERGY_CLASS_E = 104;
	/**
	 * Identifier for energy class F
	 */
	public static final int ENERGY_CLASS_F = 105;
	
	public static final int ENERGY_BUILDINGTYPE_HOUSE = 110;
	public static final int ENERGY_BUILDINGTYPE_BLOCK = 111;
	
	/**
	 * Basic constructor for ImpactFactor, where only database id is given
	 * 
	 * 	@param id The id in the database of which this impact factor is found
	 */
	public ImpactFactor(int id)
	{
		calendar = new GregorianCalendar();
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1,id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.type_id = set.getInt(1);
				this.content = set.getString(2);
			}
			
			ImpactParser parser = new ImpactParser();
			
			parser.parseScale(type_id, content);
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * 	Constructor for ImpactFactor where the current time in simulation is given
	 * 
	 * 	@param id The id in the database of which this impact factor is found
	 * 	@param time The time of which this impact factor is used in the simulation, given with a Date	
	 */
	public ImpactFactor(int id, Date time)
	{
		calendar = new GregorianCalendar();
		currentTime = time;
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1,id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.type_id = set.getInt(1);
				this.content = set.getString(2);
			}
			
			ImpactParser parser = new ImpactParser();
			
			parser.parseScale(type_id, content);
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * 	Constructor for ImpactFactor where the desired base temperature is given
	 * 
	 * 	@param id The id in the database of which this impact factor is found
	 * 	@param baseTemp The desired temperature given to the object that uses this impactfactor.
	 */
	public ImpactFactor(int id, double baseTemp)
	{
		calendar = new GregorianCalendar();
		temperatureBase = baseTemp;
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1,id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.type_id = set.getInt(1);
				this.content = set.getString(2);
			}
			
			ImpactParser parser = new ImpactParser();
			
			parser.parseScale(type_id, content);
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * 	Constructor for ImpactFactor where the desired base temperature and simulation time is given
	 * 
	 * 	@param id The id in the database of which this impact factor is found
	 * 	@param baseTemp The desired temperature given to the object that uses this impactfactor.
	 * 	@param time The time of which this impact factor is used in the simulation, given with a Date	 
	 */
	public ImpactFactor(int id, double baseTemp, Date time)
	{
		calendar = new GregorianCalendar();
		temperatureBase = baseTemp;
		currentTime = time;
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1,id);
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.type_id = set.getInt(1);
				this.content = set.getString(2);
			}
			
			ImpactParser parser = new ImpactParser();
			
			parser.parseScale(type_id, content);
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Sets the current time and then checks if the sun is up at the given time.
	 * 
	 * @param time Current time.
	 */
	public void setCurrentTime(Date time)
	{
		currentTime = time;
		setSunSunlight();
	}
	
	
	//----------------------------
	// Methods related to the sun
	// factor
	//----------------------------
	
	/**
	 * Calculates the number of daylight hours on the specified 
	 * day defined in the given time and at the given geographical latitude.
	 * 
	 * Formula found at: http://mathforum.org/library/drmath/view/56478.html
	 * Use 0.26667 or 0.8333 as constant, described in article.
	 * 
	 * @param latitude Latitude in degrees
	 * @param time Date object with time data of the desired time.
	 */
	private void setSunLengthOfDay(double latitude, Date time)
	{
		sunDate = time;
		calendar.setTime(sunDate);
		int date = calendar.get(Calendar.DAY_OF_YEAR);
		
		double p = Math.asin(0.39795*Math.cos(0.2163108 + 2*Math.atan(0.9671396*Math.tan(0.00860*(date-186)))));
		
		sunLengthOfDay = 24 - (24/Math.PI)*Math.acos((Math.sin(0.26667*Math.PI/180)+
								Math.sin(latitude*Math.PI/180)*Math.sin(p))
								/(Math.cos(latitude*Math.PI/180)*Math.cos(p)));		
	}
	
	/**
	 * Gets method for sunLengthOfDay
	 * 
	 * @return The length of this impactFactor's day in hours, if the length is not specified, returns Not a Number.
	 */
	public Double getSunLengthOfDay()
	{		
		if (sunLengthOfDay != null)
			return sunLengthOfDay;
		else
			return Double.NaN;
	}
	
	/**
	 * Checks if there is sunlight at the specified time. If this object of
	 * ImpactFactor doesn't have a specified sunLengthOfDay yet, it will be set
	 * to false.
	 */
	public void setSunSunlight()
	{
		if(sunDate != null){
			calendar.setTime(currentTime);
			double tempHours = getSunLengthOfDay();
			double tempTime = sunDate.getHours() + TimeUnit.MINUTES.toHours(sunDate.getMinutes());
			
			if(tempHours != Double.NaN)
			{
				if (tempTime > (12 - (tempHours/2)) && tempTime < (12 + tempHours/2))
				{
					sunLight = true;
				}
				else
				{
					sunLight = false;
				}
			}
		}		
		else
			sunLight = false;
	}
	
	public void setSunSunlight(Date sunrise, Date sundown)
	{
		double tempRise, tempDown, tempCurrent;
		
		tempRise = sunrise.getHours() + TimeUnit.MINUTES.toHours(sunrise.getMinutes());
		tempDown = sundown.getHours() + TimeUnit.MINUTES.toHours(sundown.getMinutes());
		tempCurrent = currentTime.getHours() + TimeUnit.MINUTES.toHours(currentTime.getMinutes());
		
		if (tempCurrent > tempRise && tempCurrent < tempDown)
			sunLight = true;			
		else
			sunLight = false;
	}
	
	public void setSunSunlight(Date sunrise, Date sundown, Date currentTime)
	{
		double tempRise, tempDown, tempCurrent;
		
		tempRise = sunrise.getHours() + TimeUnit.MINUTES.toHours(sunrise.getMinutes());
		tempDown = sundown.getHours() + TimeUnit.MINUTES.toHours(sundown.getMinutes());
		tempCurrent = currentTime.getHours() + TimeUnit.MINUTES.toHours(currentTime.getMinutes());
		
		if (tempCurrent > tempRise && tempCurrent < tempDown)
			sunLight = true;			
		else
			sunLight = false;
	}
	
	/**
	 * Get method for sunlight
	 * 
	 * @return Returns if there's sunlight or not at this object's time.
	 */
	public boolean getSunSunlight()
	{
		return sunLight;
	}
	
	//----------------------------
	// Methods related to the 
	// weather factor
	//----------------------------
	
	public Double getEffectiveTemperature()
	{
		return weatherEffectiveTemperature;
	}
	
	/**
	 * Sets the effective temperature given temperature, humidity and windspeed in celcius, hPa and m/s
	 * 
	 * http://en.wikipedia.org/wiki/Wind_chill#Australian_Apparent_Temperature
	 * May need to be changed
	 * 
	 * @param temperature The guaged temperature
	 * @param humidity The humidity in the air
	 * @parm windspeed The windspeed given in meters per second.
	 */
	public void setEffectiveTemperature(double temperature, double humidity, double windspeed)
	{
		weatherEffectiveTemperature = temperature + 0.33*humidity - 0.70*windspeed - 4.00; 
	}
	
	
	//----------------------------
	// Methods related to the 
	// temperature factor
	//----------------------------
	
	/**
	 * Get method for temperatureElasticity.
	 * 
	 * @return temperatureElasticity
	 */
	public Double getTemperatureElasticity()
	{
		return temperatureElasticity;
	}
	
	public Double getTemperatureDD()
	{
		return temperatureDD;
	}
	
	public Float getTemperatureHLC()
	{
		return temperatureHLC;
	}
	
	public Double getTemperatureAverage()
	{
		if (temperatureAverage != null)
			return temperatureAverage;
		else 
			return Double.NaN;
	}
	
	public Double getTemperatureMax()
	{
		if (temperatureMax != null)
			return temperatureMax;
		else 
			return Double.NaN;
	}
	
	public Double getTemperatureMin()
	{
		if (temperatureMin != null)
			return temperatureMin;
		else 
			return Double.NaN;
	}
	/**
	 * Sets the heat-loss coefficency for a house or blockhouse with given
	 * energy class. Will generate a random coefficency within the energy
	 * class's levels, to create variety in the simulations. 
	 * 
	 * @param bhid Building class identifier
	 * @param btid Building type identifier
	 */
	public void setTemperatureHeatLossCoefficency(int bhid, int btid)
	{
		float tempbh;
		Random tempr = new Random();
		if (btid == ENERGY_BUILDINGTYPE_HOUSE){
			switch(bhid)
			{
			case ENERGY_CLASS_A:
				tempbh = 0.45f;
				break;
			case ENERGY_CLASS_B:
				tempbh = (tempr.nextInt(34) + 46) / 100.0f;
				break;
			case ENERGY_CLASS_C:
				tempbh = (tempr.nextInt(24) + 71) / 100.0f;
				break;
			case ENERGY_CLASS_D:
				tempbh = (tempr.nextInt(34) + 96) / 100.0f;
				break;
			case ENERGY_CLASS_E:
				tempbh = (tempr.nextInt(34) + 131) / 100.0f;
				break;
			case ENERGY_CLASS_F:
				tempbh = (tempr.nextInt(79) + 166) / 100.0f;
				break;
			default:
				tempbh = Float.NaN;
			}
		}
		else if (btid == ENERGY_BUILDINGTYPE_BLOCK)
		{
			switch(bhid)
			{
			case ENERGY_CLASS_A:
				tempbh = 0.35f;
				break;
			case ENERGY_CLASS_B:
				tempbh = (tempr.nextInt(19) + 36) / 100.0f;
				break;
			case ENERGY_CLASS_C:
				tempbh = (tempr.nextInt(19) + 56) / 100.0f;
				break;
			case ENERGY_CLASS_D:
				tempbh = (tempr.nextInt(34) + 76) / 100.0f;
				break;
			case ENERGY_CLASS_E:
				tempbh = (tempr.nextInt(39) + 150) / 100.0f;
				break;
			case ENERGY_CLASS_F:
				tempbh = (tempr.nextInt(69) + 220) / 100.0f;
				break;
			default:
				tempbh = Float.NaN;
			}
		}
		else 
		{
			tempbh = Float.NaN;
		}
		temperatureHLC = tempbh;
	}

	public void setTemperatureHeatLossCoefficency(float hlc)
	{
		temperatureHLC = hlc;
	}
	
	//TODO: get HLC from database
	public void setTemperatureHeatLossCoefficency(int id)
	{
		
	}
	
	/**
	 * Finds the Degree Days given.
	 * 
	 * The calculation requires daily measurements of maximum and minimum outside air 
	 * temperatures ( Tmax and Tmin ) and a 'base temperature' Tbase, nominated by the 
	 * user as an estimate of the outside air temperature at which no artificial heating
	 * (or cooling) is required.
	 * 
	 * http://www.vesma.com/ddd/ddcalcs.htm
	 * 
	 * @parm base Desired temperature
	 * @parm min Minimum temperature for this day.
	 * @parm max Maximum temperature for this day.
	 * @parm heat True if user wants <b>heating</b> degree days, false if user wants <b>cooling</b> degree days.
	 */
	private void setTemperatureDegreeDays(double base, double min, double max, boolean heat)
	{
//		temperatureMax = max;
//		temperatureMin = min;
		if(heat)
		{
			if(min > base)
			{
				temperatureDD = 0.0;
			}
			else if((min + max)/2 > base)
			{
				temperatureDD = (base - min)/4;
			}
			else if(max >= base)
			{
				temperatureDD = (base - min)/2 - (max - base)/4;
			}
			else if(max < base)
			{
				temperatureDD = base - (max + min)/2;
			}
		}
		else if(!heat)
		{
			if(max < base)
			{
				temperatureDD = 0.0;
			}
			else if((max + min)/2 < base)
			{
				temperatureDD = (max - base)/4;
			}
			else if(min <= base)
			{
				temperatureDD = (max - base)/2 - (base - min)/4;
			}
			else if(min>base)
			{
				temperatureDD = (max + min)/2 - base;
			}
		}
	}
	
	/**
	 * Internal parser class which parses the content read from the database.
	 */
	private class ImpactParser
	{
		
		/**
		 * @param type
		 * @param content
		 * @return 
		 */
		public void parseScale(int type, String content)
		{

				try 
				{
					if( type == Type.getInstance().getTypeID("IMPACT_WEATHER"))
					{
						parseWeatherInformation(content);
					}
					else if( type == Type.getInstance().getTypeID("IMPACT_BUILDING"))
					{
						parseHLCInformation(content);
					}
					else if( type == Type.getInstance().getTypeID("IMPACT_TEMPERATURE"))
					{
						parseTemperatureInformation(content);
					}
					else if( type == Type.getInstance().getTypeID("IMPACT_SUN"))
					{
						parseSunlightInformation(content);
					}
					else
					{
						
					}
				} 
				catch (TypeIdNotFoundException e) 
				{
					e.printStackTrace();
				}

		}
		
		private void parseSunlightInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
								
				setSunLengthOfDay(tempWD.getLatitude(), tempWD.getTimeSunrise());	
				
				if(currentTime != null)
					setSunSunlight(tempWD.getTimeSunrise(), tempWD.getTimeSunset(), currentTime);
				else
					setSunSunlight(tempWD.getTimeSunrise(), tempWD.getTimeSunset());
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
			// Hvis currentTime ikke er definert:
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		
		//TODO: Fix this parser
		private void parseHLCInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				
				ArrayList<Forecast> tempCast = tempWD.getForecasts();	
				
				
				
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		
		private void parseTemperatureInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				ArrayList<Forecast> tempCast = tempWD.getForecasts();	
				
				double tempMax, tempMin, tempAverage = 0;
				
				tempMax = tempMin = tempCast.get(0).getTemperatureValue();
				
				for(Forecast f: tempCast)
				{
					if (f.getTemperatureValue() > tempMax)
						tempMax = f.getTemperatureValue();
					if (f.getTemperatureValue() < tempMin)
						tempMin = f.getTemperatureValue();
					
					tempAverage += f.getTemperatureValue();
				}
								
				temperatureMin = tempMin;
				temperatureMax = tempMax;
				temperatureAverage = tempAverage / tempCast.size();
				
				//TODO: Fix the boolean input here to let the user specify if heating or cooling.
				setTemperatureDegreeDays(temperatureBase, temperatureMin, temperatureMax, true);
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		
		/**
		 * @param content
		 */
		private void parseWeatherInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				
				ArrayList<Forecast> tempCast = tempWD.getForecasts();
				
				double tempMax, tempMin, tempAverage = 0;
				
				tempMax = tempMin = tempCast.get(0).getTemperatureValue();
				
				for(Forecast f: tempCast)
				{
					if (f.getTemperatureValue() > tempMax)
						tempMax = f.getTemperatureValue();
					if (f.getTemperatureValue() < tempMin)
						tempMin = f.getTemperatureValue();
					
					tempAverage += f.getTemperatureValue();
					
					if(f.getFrom().getTime() < currentTime.getTime() && f.getTo().getTime() > currentTime.getTime())
					{
						weatherTemperature = f.getTemperatureValue();
						weatherWindSpeed = f.getWindspeedValue();
						weatherhPa = f.getPressureValue();
						
						weatherEffectiveTemperature = weatherTemperature + 0.33*weatherhPa - 0.70*weatherWindSpeed - 4.00; 
					}
				}
				
				tempAverage = tempAverage / tempCast.size();
				
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
}
