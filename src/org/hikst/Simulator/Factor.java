package org.hikst.Simulator;

import java.util.Calendar;
import java.util.Date;

import org.hikst.Commons.Datatypes.Object;

public class Factor {

	private int typeId;
	
	private Object theObject;	
	
	private double longitude, latitude;
	private Date timeFrom, timeTo;
	
	//Variables for sun related factors
	private Double sunLengthOfDay;
	private boolean sunLight;
	private Date sunDate;
	
	//Variables and constants related to temperatures and building type
	private Double temperatureElasticity;
	private Double temperatureAverage;	//average temperature given in the forecasts
	private Double temperatureMin;		//min temperature given in the forecasts
	private Double temperatureMax;		//max temperature given in the forecasts 
	private Double temperatureDD;		//temperature degree days
	private Float temperatureHLC;		//temperature heat-loss coefficency
	private Double temperatureBase;		//desired temperature for object in this impact factor.
	private boolean temperatureHeat;	//TODO: implement
	
	private Double weatherTemperature;
	private Double weatherWindSpeed;
	private Double weatherEffectiveTemperature;
	private Double weatherhPa;
	
	public Factor()
	{
		
	}
	
	public String toString()
	{
		return typeId + ", " + sunLengthOfDay + ", " + sunLight + ", " + sunDate + ", " + temperatureElasticity
		 + ", " + temperatureAverage + ", " + temperatureMin + ", " + temperatureMax + ", " + temperatureDD + ", " + temperatureHLC
		 + ", " + temperatureBase + ", " + temperatureHeat + ", " + weatherTemperature + ", " + weatherWindSpeed + ", " + weatherEffectiveTemperature
		 + ", " + weatherhPa;
	}
	
	public Factor getFactor()
	{
		return this;
	}

	public void setTheObject(Object theObject) {
		this.theObject = theObject;
	}

	public Object getTheObject() {
		return theObject;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setTimeFrom(Date timeFrom) {
		this.timeFrom = timeFrom;
	}

	public Date getTimeFrom() {
		return timeFrom;
	}

	public void setTimeTo(Date timeTo) {
		this.timeTo = timeTo;
	}

	public Date getTimeTo() {
		return timeTo;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setSunLengthOfDay(Double sunLengthOfDay) {
		this.sunLengthOfDay = sunLengthOfDay;
	}

	public Double getSunLengthOfDay() {
		return sunLengthOfDay;
	}

	public void setSunLight(boolean sunLight) {
		this.sunLight = sunLight;
	}

	public boolean isSunLight() {
		return sunLight;
	}

	public void setSunDate(Date sunDate) {
		this.sunDate = sunDate;
	}

	public Date getSunDate() {
		return sunDate;
	}

	public void setTemperatureElasticity(Double temperatureElasticity) {
		this.temperatureElasticity = temperatureElasticity;
	}

	public Double getTemperatureElasticity() {
		return temperatureElasticity;
	}

	public void setTemperatureAverage(Double temperatureAverage) {
		this.temperatureAverage = temperatureAverage;
	}

	public Double getTemperatureAverage() {
		return temperatureAverage;
	}

	public void setTemperatureMin(Double temperatureMin) {
		this.temperatureMin = temperatureMin;
	}

	public Double getTemperatureMin() {
		return temperatureMin;
	}

	public void setTemperatureMax(Double temperatureMax) {
		this.temperatureMax = temperatureMax;
	}

	public Double getTemperatureMax() {
		return temperatureMax;
	}

	public void setTemperatureDD(Double temperatureDD) {
		this.temperatureDD = temperatureDD;
	}

	public Double getTemperatureDD() {
		return temperatureDD;
	}

	public void setTemperatureHLC(Float temperatureHLC) {
		this.temperatureHLC = temperatureHLC;
	}

	public Float getTemperatureHLC() {
		return temperatureHLC;
	}

	public void setTemperatureBase(Double temperatureBase) {
		this.temperatureBase = temperatureBase;
	}

	public Double getTemperatureBase() {
		return temperatureBase;
	}

	public void setTemperatureHeat(boolean temperatureHeat) {
		this.temperatureHeat = temperatureHeat;
	}

	public boolean isTemperatureHeat() {
		return temperatureHeat;
	}

	public void setWeatherTemperature(Double weatherTemperature) {
		this.weatherTemperature = weatherTemperature;
	}

	public Double getWeatherTemperature() {
		return weatherTemperature;
	}

	public void setWeatherWindSpeed(Double weatherWindSpeed) {
		this.weatherWindSpeed = weatherWindSpeed;
	}

	public Double getWeatherWindSpeed() {
		return weatherWindSpeed;
	}

	public void setWeatherEffectiveTemperature(
			Double weatherEffectiveTemperature) {
		this.weatherEffectiveTemperature = weatherEffectiveTemperature;
	}

	public Double getWeatherEffectiveTemperature() {
		return weatherEffectiveTemperature;
	}

	public void setWeatherhPa(Double weatherhPa) {
		this.weatherhPa = weatherhPa;
	}

	public Double getWeatherhPa() {
		return weatherhPa;
	}
	
}
