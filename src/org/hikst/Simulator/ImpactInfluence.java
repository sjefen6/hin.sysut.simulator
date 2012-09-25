package org.hikst.Simulator;

public class ImpactInfluence {
	
	// Direct influences
	public static String Effect_Infuence = "Effect";
	public static String Power_Consumption_Infuence = "Power_Consumption";
	public static String Voltage_Infuence = "Voltage";
	public static String Current_Infuence = "Current";
	
	//Indirect influences
	public static String Usage_Possebilety_Infuence = "Usage_Possebilety";
	
	private String Influence;
	private double Degree;
	private Boolean Percentage;
	
	public ImpactInfluence(String Influence, double Degree, Boolean Percentage){
		this.Influence = Influence;
		this.Degree = Degree;
		this.Percentage = Percentage;
	}
	
	public String getInfluence() {
		return Influence;
	}
	
	public double getDegree() {
		return Degree;
	}
	
	public Boolean getPercentage() {
		return Percentage;
	}
}
