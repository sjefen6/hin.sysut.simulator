package org.hikst.Commons.Statics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.hikst.Commons.Exceptions.StatusIdNotFoundException;
import org.hikst.Commons.Exceptions.StatusNotFoundException;
import org.hikst.Commons.Services.Settings;

public class Status 
{
	private static Status instance;
	
	public static Status getInstance()
	{
		if(instance == null)
		{
			instance = new Status();
		}
		
		return instance;
	}
	
	private HashMap<String, Integer> statusList = new HashMap<String,Integer>();
	private HashMap<Integer, String> statusIdList = new HashMap<Integer,String>();
	
	private Status()
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ID, Name FROM Status";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();
		
			while(set.next())
			{
				int id = set.getInt(1);
				String key = set.getString(2);
				
				statusList.put(key, id);
				statusIdList.put(id, key);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public int getStatusID(String status) throws StatusIdNotFoundException
	{
		if(statusList.containsKey(status))
		{
			return statusList.get(status);
		}
		else
		{
			throw new StatusIdNotFoundException();
		}
	}
	
	public String getStatus(Integer id) throws StatusNotFoundException
	{
		if(statusIdList.containsKey(id))
		{
			return statusIdList.get(id);
		}
		else
		{
			throw new StatusNotFoundException();
		}
	}
	
}
