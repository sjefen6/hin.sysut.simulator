package org.hikst.Simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User
{
	String Username;
	String Firstname;
	String Lastname;
	String email;
	String password;
	String salt;
	int access_level_ID;
	
	public User(String username) throws UserNotFoundException
	{
		Connection connection = Settings.getDBC();
		
		String query = "SELECT Username, Firstname, Lastname, Email, Password, Salt, Access_Level_ID FROM Users WHERE Username=?";
		
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);
			
			ResultSet set = statement.executeQuery();
			
			if(set.next())
			{
				this.Username = set.getString(1);
				this.Firstname = set.getString(2);
				this.Lastname = set.getString(3);
				this.email = set.getString(4);
				this.salt = set.getString(5);
				this.access_level_ID = set.getInt(6);
			}
			else
			{
				throw new UserNotFoundException();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserNotFoundException();
		}
		
		
	}
}
