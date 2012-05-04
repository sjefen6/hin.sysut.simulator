package org.hikst.Simulator;

import java.sql.Date;

public class CrawlerRequest
{
	int id;
	int type;
	int latitude;
	int longitude;
	java.sql.Date from;
	java.sql.Date to;
	
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
	public java.sql.Date getFrom() {
		return from;
	}
	public java.sql.Date getTo() {
		return to;
	}
	
	public CrawlerRequest(int id, int type, int latitude, int longitude,
			Date from, Date to) {
		super();
		this.id = id;
		this.type = type;
		this.latitude = latitude;
		this.longitude = longitude;
		this.from = from;
		this.to = to;
	}
	
	
}
