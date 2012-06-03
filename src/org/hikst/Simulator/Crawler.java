package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Crawler 
{
	private int id;
	private Date last_seen_TS;
	private int status_id;
	
	private boolean active = true;
	
	public Crawler(int id) throws CrawlerNotFoundException
	{
		Connection connection = Settings.getDBC();
		
		String query = "SELECT ID, Last_Seen_TS, Status_ID FROM Crawler where ID=?";
		
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.id = set.getInt(1);
				last_seen_TS = new Date(set.getLong(2));
				status_id = set.getInt(3);
			}
			else
			{
				throw new CrawlerNotFoundException();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CrawlerNotFoundException();
		}
		
		new Thread(new Crawling());
	}
	
	public ArrayList<CrawlerRequest> getCrawlerRequests()
	{
		ArrayList<CrawlerRequest> crawlerRequests = new ArrayList<CrawlerRequest>();
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int id = set.getInt(1);
				int type_id = set.getInt(2);
				int longitude = set.getInt(3);
				int latitude = set.getInt(4);
				java.util.Date time_from = new Date(set.getLong(5));
				java.util.Date time_to = new Date(set.getLong(6));
				
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		
		return crawlerRequests;
	}
	
	private class Crawling implements Runnable
	{
		public void run()
		{		
			while(active)
			{
				ArrayList<CrawlerRequest> requests = getCrawlerRequests();
				
				for(int request = 0; request < requests.size(); request++)
				{
					processRequest(requests.get(request));
				}
			}	
		}
		
		private void processRequest(CrawlerRequest request)
		{
			try
			{
			
				String type = Type.getInstance().getType(request.getType());
			
				if(type.equals(Impact.IMPACT_WEATHER)){}
				
			}catch(TypeNotFoundException ex)
			{
				System.out.println("No type with the ID = \""+request.getType()+"\" was found..");
				ex.printStackTrace();
			}
			
		}
	}
	
}
