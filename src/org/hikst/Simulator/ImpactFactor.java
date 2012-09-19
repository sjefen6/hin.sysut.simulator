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
	public static final int IMPACT_WEATHER = 1;
	
	private int type_id;
	private String content;
	
	private Calendar calendar;
	
	//Variables for sun related factors
	private Double lengthOfDay;
	private boolean sunlight;
	
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
	
	/**
	 * Calculates the number of daylight hours on the specified 
	 * day defined in the given time and at the given geographical latitude.
	 * 
	 * @param latitude Latitude in degrees
	 * @param time Date object with time data of the desired time.
	 */
	private void setLengthOfDay(double latitude, Date time)
	{
		calendar.setTime(time);
		int date = calendar.get(Calendar.DAY_OF_YEAR);
		
		double p = Math.asin(0.39795*Math.cos(0.2163108 + 2*Math.atan(0.9671396*Math.tan(0.00860*(date-186)))));
		
		lengthOfDay = 24 - (24/Math.PI)*Math.acos((Math.sin(0.8333*Math.PI/180)+
								Math.sin(latitude*Math.PI/180)*Math.sin(p))
								/(Math.cos(latitude*Math.PI/180)*Math.cos(p)));		
	}
	
	/**
	 * @return The length of this impactFactor's day in hours, if the length is not specified, returns Not a Number.
	 */
	public Double getHoursOfSunLight()
	{		
		if (lengthOfDay != null)
			return lengthOfDay;
		else
			return Double.NaN;
	}
	
	public void setSunlight(Date time)
	{
		calendar.setTime(time);
		double tempHours = getHoursOfSunLight();
		double tempTime = time.getHours() + TimeUnit.MINUTES.toHours(time.getMinutes());
		
		if(tempHours != Double.NaN)
		{
			if (tempTime > (12 - (tempHours/2)) && tempTime < (12 + tempHours/2))
			{
				sunlight = false;
			}
			else
				sunlight = false;
		}
		else
			sunlight = false;
	}
	
	public boolean getSunlight()
	{
		return sunlight;
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
				default:
				{
					return Float.NaN;
				}
			}
			
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
