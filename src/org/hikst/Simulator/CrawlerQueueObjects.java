package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrawlerQueueObjects
{
	public static String Request_Pending = "Pending";
	public static String Request_Processing = "Processing";
	public static String Request_Finished = "Finished";
	
	int id;
	int type;
	int latitude;
	int longitude;
	java.util.Date from;
	java.util.Date to;
	
	public int getId() {
		return id;
	}
	public int getType() {
		return type;
	}
	public int getLatitude() {
		return latitude;
	}
	public int getLongitude() {
		return longitude;
	}
	public java.util.Date getFrom() {
		return from;
	}
	public java.util.Date getTo() {
		return to;
	}
	
	public CrawlerQueueObjects(int id, int type, int latitude, int longitude,
			Date from, Date to) {
		super();
		this.id = id;
		this.type = type;
		this.latitude = latitude;
		this.longitude = longitude;
		this.from = from;
		this.to = to;
	}
	
	public CrawlerQueueObjects(int id) throws CrawlerRequestNotFoundException
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ID, Type_ID, Latitude, Longitude, Time_From,Time_To, Status_ID WHERE ID=?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();
		
			if(set.next())
			{
				this.id = set.getInt(1);
				this.type = set.getInt(2);
				this.longitude = set.getInt(3);
				this.latitude = set.getInt(4);
				this.from = new java.util.Date(set.getLong(5));
				this.to = new java.util.Date(set.getInt(6));
			}
			else
			{
				throw new CrawlerRequestNotFoundException();
			}
		}catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void setStatusToProcessing()
	{
		try
		{
			Connection connection = Settings.getDBC();
			
			int statusID = Status.getInstance().getStatusID(Request_Processing);
			
			String query = "UPDATE TABLE Simulator_Queue_Objects SET Status_ID=? WHERE ID=?";
			
			PreparedStatement statement =  connection.prepareStatement(query);
			statement.setInt(1, statusID);
			statement.setInt(2, id);
			statement.executeUpdate();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		catch(StatusIdNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void setStatusToFinished()
	{
		try
		{
			Connection connection = Settings.getDBC();
			
			int statusID = Status.getInstance().getStatusID(Request_Finished);
			
			String query = "UPDATE TABLE Simulator_Queue_Objects SET Status_ID=? WHERE ID=?";
			
			PreparedStatement statement =  connection.prepareStatement(query);
			statement.setInt(1, statusID);
			statement.setInt(2, id);
			statement.executeUpdate();
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		catch(StatusIdNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}
}
