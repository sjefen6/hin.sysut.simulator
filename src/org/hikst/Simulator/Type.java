package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Type
{
	
	private static Type instance;
	
	public static Type getInstance()
	{
		if(instance == null)
		{
			instance = new Type();
		}
		
		return instance;
	}
	
	private HashMap<String, Integer> typeList = new HashMap<String,Integer>();
	private HashMap<Integer, String> typeIdList = new HashMap<Integer,String>();
	
	private Type()
	{
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ID, Name FROM Type";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();
		
			while(set.next())
			{
				int id = set.getInt(1);
				String key = set.getString(2);
				
				typeList.put(key, id);
				typeIdList.put(id, key);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public int getTypeID(String type) throws TypeIdNotFoundException
	{
		if(typeList.containsKey(type))
		{
			return typeList.get(type);
		}
		else
		{
			throw new TypeIdNotFoundException();
		}
	}
	
	public String getType(Integer id) throws TypeNotFoundException
	{
		if(typeIdList.containsKey(id))
		{
			return typeIdList.get(id);
		}
		else
		{
			throw new TypeNotFoundException();
		}
	}
}
