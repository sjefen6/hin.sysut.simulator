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
import org.hikst.Commons.Exceptions.*;
import org.hikst.Commons.JSON.*;
import org.hikst.Commons.Services.*;
import org.hikst.Commons.Statics.*;

public class ImpactFactor 
{
	public static final int IMPACT_SUN = 2;
	public static final int IMPACT_TEMPERATURE = 5;
	public static final int IMPACT_WEATHER = 1;
	public static final int IMPACT_BUILDING = 3;
	
	public static final String IMPACT_SUN_STRING = "IMPACT_SUN";
	public static final String IMPACT_TEMPERATURE_STRING = "IMPACT_TEMPERATURE";
	public static final String IMPACT_WEATHER_STRING = "IMPACT_WEATHER";
	public static final String IMPACT_BUILDING_STRING = "IMPACT_BUILDING";
	
	public static final double temperatureBaseResidential = 18.0;
	public static final double temperatureBaseOffice = 15.0;
	public static final double temperatureBaseIndustry = 15.0;
	
	private ArrayList<Factor> theFactorList;
	private Factor theFactor;
	
	private double longitude, latitude;
	private Date timeFrom, timeTo;	

	private int type_id;
	private String content;
	private Date currentTime;
	
	private Calendar calendar;
	private Object theObject;			//unsure if it's needed
	

//	
//	//Variables for sun related factors
//	private Double sunLengthOfDay;
//	private boolean sunLight;
//	private Date sunDate;
//	
//	//Variables and constants related to temperatures and building type
//	private Double temperatureElasticity;
//	private Double temperatureAverage;	//average temperature given in the forecasts
//	private Double temperatureMin;		//min temperature given in the forecasts
//	private Double temperatureMax;		//max temperature given in the forecasts 
//	private Double temperatureDD;		//temperature degree days
//	private Float temperatureHLC;		//temperature heat-loss coefficency
//	private Double temperatureBase;		//desired temperature for object in this impact factor.
//
//	
//	private Double weatherTemperature;
//	private Double weatherWindSpeed;
//	private Double weatherEffectiveTemperature;
//	private Double weatherhPa;	
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
	public static final int ENERGY_BUILDINGTYPE_OFFICE = 112;
	public static final int ENERGY_BUILDINGTYPE_SCHOOL = 113;
	public static final int ENERGY_BUILDINGTYPE_HOSPITAL = 114;
	public static final int ENERGY_BUILDINGTYPE_INDUSTRY_OR_WORKSHOP = 115;
	public static final int ENERGY_BUILDINGTYPE_HOTEL = 116;
	public static final int ENERGY_BUILDINGTYPE_BUSINESS_WITH_FRIDGE_OR_HEATER_GROCERIES = 117;
	public static final int ENERGY_BUILDINGTYPE_BUSINESS_WITHOUT_GROCERIES = 118;
	
	/**
	 * Basic constructor for ImpactFactor, where only database id is given
	 * 
	 * 	@param id The id in the database of which this impact factor is found
	 */
	public ImpactFactor(int id)
	{
		calendar = new GregorianCalendar();
		theFactor = new Factor();
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Impact_Factor WHERE ID=?";
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
		theFactor = new Factor();
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Impact_Factor WHERE ID=?";
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
	public ImpactFactor(int id, Object object)
	{
		calendar = new GregorianCalendar();
		theFactor = new Factor();
		theFactor.setTemperatureBase(object.getTargetTemperature());
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Impact_Factor WHERE ID=?";
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
		theFactor.setTemperatureBase(baseTemp);
		currentTime = time;
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Impact_Factor WHERE ID=?";
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
	
	public ImpactFactor(double longitude, double latitude, Date from, Date to, int type)
	{
		this.longitude = longitude;
		this.latitude = latitude;
		this.timeFrom = from;
		this.timeTo = to;
		this.type_id = type;
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT * FROM Impact_Factor WHERE Type_ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1,type_id);
			ResultSet set = statement.executeQuery();
			
			ImpactParser parser = new ImpactParser();
			
			//TODO: Make hack to fix adding factors to theFactorList in given long/lat area.
			while(set.next())
			{
				this.type_id = set.getInt(1);
				this.content = set.getString(2);
				
				Factor tempFactor = new Factor();
				
				tempFactor = parser.parseScale(type_id, content);
				
				theFactorList.add(tempFactor);
			}
			
			
			
			
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		
		
	}
	
	public int getTypeID()
	{
		return type_id;
	}
	
	public Factor getTheFactor()
	{
		return theFactor;
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
	private double calculateSunLengthOfDay(double latitude, Date time)
	{
		int date = calendar.get(Calendar.DAY_OF_YEAR);
		
		double p = Math.asin(0.39795*Math.cos(0.2163108 + 2*Math.atan(0.9671396*Math.tan(0.00860*(date-186)))));
		
		return (24 - (24/Math.PI)*Math.acos((Math.sin(0.26667*Math.PI/180)+
								Math.sin(latitude*Math.PI/180)*Math.sin(p))
								/(Math.cos(latitude*Math.PI/180)*Math.cos(p))));		
	}
	
	/**
	 * Checks if there is sunlight at the specified time. If this object of
	 * ImpactFactor doesn't have a specified sunLengthOfDay yet, it will be set
	 * to false.
	 */
	public boolean isSunlight(Date sunDate, double hoursOfSunLight)
	{
		if(sunDate != null){
			calendar.setTime(currentTime);
			double tempHours = hoursOfSunLight;
			double tempTime = sunDate.getHours() + TimeUnit.MINUTES.toHours(sunDate.getMinutes());
			
			if(tempHours != Double.NaN)
			{
				if (tempTime > (12 - (tempHours/2)) && tempTime < (12 + tempHours/2))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}		
		return false;
	}
	
	public boolean isSunlight(Date sunrise, Date sundown)
	{
		double tempRise, tempDown, tempCurrent;
		
		tempRise = sunrise.getHours() + TimeUnit.MINUTES.toHours(sunrise.getMinutes());
		tempDown = sundown.getHours() + TimeUnit.MINUTES.toHours(sundown.getMinutes());
		tempCurrent = currentTime.getHours() + TimeUnit.MINUTES.toHours(currentTime.getMinutes());
		
		if (tempCurrent > tempRise && tempCurrent < tempDown)
			return true;			
		else
			return false;
	}
	
	public boolean isSunlight(Date sunrise, Date sundown, Date currentTime)
	{
		double tempRise, tempDown, tempCurrent;
		
		tempRise = sunrise.getHours() + TimeUnit.MINUTES.toHours(sunrise.getMinutes());
		tempDown = sundown.getHours() + TimeUnit.MINUTES.toHours(sundown.getMinutes());
		tempCurrent = currentTime.getHours() + TimeUnit.MINUTES.toHours(currentTime.getMinutes());
		
		if (tempCurrent > tempRise && tempCurrent < tempDown)
			return true;			
		else
			return false;
	}
	
	//----------------------------
	// Methods related to the 
	// weather factor
	//----------------------------
	
	/**
	 * Sets the effective temperature given temperature, humidity and windspeed in celcius, hPa and m/s
	 * 
	 * http://en.wikipedia.org/wiki/Wind_chill#Australian_Apparent_Temperature
	 * May need to be changed
	 * 
	 * @param temperature The guaged temperature
	 * @param humidity The humidity in the air
	 * @parm windspeed The windspeed given in meters per second.
	 * @return the effective temperature.
	 */
	public double setEffectiveTemperature(double temperature, double humidity, double windspeed)
	{
		return (temperature + 0.33*humidity - 0.70*windspeed - 4.00); 
	}
	
	//----------------------------
	// Methods related to the 
	// temperature factor
	//----------------------------
	
	/**
	 * Sets the heat-loss coefficency for a house or blockhouse with given
	 * energy class. Will generate a random coefficency within the energy
	 * class's levels, to create variety in the simulations. 
	 * 
	 * @param bhid Building class identifier
	 * @param btid Building type identifier
	 */
	public float setTemperatureHeatLossCoefficency(int bhid, int btid)
	{
		float tempbh;
		Random tempr = new Random();
		if (btid == ENERGY_BUILDINGTYPE_HOUSE)
		{
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
		else if (btid == ENERGY_BUILDINGTYPE_SCHOOL)
		{
			switch(bhid)
			{
			case ENERGY_CLASS_A:
				tempbh = 0.60f;
				break;
			case ENERGY_CLASS_B:
				tempbh = (tempr.nextInt(24) + 61) / 100.0f;
				break;
			case ENERGY_CLASS_C:
				tempbh = (tempr.nextInt(29) + 86) / 100.0f;
				break;
			case ENERGY_CLASS_D:
				tempbh = (tempr.nextInt(29) + 116) / 100.0f;
				break;
			case ENERGY_CLASS_E:
				tempbh = (tempr.nextInt(34) + 146) / 100.0f;
				break;
			case ENERGY_CLASS_F:
				tempbh = (tempr.nextInt(89) + 181) / 100.0f;
				break;
			default:
				tempbh = Float.NaN;
			}
		}
		else if (btid == ENERGY_BUILDINGTYPE_OFFICE)
		{
			switch(bhid)
			{
			case ENERGY_CLASS_A:
				tempbh = 0.55f;
				break;
			case ENERGY_CLASS_B:
				tempbh = (tempr.nextInt(24) + 56) / 100.0f;
				break;
			case ENERGY_CLASS_C:
				tempbh = (tempr.nextInt(29) + 81) / 100.0f;
				break;
			case ENERGY_CLASS_D:
				tempbh = (tempr.nextInt(29) + 116) / 100.0f;
				break;
			case ENERGY_CLASS_E:
				tempbh = (tempr.nextInt(34) + 146) / 100.0f;
				break;
			case ENERGY_CLASS_F:
				tempbh = (tempr.nextInt(89) + 181) / 100.0f;
				break;
			default:
				tempbh = Float.NaN;
			}
		}
		else 
		{
			tempbh = Float.NaN;
		}
		return tempbh;
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
	public static double setTemperatureDegreeDays(double base, double min, double max, boolean heat)
	{
		if(heat)
		{
			if(min > base)
			{
				return 0.0;
			}
			else if((min + max)/2 > base)
			{
				return (base - min)/4;
			}
			else if(max >= base)
			{
				return (base - min)/2 - (max - base)/4;
			}
			else if(max < base)
			{
				return base - (max + min)/2;
			}
		}
		else 
		{
			if(max < base)
			{
				return 0.0;
			}
			else if((max + min)/2 < base)
			{
				return (max - base)/4;
			}
			else if(min <= base)
			{
				return (max - base)/2 - (base - min)/4;
			}
			else if(min>base)
			{
				return (max + min)/2 - base;
			}
		}
		
		return Float.NaN;
	}
	
	/**
	 * Internal parser class which parses the content read from the database.
	 */
	private class ImpactParser
	{
		
		/**
		 * @param type
		 * @param content
		 */
		public Factor parseScale(int type, String content)
		{

				try 
				{
					if( type == Type.getInstance().getTypeID(IMPACT_WEATHER_STRING))
					{
						return parseWeatherInformation(content);
					}
					else if( type == Type.getInstance().getTypeID(IMPACT_BUILDING_STRING))
					{
						return parseHLCInformation(content);
					}
					else if( type == Type.getInstance().getTypeID(IMPACT_TEMPERATURE_STRING))
					{
						return parseTemperatureInformation(content);
					}
					else if( type == Type.getInstance().getTypeID(IMPACT_SUN_STRING))
					{
						return parseSunlightInformation(content);
					}
					else
					{
						throw new TypeIdNotFoundException();
					}
				} 
				catch (TypeIdNotFoundException e) 
				{
					e.printStackTrace();
				}
				return null;
		}
		
		private Factor parseSunlightInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				Factor tempFactor = new Factor();
								
				tempFactor.setSunLengthOfDay(calculateSunLengthOfDay(tempWD.getLatitude(), tempWD.getTimeSunrise()));	
				
				if(currentTime != null)
					tempFactor.setSunLight(isSunlight(tempWD.getTimeSunrise(), tempWD.getTimeSunset(), currentTime));
				else
					tempFactor.setSunLight(isSunlight(tempWD.getTimeSunrise(), tempWD.getTimeSunset()));
				
				
				tempFactor.setLatitude(tempWD.getLatitude());
				tempFactor.setLongitude(tempWD.getLongitude());
				tempFactor.setTimeFrom(tempWD.getLastUpdate());
				tempFactor.setTimeTo(tempWD.getNextUpdate());
				
				return tempFactor;
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
			return null;
		}
		
		//TODO: Fix this parser
		private Factor parseHLCInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				Factor tempFactor = new Factor();
				ArrayList<Forecast> tempCast = tempWD.getForecasts();		
				
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			return null;
		}
		
		private Factor parseTemperatureInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				ArrayList<Forecast> tempCast = tempWD.getForecasts();	
				Factor tempFactor = new Factor();
				
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
								
				tempFactor.setTemperatureAverage(tempAverage / tempCast.size());
				tempFactor.setTemperatureMax(tempMax);
				tempFactor.setTemperatureMin(tempMin);
				
				tempFactor.setLatitude(tempWD.getLatitude());
				tempFactor.setLongitude(tempWD.getLongitude());
				tempFactor.setTimeFrom(tempWD.getLastUpdate());
				tempFactor.setTimeTo(tempWD.getNextUpdate());
				
				//TODO: Fix the boolean input here to let the user specify if heating or cooling.
				tempFactor.setTemperatureDD(setTemperatureDegreeDays(theFactor.getTemperatureBase(), tempMin, tempMax, true));
				return tempFactor;
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * @param content
		 */
		private Factor parseWeatherInformation(String content)
		{
			try 
			{	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				Factor tempFactor = new Factor();
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
						tempFactor.setWeatherTemperature(f.getTemperatureValue());
						tempFactor.setWeatherWindSpeed(f.getWindspeedValue());
						tempFactor.setWeatherhPa(f.getPressureValue());
						
						tempFactor.setWeatherEffectiveTemperature(setEffectiveTemperature(f.getTemperatureValue(), f.getPressureValue(), f.getWindspeedValue())); 
					}
				}
				
				tempFactor.setLatitude(tempWD.getLatitude());
				tempFactor.setLongitude(tempWD.getLongitude());
				tempFactor.setTimeFrom(tempWD.getLastUpdate());
				tempFactor.setTimeTo(tempWD.getNextUpdate());
				
				tempFactor.setTemperatureAverage(tempAverage / tempCast.size());
				
				return tempFactor;	
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
			return null;
		}	
	}
}
