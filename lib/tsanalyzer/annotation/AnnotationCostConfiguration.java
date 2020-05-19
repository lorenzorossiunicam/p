package org.processmining.plugins.tsanalyzer.annotation;

import java.text.NumberFormat;
import java.util.HashMap;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class AnnotationCostConfiguration implements AnnotationConfiguration {

	/**
	 * Key for the cost amount attribute.
	 */
	private static final String KEY_COSTELEMENT = "cost:element";
	/**
	 * Key for the cost amount attribute.
	 */
	public static final String KEY_COSTAMOUNT = "cost:amount";

	/**
	 * Partial Key for the cost type attribute.
	 */
	public static final String KEY_COSTTYPE = "cost:type";
	
	NumberFormat currencyFormat;

	public AnnotationCostConfiguration() {
		currencyFormat = NumberFormat.getCurrencyInstance();
	}
	
	@Override
	public long getMaxValue(XTrace trace) {
		Double instanceCost = getProcessInstanceCost(trace);
		return (long) (instanceCost * 100);
	}

	@Override
	public long getMinValue(XTrace trace) {
		return 0;
	}

	@Override
	public String getName(String name) {
		if (name.equalsIgnoreCase("remaining")) {
			return "Remaining costs";
		} else if (name.equalsIgnoreCase("elapsed")) {
			return "Spend costs";
		} else if (name.equalsIgnoreCase("duration")) {
			return "Total costs";
		}
		return null;
	}

	@Override
	public String getString(long value) {
		return currencyFormat.format(value);
	}

	@Override
	public long getValue(XTrace trace, int eventIndex) {
		Double activityCost = getEventCost(trace, eventIndex);
		return (long) (activityCost * 100);
	}

	/**
	 * Gets the total cost of a trace.
	 * 
	 * @param trace
	 *            the trace
	 * @return the cost of the trace
	 */
	private double getProcessInstanceCost(XTrace trace) {
		try {

			XAttribute costAttribute = trace.getAttributes().get(KEY_COSTELEMENT);
			if (costAttribute != null)
			{	Double cost = calculateTotalCostFromAttribute(costAttribute);
				return new Double(cost);

			}
		}
		catch (Exception ce) {
			System.out.println(ce.getMessage());
		}
		return -1;
	}

	/**
	 * Gets the total cost of an event.
	 * 
	 * @param trace
	 *            the trace
	 *  @param int 
	 *		    the index of the event
	 * @return the cost of the event
	 */
	private Double getEventCost(XTrace trace, int index) {
		try {

			XEvent event = trace.get(index);
			XAttribute costAttribute = event.getAttributes().get(KEY_COSTELEMENT);
			if (costAttribute != null)
			{
				double cost = calculateTotalCostFromAttribute(costAttribute);
				return new Double(cost);
			}
		}
		catch (Exception ce) {
			System.out.println(ce.toString());
		}
		return null;
	}

	/**
	 * Gets the total cost from a cost attribute.
	 * 
	 * @param costAttribute
	 *            
	 * @return the total cost
	 */

	private double calculateTotalCostFromAttribute(XAttribute costAttribute)
	{
		double totalCost = 0.0;
		HashMap <String,Double> costMap = getCostData(costAttribute);
		if (costMap != null)
		{ 	for(Double d: costMap.values())
			{
				totalCost += totalCost + d.doubleValue();
			}
		}
		return totalCost;

	}

	/**
	 * Gets the cost data from an attribute.
	 * 
	 * @param att
	 *            the attribute
	 * @return all the cost data (cost type, cost amount) of an attribute in the form of a hashmap or null
	 */
	
	private HashMap<String,Double> getCostData(XAttribute att)
	{ 
		HashMap<String, Double> costDataMap = new HashMap<String,Double>();
		if (att != null)
		{  
			XAttributeMap costAttributeMap = att.getAttributes();
			for (XAttribute x: costAttributeMap.values())
			{	if (x.getKey().startsWith(KEY_COSTTYPE))
				{
					String costType = ((XAttributeLiteral) x).getValue();
					XAttribute costAmtAttribute = x.getAttributes().get(KEY_COSTAMOUNT);
					String costString = ((XAttributeLiteral) costAmtAttribute).getValue();
					Double costAmt = Double.parseDouble(costString);
				//	System.out.println("mappings" + costType + " --- " + costString);
					costDataMap.put(costType,costAmt);
				}
			}
			return costDataMap;
		}
		//System.out.println("getCostData returns null");
		return null;
	}
}
