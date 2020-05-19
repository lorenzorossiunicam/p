package org.processmining.plugins.tsanalyzer;

public class TSAnalyzerSettings {
	
	private boolean time;
	private boolean frequency;
	
	public TSAnalyzerSettings() //default settings
	{
		this(true,false);
	}
	
	public TSAnalyzerSettings(boolean time, boolean frequency)
	{
		this.time = time;
		this.frequency = frequency;
	}
	
	public boolean getTime()
	{
		return time;
	}
	
	public boolean getFrequency()
	{
		return frequency;
	}
	

}
