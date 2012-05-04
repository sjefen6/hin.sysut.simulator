package org.hikst.Simulator;

import java.util.ArrayList;

//Objects used for simulations 
public class SimulatorObject 
{
	private int ID;
	private String name;
	private float effect;
	
	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public float getEffect() {
		return effect;
	}
	
	public SimulatorObject(int iD, String name, float effect) {
		super();
		ID = iD;
		this.name = name;
		this.effect = effect;
	}
}
