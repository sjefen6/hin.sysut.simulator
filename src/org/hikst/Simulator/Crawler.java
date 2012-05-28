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
	
	public Crawler(int id) throws CrawlerNotFoundException
	{
		Connection connection = Settings.getDBC();
		
		String query = "SELECT ID, Last_Seen_TS, Status_ID FROM Crawler where ID=?";
		
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(0, id);
			
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
	}
	
	public ArrayList<CrawlerRequest> getCrawlerRequests()
	{
		ArrayList<CrawlerRequest> crawlerRequests = new ArrayList<CrawlerRequest>();
		
		return crawlerRequests;
	}
}
