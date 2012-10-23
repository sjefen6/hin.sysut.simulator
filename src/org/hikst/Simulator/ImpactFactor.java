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

/**
 * TODO:
 */
public class ImpactFactor 
{
	public static final int IMPACT_SUN = 0;
	public static final int IMPACT_TEMPERATURE = 1;
	public static final int IMPACT_WEATHER = 2;
	public static final int IMPACT_BUILDING = 3;

	private int type_id;
	private String content;
	
	private Calendar calendar;
	private Object theObject;
	
	//Variables for sun related factors
	private Double sunLengthOfDay;
	private boolean sunLight;
	private Date sunDate;
	
	//Variables and constants related to temperatures and building type
	private double temperatureElasticity;
	private double temperatureReal;
	private double temperatureExpected;
	private double temperatureAverage;
	private double temperatureMin;
	private double temperatureMax;
	private double temperatureDD;
	private double temperatureHLC;		//temperature heat-loss coefficency
	public static final double temperatureBaseResidential = 18.0;
	public static final double temperatureBaseOffice = 15.0;
	public static final double temperatureBaseIndustry = 15.0;
	
	//TODO: http://www.vesma.com/ddd/index.htm
	
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
			calendar.setTime(sunDate);
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
	
	//TODO:
	
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
	
	public Double getTemperatureHLC()
	{
		return temperatureHLC;
	}
	
	/**
	 * Sets the heat-loss coefficency for a house or blockhouse with given
	 * energy class. Will generate a random coefficency within the energy
	 * class's levels, to create variety in the simulations. 
	 * 
	 * @param bhid Building class identifier
	 * @param btid Building type identifier
	 */
	private void setTemperatureHeatLossCoefficency(int bhid, int btid)
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
		temperatureHLC = (double)tempbh;
	}
	
	
	//TODO: Refine and define to work properly. There are expected bugs in this method
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
		temperatureMax = max;
		temperatureMin = min;
		if(heat)
		{
			if(min > base)
			{
				temperatureDD = 0;
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
				temperatureDD = 0;
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
	 *	TODO:
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

				try {
					if( type == Type.getInstance().getTypeID("IMPACT_WEATHER"))
					{
						parseWeatherInformation(content);

					}
					else if( type == Type.getInstance().getTypeID("IMPACT_BUILDING"))
					{
						
					}
					else if( type == Type.getInstance().getTypeID("IMPACT_TEMPERATURE"))
					{
						
					}
					else if( type == Type.getInstance().getTypeID("IMPACT_SUN"))
					{

					}
					else
					{

					}
				} catch (TypeIdNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
			
		
		
		//TODO:
		private float parseSunlightInformation(String content)
		{
			try {	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
								
				setSunLengthOfDay(tempWD.getLatitude(), tempWD.getTimeSunrise());	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Float.NaN;
		}
		
		//TODO
		private void parseHLCInformation(String content)
		{
			try {	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				
				ArrayList<Forecast> tempCast = tempWD.getForecasts();	
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//TODO
		/**
		 * @param content
		 * @return Not-a-Number float value
		 */
		private void parseWeatherInformation(String content)
		{
			try {	
				WeatherData tempWD = new WeatherData(new JSONObject(content));
				
				ArrayList<Forecast> tempCast = tempWD.getForecasts();
				

			
				
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
}
