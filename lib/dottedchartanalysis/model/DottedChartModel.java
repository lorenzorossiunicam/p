/*
 * Created on July. 02, 2007
 * 
 * Author: Minseok Song (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 * 
 * LICENSE WARNING: This code has been created within the realm of an STW
 * project. The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear. Before this code can be
 * released in any form, be it binary or source code, this issue has to be
 * clarified with the STW. Please do not add this file to any build or source
 * export transferred to anybody outside the TM.IS group.
 */

package org.processmining.plugins.dottedchartanalysis.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.XTimeBounds;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.dottedchartanalysis.LogUtils;
import org.processmining.plugins.dottedchartanalysis.model.guimap.GuiMap;
import org.processmining.plugins.dottedchartanalysis.model.guimap.GuiMapModel;
import org.processmining.plugins.dottedchartanalysis.ui.DottedChartOptionPanel;

public class DottedChartModel {

	public static final String STR_NONE = "None";
	public static final String ST_ORIG = "Originator";
	public static final String ST_TASK = "Task ID";
	public static final String ST_INST = "Instance ID";
	public static final String ST_EVEN = "Event";
	public static final String ST_DATA = "Data";
	public static final String ST_NONE = "None";
	//	public static final String NULL_VALUE = "NIL";
	public static final String STATISTICS_OVERALL = "Overall";

	// time options
	public static final String TIME_ACTUAL = "Actual";
	public static final String TIME_RELATIVE_TIME = "Relative(Time)";
	public static final String TIME_RELATIVE_RATIO = "Relative(Ratio)";
	public static final String TIME_LOGICAL = "Logical";
	public static final String TIME_LOGICAL_RELATIVE = "Logical(Relative)";

	// temporal objects
	private ArrayList<String> taskList = new ArrayList<String>();
	private ArrayList<String> instanceList = new ArrayList<String>();
	private ArrayList<String> originatorList = new ArrayList<String>();
	private ArrayList<String> eventList = new ArrayList<String>();

	protected long timeOffset = 1 * 3600000L;

	// arrays for time options
	protected long[] currentTime;
	protected long[] relativeTime;
	protected long[] relativeRatio;
	protected long[] logicalRelative;

	// sorting
	protected SortedMapModel sortedMapModel = new SortedMapModel(this);

	// code of each audit trail entry
	protected int[] taskCode;
	protected int[] instanceCode;
	protected int[] originatorCode;
	protected int[] eventCode;
	protected GuiMapModel guiMapModel = new GuiMapModel();

	protected long logMinValue = Long.MAX_VALUE;
	protected long logMaxValue = Long.MIN_VALUE;
	protected long logRelativeMaxValue = Long.MIN_VALUE;
	protected long logLogicalRelativeMaxValue = Long.MIN_VALUE;

	protected XLog inputLog;

	//	protected PluginContext context;

	// HV : Disabled statistics. 
	//	private ArrayList<DescriptiveStatistics> timeStatistics = null;
	//	private DescriptiveStatistics overallStatistics = null;
	//	private final StatisticsModel statisticsModel = new StatisticsModel(this);

	public DottedChartModel(PluginContext context, XLog aInputLog) {
		//		this.context = context;
		inputLog = aInputLog;
		construct(); //basic initialization
	}

	public XLog getLogReader() {
		return inputLog;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public long[] getCurrentTime() {
		return currentTime;
	}

	public long[] getRelativeTime() {
		return relativeTime;
	}

	public long[] getRelativeRatio() {
		return relativeRatio;
	}

	public long[] getLogicalRelative() {
		return logicalRelative;
	}

	public long[] getTimeList(String str) {
		long[] items = null;
		if (str.equals(TIME_ACTUAL)) {
			items = getCurrentTime();
		} else if (str.equals(TIME_RELATIVE_TIME)) {
			items = getRelativeTime();
		} else if (str.equals(TIME_RELATIVE_RATIO)) {
			items = getRelativeRatio();
		} else if (str.equals(TIME_LOGICAL_RELATIVE)) {
			items = getLogicalRelative();
		}
		return items;
	}

	public long getTime(String str, int index) {
		return getTimeList(str)[index];
	}

	public int[] getTaskCode() {
		return taskCode;
	}

	public int[] getInstanceCode() {
		return instanceCode;
	}

	public int[] getEventCode() {
		return eventCode;
	}

	public int[] getOrigiantorCode() {
		return originatorCode;
	}

	public String getInstance(int k) {
		try {
			return instanceList.get(instanceCode[k]);
		} catch (Exception e) {
			return LogUtils.NONAME;
		}
	}

	public String getTask(int k) {
		try {
			return taskList.get(taskCode[k]);
		} catch (Exception e) {
			return LogUtils.NONAME;
		}
	}

	public String getOriginator(int k) {
		try {
			return originatorList.get(originatorCode[k]);
		} catch (Exception e) {
			return LogUtils.NORESOURCE;
		}
	}

	public String getEvent(int k) {
		try {
			return eventList.get(eventCode[k]);
		} catch (Exception e) {
			return LogUtils.NOTRANSITION;
		}
	}

	public long getCurrentTime(int k) {
		return currentTime[k];
	}

	public SortedMapModel getSortedMapModel() {
		return sortedMapModel;
	}

	public int[] getCode(String str) {
		if (str.equals(ST_INST)) {
			return instanceCode;
		} else if (str.equals(ST_ORIG)) {
			return originatorCode;
		} else if (str.equals(ST_TASK)) {
			return taskCode;
		} else if (str.equals(ST_EVEN)) {
			return eventCode;
		}
		return null;
	}

	public ArrayList<String> getItemArrayList(String str) {
		if (str.equals(ST_INST)) {
			return instanceList;
		} else if (str.equals(ST_ORIG)) {
			return originatorList;
		} else if (str.equals(ST_TASK)) {
			return taskList;
		} else if (str.equals(ST_EVEN)) {
			return eventList;
		}
		return null;
	}

	public int getComponentSize(String str) {
		return getItemArrayList(str).size();
	}

	/**
	 * This method returns an available component list in dotted chart model.
	 * 
	 * @return ArrayList<String> AvailableComponentList
	 */
	public ArrayList<String> getAvailableComponentList() {
		ArrayList<String> list = new ArrayList<String>();
		Boolean flag = false;
		if (getItemArrayList(DottedChartModel.ST_TASK).size() > 0) {
			list.add(DottedChartModel.ST_TASK);
			flag = true;
		}
		if (getItemArrayList(DottedChartModel.ST_ORIG).size() > 0) {
			list.add(DottedChartModel.ST_ORIG);
			flag = true;
		}
		if (getItemArrayList(DottedChartModel.ST_INST).size() > 0) {
			list.add(DottedChartModel.ST_INST);
			flag = true;
		}
		if (getItemArrayList(DottedChartModel.ST_EVEN).size() > 0) {
			list.add(DottedChartModel.ST_EVEN);
			flag = true;
		}
		if (!flag) {
			list.add(DottedChartModel.ST_NONE);
		}
		return list;
	}

	public long getLogMinValueforScreen() {
		return (logMinValue + timeOffset) / 86400000L * 86400000L - timeOffset;
	}

	public long getLogMinValue() {
		return logMinValue;
	}

	public long getLogMaxValue() {
		return logMaxValue;
	}

	public long getLogRelativeMaxValue() {
		return logRelativeMaxValue;
	}

	public long getLogRelativeRatioMaxValue() {
		return 10000;
	}

	public long getLogLogiclRelativeMaxValue() {
		return logLogicalRelativeMaxValue;
	}

	// initialize all the required attributes
	public void initValues() {
		XLogInfo summary = XLogInfoFactory.createLogInfo(inputLog);
		XTimeBounds boundaries = summary.getLogTimeBoundaries();

		logMinValue = (boundaries.getStartDate() == null ? 0 : boundaries.getStartDate().getTime());
		logMaxValue = (boundaries.getEndDate() == null ? 0 : boundaries.getEndDate().getTime());

		int numATEs = summary.getNumberOfEvents();
		currentTime = new long[numATEs];
		relativeTime = new long[numATEs];
		relativeRatio = new long[numATEs];
		logicalRelative = new long[numATEs];

		taskCode = new int[numATEs];
		instanceCode = new int[numATEs];
		originatorCode = new int[numATEs];
		eventCode = new int[numATEs];

		// make list
		// originators
		XEventClasses originators = summary.getResourceClasses();
		originatorList = new ArrayList<String>();
		originators.getClasses();
		for (int k = 0; k < originators.getClasses().size(); k++) {
			originatorList.add(originators.getByIndex(k).toString());
		}
		originatorList.add(LogUtils.NORESOURCE);
		// tasks
		XEventClasses tasks = summary.getNameClasses();
		taskList = new ArrayList<String>();
		tasks.getClasses();
		for (int k = 0; k < tasks.getClasses().size(); k++) {
			taskList.add(tasks.getByIndex(k).toString());
		}
		taskList.add(LogUtils.NONAME);
		// events
		XEventClasses events = summary.getTransitionClasses();
		eventList = new ArrayList<String>();
		events.getClasses();
		for (int k = 0; k < events.getClasses().size(); k++) {
			eventList.add(events.getByIndex(k).toString());
		}
		eventList.add(LogUtils.NOTRANSITION);
		// instance
		instanceList = new ArrayList<String>();
		for (XTrace trace : inputLog) {
			instanceList.add(LogUtils.extractName(trace));
		}
		instanceList.add(LogUtils.NONAME);
		// initialize statistics
		//		statisticsModel.initStatistics();
	}

	/**
	 * construct dotted chart model
	 */
	public void construct() {
		// initialize 
		initValues();
	}

	public void constructMap(String key) {
		int num = 0;
		int num_instance = 0;

		for (XTrace trace : inputLog) {
			long firstTime = -1;
			long duration = -1;
			//			long current = -1;
			int startnum = num;
			int index = 0;
			for (XEvent event : trace) {
				//				XExtendedEvent xEvent = XExtendedEvent.wrap(event);

				instanceCode[num] = num_instance;
				taskCode[num] = taskList.indexOf(LogUtils.extractName(event));

				originatorCode[num] = originatorList.indexOf(LogUtils.extractResource(event));

				eventCode[num] = eventList.indexOf(LogUtils.extractTransition(event));

				Date timestamp = LogUtils.extractTimestamp(event);

				if (key.startsWith(TIME_LOGICAL_RELATIVE)) {

					// logical relative
					logicalRelative[num] = index++;

					if (key.equals(TIME_LOGICAL_RELATIVE + ST_INST)) {
						guiMapModel.makeLogicalMap(TIME_LOGICAL_RELATIVE + ST_INST, logicalRelative[num],
								String.valueOf(num_instance), num);
					} else if (key.equals(TIME_LOGICAL_RELATIVE + ST_ORIG)) {
						guiMapModel.makeLogicalMap(TIME_LOGICAL_RELATIVE + ST_ORIG, logicalRelative[num],
								String.valueOf(originatorCode[num]), num);
					} else if (key.equals(TIME_LOGICAL_RELATIVE + ST_EVEN)) {
						guiMapModel.makeLogicalMap(TIME_LOGICAL_RELATIVE + ST_EVEN, logicalRelative[num],
								String.valueOf(eventCode[num]), num);
					} else if (key.equals(TIME_LOGICAL_RELATIVE + ST_TASK)) {
						guiMapModel.makeLogicalMap(TIME_LOGICAL_RELATIVE + ST_TASK, logicalRelative[num],
								String.valueOf(taskCode[num]), num);
					}

					// for sorting // logical relative
					sortedMapModel.updateMinMaxValue(TIME_LOGICAL_RELATIVE, String.valueOf(num_instance),
							String.valueOf(originatorCode[num]), String.valueOf(eventCode[num]),
							String.valueOf(taskCode[num]), logicalRelative[num]);
				}

				if (timestamp == null) {
					currentTime[num] = -1;
					relativeTime[num] = -1;
					relativeRatio[num] = -1;
				} else {

					if (firstTime == -1) {
						firstTime = timestamp.getTime();
					}

					if (key.startsWith(TIME_ACTUAL)) {

						// for actual time
						currentTime[num] = timestamp.getTime();

						if (key.equals(TIME_ACTUAL + ST_INST)) {
							guiMapModel.makeActualMap(TIME_ACTUAL + ST_INST, timestamp.getTime(),
									String.valueOf(num_instance), num);
						} else if (key.equals(TIME_ACTUAL + ST_ORIG)) {
							guiMapModel.makeActualMap(TIME_ACTUAL + ST_ORIG, timestamp.getTime(),
									String.valueOf(originatorCode[num]), num);
						} else if (key.equals(TIME_ACTUAL + ST_EVEN)) {
							guiMapModel.makeActualMap(TIME_ACTUAL + ST_EVEN, timestamp.getTime(),
									String.valueOf(eventCode[num]), num);
						} else if (key.equals(TIME_ACTUAL + ST_TASK)) {
							guiMapModel.makeActualMap(TIME_ACTUAL + ST_TASK, timestamp.getTime(),
									String.valueOf(taskCode[num]), num);
						}

						// for sorting // actual
						sortedMapModel.updateMinMaxValue(TIME_ACTUAL, String.valueOf(num_instance),
								String.valueOf(originatorCode[num]), String.valueOf(eventCode[num]),
								String.valueOf(taskCode[num]), timestamp.getTime());
					}

					// relative time					
					relativeTime[num] = timestamp.getTime() - firstTime;

					if (duration < relativeTime[num]) {
						duration = relativeTime[num];
					}

					if (key.startsWith(TIME_RELATIVE_TIME)) {

						if (key.equals(TIME_RELATIVE_TIME + ST_INST)) {
							guiMapModel.makeRelativeTimeMap(TIME_RELATIVE_TIME + ST_INST, relativeTime[num],
									String.valueOf(num_instance), num);
						} else if (key.equals(TIME_RELATIVE_TIME + ST_ORIG)) {
							guiMapModel.makeRelativeTimeMap(TIME_RELATIVE_TIME + ST_ORIG, relativeTime[num],
									String.valueOf(originatorCode[num]), num);
						} else if (key.equals(TIME_RELATIVE_TIME + ST_EVEN)) {
							guiMapModel.makeRelativeTimeMap(TIME_RELATIVE_TIME + ST_EVEN, relativeTime[num],
									String.valueOf(eventCode[num]), num);
						} else if (key.equals(TIME_RELATIVE_TIME + ST_TASK)) {
							guiMapModel.makeRelativeTimeMap(TIME_RELATIVE_TIME + ST_TASK, relativeTime[num],
									String.valueOf(taskCode[num]), num);
						}

						// for sorting // relative
						sortedMapModel.updateMinMaxValue(TIME_RELATIVE_TIME, String.valueOf(num_instance),
								String.valueOf(originatorCode[num]), String.valueOf(eventCode[num]),
								String.valueOf(taskCode[num]), timestamp.getTime() - firstTime);
					}

					// statistics
					//					if (current == -1) {
					//						current = timestamp.getTime();
					//					} else {
					//						statisticsModel.addValue(TIME_ACTUAL, LogUtils.extractName(trace), timestamp.getTime()
					//								- current);
					//						current = timestamp.getTime();
					//					}
				}
				num++;
			}
			// for statistics
			//			statisticsModel.addValueOverAll(TIME_ACTUAL, current - firstTime);

			// relative time max
			for (int k = num - 1; k >= startnum; k--) {
				if (relativeTime[k] != -1) {
					duration = relativeTime[k];
					if (logRelativeMaxValue < duration) {
						logRelativeMaxValue = duration;
					}
					break;
				}
			}
			// logical relative time max
			if (logLogicalRelativeMaxValue < index) {
				logLogicalRelativeMaxValue = index;
			}
			num_instance++;
			// relative ratio
			if (duration == -1) {
				continue;
			}
			//			current = -1;
			for (int k = startnum; k < num; k++) {

				if (relativeTime[k] == -1) {
					continue;
				}

				if (duration == 0) {
					relativeRatio[k] = 0;
				} else {
					relativeRatio[k] = (int) (((float) relativeTime[k] / (float) duration) * 10000);
				}

				if (key.startsWith(TIME_RELATIVE_RATIO)) {

					if (key.equals(TIME_RELATIVE_RATIO + ST_INST)) {
						guiMapModel.makeRelativeRatioMap(TIME_RELATIVE_RATIO + ST_INST, relativeRatio[k],
								String.valueOf(num_instance - 1), k);
					} else if (key.equals(TIME_RELATIVE_RATIO + ST_ORIG)) {
						guiMapModel.makeRelativeRatioMap(TIME_RELATIVE_RATIO + ST_ORIG, relativeRatio[k],
								String.valueOf(originatorCode[k]), k);
					} else if (key.equals(TIME_RELATIVE_RATIO + ST_EVEN)) {
						guiMapModel.makeRelativeRatioMap(TIME_RELATIVE_RATIO + ST_EVEN, relativeRatio[k],
								String.valueOf(eventCode[k]), k);
					} else if (key.equals(TIME_RELATIVE_RATIO + ST_TASK)) {
						guiMapModel.makeRelativeRatioMap(TIME_RELATIVE_RATIO + ST_TASK, relativeRatio[k],
								String.valueOf(taskCode[k]), k);
					}

					// for sorting // relative ratio
					sortedMapModel.updateMinMaxValue(TIME_RELATIVE_RATIO, String.valueOf(num_instance - 1),
							String.valueOf(originatorCode[k]), String.valueOf(eventCode[k]),
							String.valueOf(taskCode[k]), relativeRatio[k]);
				}

				//				if (current != -1) {
				//					statisticsModel.addValue(TIME_RELATIVE_RATIO, LogUtils.extractName(trace), relativeRatio[k]
				//							- current);
				//				}

				//				current = relativeRatio[k];
			}
		}

		// add data to statistics
		//		statisticsModel.addValuesForOthers();
	}

	public HashMap<String, HashMap<String, ArrayList<Integer>>> getProperActualMap(String key, double milli2pixels) {
		String type = "month";
		long temp = (long) (1 / milli2pixels);

		if (temp <= 1000L) {
			type = "second";//hourMap;
		} else if (temp <= 60000L) {
			type = "minute";
		} else if (temp <= 1800000L) {
			type = "halfhour";
		} else if (temp <= 3600000L) {
			type = "hour";
		} else if (temp <= 86400000L) {
			type = "day";
		} else if (temp <= 2592000000L) {
			type = "week";
		}

		//		context.log("Map type = " + type, MessageLevel.DEBUG);

		GuiMap map = guiMapModel.getMap(key, type);

		if (map == null) {
			constructMap(key);
			map = guiMapModel.getMap(key, type);
		}

		return map.getMap();
	}

	public HashMap<String, HashMap<String, ArrayList<Integer>>> getProperTimeMap(String key, double milli2pixels) {
		if (key.startsWith(TIME_RELATIVE_RATIO)) {
			return getProperRelativeRatioMap(key, milli2pixels);
		} else if (key.startsWith(TIME_RELATIVE_TIME)) {
			return getProperRelativeTimeMap(key, milli2pixels);
		} else if (key.startsWith(TIME_ACTUAL)) {
			return getProperActualMap(key, milli2pixels);
		} else if (key.startsWith(TIME_LOGICAL_RELATIVE)) {
			return getLogicalMap(key);
		}
		return null;
	}

	public HashMap<String, HashMap<String, ArrayList<Integer>>> getProperRelativeTimeMap(String key, double milli2pixels) {
		String type = "day";
		long temp = (long) (1 / milli2pixels);
		if (temp <= 1000L) {
			type = "second";//hourMap;
		} else if (temp <= 60000L) {
			type = "minute";
		} else if (temp <= 1800000L) {
			type = "halfhour";
		} else if (temp <= 3600000L) {
			type = "hour";
		}

		//		context.log("Map type = " + type, MessageLevel.DEBUG);

		GuiMap map = guiMapModel.getMap(key, type);

		if (map == null) {
			constructMap(key);
			map = guiMapModel.getMap(key, type);
		}

		return map.getMap();
	}

	public HashMap<String, HashMap<String, ArrayList<Integer>>> getProperRelativeRatioMap(String key,
			double milli2pixels) {
		long temp = (long) (1 / milli2pixels);
		String type = "100";

		if (temp <= 1L) {
			type = "1";
		} else if (temp <= 5L) {
			type = "5";
		} else if (temp <= 10L) {
			type = "10";
		} else if (temp <= 50L) {
			type = "50";
		}

		GuiMap map = guiMapModel.getMap(key, type);

		if (map == null) {
			constructMap(key);
			map = guiMapModel.getMap(key, type);
		}

		return map.getMap();
	}

	public HashMap<String, HashMap<String, ArrayList<Integer>>> getLogicalMap(String key) {

		GuiMap map = guiMapModel.getMap(key, "");

		if (map == null) {
			constructMap(key);
			map = guiMapModel.getMap(key, "");
		}

		return map.getMap();
	}

	public long getProperTimeUnit(String timeOption, double milli2pixels) {
		if (timeOption.equals(TIME_RELATIVE_RATIO)) {
			return getRelativeRatioProperTimeUnit(milli2pixels);
		} else if (timeOption.equals(TIME_RELATIVE_TIME)) {
			return getRelativeProperTimeUnit(milli2pixels);
		} else if (timeOption.equals(TIME_ACTUAL)) {
			return getActualProperTimeUnit(milli2pixels);
		} else if (timeOption.equals(TIME_LOGICAL_RELATIVE)) {
			return 1;
		}
		return 1;
	}

	public long getActualProperTimeUnit(double milli2pixels) {
		long temp = (long) (1 / milli2pixels);
		if (temp <= 1000L) {
			return 1000L;
		} else if (temp <= 60000L) {
			return 60000L;
		} else if (temp <= 1800000L) {
			return 1800000L;
		} else if (temp <= 3600000L) {
			return 3600000L;
		} else if (temp <= 86400000L) {
			return 86400000L;
		} else if (temp <= 604800000L) {
			return 604800000L;
		}
		return 2592000000L;
		//		else if(temp<=2592000000L) return 2592000000L;
		//		return 604800000L;
	}

	public long getRelativeProperTimeUnit(double milli2pixels) {
		long temp = (long) (1 / milli2pixels);
		if (temp <= 1000L) {
			return 1000L;
		} else if (temp <= 60000L) {
			return 60000L;
		} else if (temp <= 1800000L) {
			return 1800000L;
		} else if (temp <= 3600000L) {
			return 3600000L;
		}
		return 86400000L;
	}

	public long getRelativeRatioProperTimeUnit(double milli2pixels) {
		long temp = (long) (1 / milli2pixels);
		if (temp <= 1L) {
			return 1;
		} else if (temp <= 5L) {
			return 5;
		} else if (temp <= 10L) {
			return 10;
		} else if (temp <= 50L) {
			return 50;
		} else {
			return 100;
		}
	}

	// HV: Disabled statistics. Results were off anyway, and it takes heaps of space and time.
	public void initTimeStatistics(String str) {
		//		timeStatistics = new ArrayList<DescriptiveStatistics>();
		//		for (int i = 0; i < getComponentSize(str) + 1; i++) {
		//			DescriptiveStatistics tempDS = new DescriptiveStatistics();
		//			timeStatistics.add(tempDS);
		//		}
		//		overallStatistics = new DescriptiveStatistics();
	}

	//	public ArrayList<DescriptiveStatistics> getTimeStatistics() {
	//		return timeStatistics;
	//	}

	//	public DescriptiveStatistics getOverallStatistics() {
	//		return overallStatistics;
	//	}

	//	public JPanel getStatisticsPanel(DottedChartOptionPanel dcop) {
	//		return statisticsModel.getPanel(dcop);
	//	}

	public void updateStatisticsPanel(DottedChartOptionPanel dcop) {
		//		statisticsModel.update(dcop);
	}

}
