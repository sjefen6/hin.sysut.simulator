package org.hikst.Simulator;

public class Impact
{
	public static final int IMPACT_WEATHER = 1;
	
	float percent;
	int type;
	float scale;
	
	public float getPercent() {
		return percent;
	}
	
	public void setPercent(float percent) {
		this.percent = percent;
	}
	
	public float getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	
	
	public Impact(float percent, int type,String content) {
		super();
		this.percent = percent;
		this.type = type;
		
		//maybe make a static instance of ImpactParser instead...
		this.scale = new ImpactParser().parseScale(type, content);
	}
	
	private class ImpactParser
	{
		public float parseScale(int type,String content)
		{
			//parse content here
			switch(type)
			{
				case IMPACT_WEATHER:
				{
					return parseWeatherInformation(content);
				}
				default:
				{
					return Float.NaN;
				}
			}
			
		}
		
		private float parseWeatherInformation(String content)
		{
			return Float.NaN;
		}
		
	}
}
