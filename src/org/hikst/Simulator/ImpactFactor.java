package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.DateTimeAtCompleted;

/**
 * TODO:
 */
public class ImpactFactor 
{
	public static final int IMPACT_SUN = 0;
	public static final int IMPACT_WEATHER = 1;
	public static final int IMPACT_TEMPERATURE = 2;
	public static final int IMPACT_BUILDING = 3;

	private int type_id;
	private String content;
	
	private Calendar calendar;
	
	//Variables for sun related factors
	private Double sunLengthOfDay;
	private boolean sunLight;
	private Date sunDate;
	
	//Variables and constants related to temperatures
	private double temperatureElasticity;
	private double temperatureReal;
	private double temperatureExpected;
	private double temperatureAverage;
	private double temperatureMin;
	private double temperatureMax;
	private double temperatureHDD;
	public static final double temperatureBaseResidential = 18.0;
	public static final double temperatureBaseOffice = 15.0;
	public static final double temperatureBaseIndustry = 15.0;
	
	//TODO: http://www.vesma.com/ddd/ddcalcs.html
	
	//Variables related to building type
	private boolean isResidential;
	private int inhabitans;
	
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
	public double getTemperatureElasticity()
	{
		return temperatureElasticity;
	}
	
	public double getTemperatureHDD()
	{
		return temperatureHDD;
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
		public float parseScale(int type,String content)
		{
			//parse content here
			switch(type)
			{
				case IMPACT_WEATHER:
				{
					return parseWeatherInformation(content);
				}
				case IMPACT_SUN:
				{
					return parseSunlightInformation(content);
				}
				default:
				{
					return Float.NaN;
				}
			}
			
		}
		
		//TODO:
		private float parseSunlightInformation(String content)
		{
			return Float.NaN;
		}
		
		/**
		 * @param content
		 * @return Not-a-Number float value
		 */
		private float parseWeatherInformation(String content)
		{
			return Float.NaN;
		}
		
	}
}
