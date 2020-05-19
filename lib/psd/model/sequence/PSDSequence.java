package org.processmining.plugins.psd.model.sequence;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.psd.model.PSDArrow;
import org.processmining.plugins.psd.model.PSDBlock;
import org.processmining.plugins.psd.model.PSDLifeLine;
import org.processmining.plugins.psd.utils.PSDLogUtils;

public class PSDSequence {

	/*
	 * Contains a sorted list data-element blocks, that is sorted on if
	 * flexible-equivalent: data-element name (alphabetically), in case of
	 * multiple blocks of the same data-element name then also on begin time, if
	 * still multiples, then also on end time. (Sorted ascendingly) if
	 * strict-equivalent: on begin time, end time and finally on data-element
	 */
	private List<PSDSequenceBlock> sortedDataEltBlocks = new ArrayList<PSDSequenceBlock>();
	/*
	 * Contains a sorted list data-element blocks, but only if strict-equivalent
	 * selected. Sorted on, end time, begin time and finally on data-element
	 */
	private List<PSDSequenceBlock> sortedOnEndDataEltBlocks = new ArrayList<PSDSequenceBlock>();
	/**
	 * ArrayList containing the arrows that appear in the sequence, sorted first
	 * on source data-element name of the arrow (alphabetically), if there are
	 * multiple arrows with the same source, then also sorted on the destination
	 * data-element name. (Sorted ascendingly)
	 * 
	 */
	private List<PSDSequenceArrow> arrowList = new ArrayList<PSDSequenceArrow>();
	/**
	 * date at which the sequence begins
	 */
	private Date beginDate;
	/**
	 * date at which the sequence ends
	 */
	private Date endDate;
	/**
	 * process instance in which the sequence appeared
	 */
	private String piName;
	/**
	 * Color of the sequence
	 */
	private Color thisColor = Color.RED;
	/**
	 * Start point at which the sequence is to be drawn
	 */
	private double startY = 60;
	/**
	 * End point at which the sequence is to be drawn
	 */
	private double endY = 60;

	/**
	 * Constructor to initialize the sequence
	 * 
	 * @param beginDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @param piName
	 *            String
	 */
	public PSDSequence(Date beginDate, Date endDate, String piName) {
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.piName = piName;
	}

	///////////////////////INITIALIZATION/SORTING METHODS////////////////////
	/**
	 * This method initializes the sequence, and sorts the arrows and
	 * data-element blocks in such a manner that it can be easily compared with
	 * other sequences
	 * 
	 * @param relationList
	 *            ArrayList: contains relations (arraylist of 2 audit trail
	 *            entries (ates) ) between ates where the first ate corresponds
	 *            to an event that is a direct precessor of the event that
	 *            corresponds to the second ate.
	 * @param dataEltType
	 *            String : the data-element type that is used
	 * @param strict
	 *            boolean : true if 'strict' patterns used, false if 'flexible'.
	 */
	public void initializeSequence(List<XEvent[]> relationList, String dataEltType, boolean strict) {
		//initialize arrowList
		arrowList = new ArrayList<PSDSequenceArrow>();
		//initialize map that maps ates to the set of similar ates
		Map<XEvent, Set<XEvent>> ateToSetMap = new HashMap<XEvent, Set<XEvent>>();
		//initialize map that maps ates to the set of arrows that originates from
		//the data-element block of which the ate is part
		Map<XEvent, Set<PSDArrow>> sourceAteToArrow = new HashMap<XEvent, Set<PSDArrow>>();
		//initialize map that maps ates to the set of arrows that end in the
		//data-element block of which the ate is part
		Map<XEvent, Set<PSDArrow>> destAteToArrow = new HashMap<XEvent, Set<PSDArrow>>();
		ListIterator<XEvent[]> rels = relationList.listIterator();
		//run through relationList
		while (rels.hasNext()) {
			XEvent[] rel = rels.next();
			Set<XEvent> firstSet = ateToSetMap.get(rel[0]);
			Set<XEvent> secondSet = ateToSetMap.get(rel[1]);
			String first = PSDLogUtils.getEltOfAte(rel[0], dataEltType);
			String second = PSDLogUtils.getEltOfAte(rel[1], dataEltType);
			//compare data-elts
			if (first != null && second != null && !first.equals(second)) {
				// the dataElts are different, so add arrow from the one elt to the other
				PSDSequenceArrow arrow = new PSDSequenceArrow(PSDLogUtils.getTimestamp(rel[0]),
						PSDLogUtils.getTimestamp(rel[1]), first, second);
				arrowList.add(arrow);
				Set<PSDArrow> arrowSet0 = new HashSet<PSDArrow>();
				if (sourceAteToArrow.containsKey(rel[0])) {
					arrowSet0 = sourceAteToArrow.get(rel[0]);
				}
				arrowSet0.add(arrow);
				sourceAteToArrow.put(rel[0], arrowSet0);
				Set<PSDArrow> arrowSet1 = new HashSet<PSDArrow>();
				if (destAteToArrow.containsKey(rel[1])) {
					arrowSet1 = destAteToArrow.get(rel[1]);
				}
				arrowSet1.add(arrow);
				destAteToArrow.put(rel[1], arrowSet1);
				if (firstSet == null) {
					//create new set
					firstSet = new HashSet<XEvent>();
					firstSet.add(rel[0]);
					//add it to the map
					ateToSetMap.put(rel[0], firstSet);
				}
				if (secondSet == null) {
					//create new set
					secondSet = new HashSet<XEvent>();
					secondSet.add(rel[1]);
					//add it to the map
					ateToSetMap.put(rel[1], secondSet);
				}
			} else if (first != null && second != null) {
				//data-Elts are equal
				if (firstSet == null && secondSet == null) {
					//both sets do not yet exist, create new set
					firstSet = new HashSet<XEvent>();
					firstSet.add(rel[0]);
					firstSet.add(rel[1]);
				} else if (secondSet == null) {
					//secondSet does not yet exist, add second ate to firstSet
					firstSet.add(rel[1]);
				} else if (firstSet == null) {
					firstSet = secondSet;
					//firstSet does not yet exist, add second ate to secondSet
					firstSet.add(rel[0]);
				} else {
					//both sets do exist, unite
					firstSet.addAll(secondSet);
				}
				//Make sure all ates in the set are mapped to the same set
				Iterator<XEvent> it = firstSet.iterator();
				while (it.hasNext()) {
					ateToSetMap.put(it.next(), firstSet);
				}
			}
		}
		if (!strict) {
			//sort arrowList by: source, destination, begin time, end time (in that order)
			sortArrowList();
		} else {
			//sort arrowList by: begin time, end time (in that order), source, destination
			sortArrowListStrict();
		}
		createBlocks(dataEltType, ateToSetMap, sourceAteToArrow, destAteToArrow, strict);
	}

	/**
	 * Sorts arrowList on source data-element name, destination data-element
	 * name begin time and end time (in that order)
	 */
	public void sortArrowList() {
		@SuppressWarnings("unchecked")
		List<PSDSequenceArrow> originalList = (List<PSDSequenceArrow>) ((ArrayList) arrowList).clone();
		List<String> sources = new ArrayList<String>();
		Map<String, Set<Integer>> sourceToArrows = new HashMap<String, Set<Integer>>();
		for (int i = 0; i < arrowList.size(); i++) {
			String source = originalList.get(i).getSource();

			if (!sources.contains(source)) {
				sources.add(source);
			}
			Set<Integer> arrowSet = new HashSet<Integer>();
			if (sourceToArrows.containsKey(source)) {
				arrowSet = sourceToArrows.get(source);
			}
			arrowSet.add(i);
			sourceToArrows.put(source, arrowSet);
		}
		int index = 0;
		//sort list of sources
		Collections.sort(sources);
		Iterator<String> lit = sources.listIterator();
		//run through begin times
		while (lit.hasNext()) {
			String source = lit.next();
			Set<Integer> prts = sourceToArrows.get(source);
			if (prts != null) {
				//initialize to sort on destination
				List<String> destList = new ArrayList<String>();
				Map<String, Set<Integer>> destToArrows = new HashMap<String, Set<Integer>>();
				Iterator<Integer> elts = prts.iterator();
				while (elts.hasNext()) {
					int ind = elts.next().intValue();
					String dest = originalList.get(ind).getDestination();
					if (!destList.contains(dest)) {
						destList.add(dest);
					}
					Set<Integer> destSet = new HashSet<Integer>();
					if (destToArrows.containsKey(dest)) {
						destSet = destToArrows.get(dest);
					}
					destSet.add(ind);
					destToArrows.put(dest, destSet);
				}
				//sort on destination name (alphabetically)
				Collections.sort(destList);
				Iterator<String> lit0 = destList.listIterator();
				//run through sorted destination list
				while (lit0.hasNext()) {
					String dest = lit0.next();
					Set<Integer> ars = destToArrows.get(dest);
					if (ars != null) {
						//initialize sort on begin time
						List<Date> beginTimeList = new ArrayList<Date>();
						Map<Date, Set<Integer>> beginTimeToArrows = new HashMap<Date, Set<Integer>>();
						Iterator<Integer> arcs = ars.iterator();
						while (arcs.hasNext()) {
							int ind = arcs.next().intValue();
							Date beginTime = originalList.get(ind).getBeginTimestamp();
							if (!beginTimeList.contains(beginTime)) {
								beginTimeList.add(beginTime);
							}
							Set<Integer> beginTimeSet = new HashSet<Integer>();
							if (beginTimeToArrows.containsKey(beginTime)) {
								beginTimeSet = beginTimeToArrows.get(beginTime);
							}
							beginTimeSet.add(ind);
							beginTimeToArrows.put(beginTime, beginTimeSet);
						}
						Collections.sort(beginTimeList);
						Iterator<Date> lit1 = beginTimeList.listIterator();
						//run through sorted begin time list
						while (lit1.hasNext()) {
							Date beginTime = lit1.next();
							Set<Integer> indices = beginTimeToArrows.get(beginTime);
							if (indices != null) {
								//initialize sort on end time
								List<Date> endTimeList = new ArrayList<Date>();
								Map<Date, Set<Integer>> endTimeToArrows = new HashMap<Date, Set<Integer>>();
								Iterator<Integer> iterator = indices.iterator();
								while (iterator.hasNext()) {
									int ind = iterator.next().intValue();
									Date endTime = originalList.get(ind).getEndTimestamp();
									if (!endTimeList.contains(endTime)) {
										endTimeList.add(endTime);
									}
									Set<Integer> endSet = new HashSet<Integer>();
									if (endTimeToArrows.containsKey(dest)) {
										endSet = endTimeToArrows.get(endTime);
									}
									endSet.add(ind);
									endTimeToArrows.put(endTime, endSet);
								}
								Collections.sort(endTimeList);
								Iterator<Date> lit2 = endTimeList.listIterator();
								//run through sorted end time list
								while (lit2.hasNext()) {
									Date endTime = lit2.next();
									Set<Integer> endSet = endTimeToArrows.get(endTime);
									Iterator<Integer> it7 = endSet.iterator();
									while (it7.hasNext()) {
										int ind = it7.next().intValue();
										//place arrow in new (sorted) location in arrowList
										arrowList.set(index++, originalList.get(ind));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sorts arrowList on begin time, end time source data-element name,
	 * destination data-element name(in that order)
	 */
	public void sortArrowListStrict() {
		List<PSDSequenceArrow> originalList = (List<PSDSequenceArrow>) ((ArrayList) arrowList).clone();
		List<Date> beginTimeList = new ArrayList<Date>();
		Map<Date, Set<Integer>> beginTimeToArrows = new HashMap<Date, Set<Integer>>();
		for (int i = 0; i < arrowList.size(); i++) {
			Date beginTime = originalList.get(i).getBeginTimestamp();
			if (!beginTimeList.contains(beginTime)) {
				beginTimeList.add(beginTime);
			}
			Set<Integer> beginTimeSet = new HashSet<Integer>();
			if (beginTimeToArrows.containsKey(beginTime)) {
				beginTimeSet = beginTimeToArrows.get(beginTime);
			}
			beginTimeSet.add(i);
			beginTimeToArrows.put(beginTime, beginTimeSet);
		}
		int index = 0;
		Collections.sort(beginTimeList);
		Iterator<Date> lit1 = beginTimeList.listIterator();
		//run through sorted begin time list
		while (lit1.hasNext()) {
			Date beginTime = lit1.next();
			Set<Integer> indices = beginTimeToArrows.get(beginTime);
			if (indices != null) {
				//initialize sort on end time
				List<Date> endTimeList = new ArrayList<Date>();
				Map<Date, Set<Integer>> endTimeToArrows = new HashMap<Date, Set<Integer>>();
				Iterator<Integer> iterator = indices.iterator();
				while (iterator.hasNext()) {
					int ind = iterator.next().intValue();
					Date endTime = originalList.get(ind).getEndTimestamp();
					if (!endTimeList.contains(endTime)) {
						endTimeList.add(endTime);
					}
					Set<Integer> endTimeSet = new HashSet<Integer>();
					if (endTimeToArrows.containsKey(endTime)) {
						endTimeSet = endTimeToArrows.get(endTime);
					}
					endTimeSet.add(ind);
					endTimeToArrows.put(endTime, endTimeSet);
				}
				Collections.sort(endTimeList);
				Iterator<Date> lit2 = endTimeList.listIterator();
				//run through sorted end time list
				while (lit2.hasNext()) {
					Date endTime = lit2.next();
					Set<Integer> endTimes = endTimeToArrows.get(endTime);
					if (endTimes != null) {
						//initialize sort on sources
						List<String> sources = new ArrayList<String>();
						Map<String, Set<Integer>> sourceToArrows = new HashMap<String, Set<Integer>>();
						Iterator<Integer> prts = endTimes.iterator();
						while (prts.hasNext()) {
							int ind = prts.next().intValue();
							String source = originalList.get(ind).getSource();
							if (!sources.contains(source)) {
								sources.add(source);
							}
							Set<Integer> arrowSet = new HashSet<Integer>();
							if (sourceToArrows.containsKey(source)) {
								arrowSet = sourceToArrows.get(source);
							}
							arrowSet.add(ind);
							sourceToArrows.put(source, arrowSet);
						}
						Collections.sort(sources);
						Iterator<String> srcs = sources.listIterator();
						//run through sorted end time list
						while (srcs.hasNext()) {
							String source = srcs.next();
							Set<Integer> sourceSet = sourceToArrows.get(source);
							if (sourceSet != null) {
								//initialize sort on destination
								List<String> destList = new ArrayList<String>();
								Map<String, Set<Integer>> destToArrows = new HashMap<String, Set<Integer>>();
								Iterator<Integer> elts = sourceSet.iterator();
								while (elts.hasNext()) {
									int ind = elts.next().intValue();
									String dest = originalList.get(ind).getDestination();
									if (!destList.contains(dest)) {
										destList.add(dest);
									}
									Set<Integer> destSet = new HashSet<Integer>();
									if (destToArrows.containsKey(dest)) {
										destSet = destToArrows.get(dest);
									}
									destSet.add(ind);
									destToArrows.put(dest, destSet);
								}
								//sort on destination name (alphabetically)
								Collections.sort(destList);
								Iterator<String> lit0 = destList.listIterator();
								//run through sorted destination list
								while (lit0.hasNext()) {
									String dst = lit0.next();
									Set<Integer> dstSet = destToArrows.get(dst);
									Iterator<Integer> it7 = dstSet.iterator();
									while (it7.hasNext()) {
										int ind = it7.next().intValue();
										//place arrow in new (sorted) location in arrowList
										arrowList.set(index++, originalList.get(ind));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Creates and initializes the data-element blocks that are part of this
	 * sequence, stores them in the sortedDataEltBlocks list and sorts this
	 * list.
	 * 
	 * @param dataEltType
	 *            String
	 * @param ateToSetMap
	 *            HashMap
	 * @param sourceAteToArrow
	 *            HashMap
	 * @param destAteToArrow
	 *            HashMap
	 * @param strict
	 *            boolean
	 */
	private void createBlocks(String dataEltType, Map<XEvent, Set<XEvent>> ateToSetMap,
			Map<XEvent, Set<PSDArrow>> sourceAteToArrow, Map<XEvent, Set<PSDArrow>> destAteToArrow, boolean strict) {
		// initialize list, which is to contain the data-element blocks that exist
		// in this sequence
		Set<PSDSequenceBlock> blockSet = new HashSet<PSDSequenceBlock>();
		Map<String, Set<PSDSequenceBlock>> blockToSetOfSimilar = new HashMap<String, Set<PSDSequenceBlock>>();
		//set to prevent double work
		Set<Set<XEvent>> testSet = new HashSet<Set<XEvent>>();
		//run through values of ateToSetMap, to check from what Timestamp to what
		//Timestamp a case 'spends in' a data element-part
		Iterator<Set<XEvent>> values = ateToSetMap.values().iterator();
		while (values.hasNext()) {
			Set<XEvent> currentSet = values.next();
			//HashMap can contain double occurences of values. Only run through each set once
			if (!testSet.contains(currentSet)) {
				testSet.add(currentSet);
				Date firstDate = null, secondDate = null;
				//initialize set that is to contain the arrows that originate from the new data-element block
				Set<PSDArrow> outArrowSet = new HashSet<PSDArrow>();
				//initialize set that is to contain the arrows that end in the new data-element block
				Set<PSDArrow> inArrowSet = new HashSet<PSDArrow>();
				//walk through set to get begin and end time
				Iterator<XEvent> it = currentSet.iterator();
				String dataElement = "";
				while (it.hasNext()) {
					XEvent at = it.next();
					Date time = PSDLogUtils.getTimestamp(at);
					if (time != null) {
						if (firstDate == null || time.before(firstDate)) {
							firstDate = time;
						}
						if (secondDate == null || time.after(secondDate)) {
							secondDate = time;
						}
					}
					//get dataElt
					String tempStr = PSDLogUtils.getEltOfAte(at, dataEltType);
					if (tempStr != null) {
						dataElement = tempStr;
					}
					if (sourceAteToArrow.containsKey(at)) {
						outArrowSet.addAll(sourceAteToArrow.get(at));
					}
					if (destAteToArrow.containsKey(at)) {
						inArrowSet.addAll(destAteToArrow.get(at));
					}
				}
				if (firstDate != null && secondDate != null) {
					PSDSequenceBlock block = new PSDSequenceBlock(firstDate, secondDate, dataElement);
					Set<PSDSequenceBlock> similarBlocks = new HashSet<PSDSequenceBlock>();
					if (blockToSetOfSimilar.containsKey(dataElement)) {
						similarBlocks = blockToSetOfSimilar.get(dataElement);
						//walk through similar blocks to check whether they overlap
						Iterator<PSDSequenceBlock> simBlocks = similarBlocks.iterator();
						while (simBlocks.hasNext()) {
							PSDSequenceBlock bl = simBlocks.next();
							boolean overlapping = false;
							if (firstDate.getTime() >= bl.getBeginTimestamp().getTime()
									&& firstDate.getTime() <= bl.getEndTimestamp().getTime()) {
								//combine overlapping blocks
								blockSet.remove(bl);
								block.setBeginTimestamp(bl.getBeginTimestamp());
								outArrowSet.addAll(bl.getOutArrows());
								inArrowSet.addAll(bl.getInArrows());
								overlapping = true;
							}
							if (secondDate.getTime() >= bl.getBeginTimestamp().getTime()
									&& secondDate.getTime() <= bl.getEndTimestamp().getTime()) {
								//combine overlapping blocks
								block.setEndTimestamp(bl.getEndTimestamp());
								if (!overlapping) {
									blockSet.remove(bl);
									outArrowSet.addAll(bl.getOutArrows());
									inArrowSet.addAll(bl.getInArrows());
								}
							}
						}
					}
					similarBlocks.add(block);
					blockToSetOfSimilar.put(dataElement, similarBlocks);
					block.setInArrows(inArrowSet);
					block.setOutArrows(outArrowSet);
					blockSet.add(block);
					Iterator<PSDArrow> inArrows = inArrowSet.iterator();
					while (inArrows.hasNext()) {
						PSDArrow inArrow = inArrows.next();
						inArrow.setDestinationBlock(block);
					}
					Iterator<PSDArrow> outArrows = outArrowSet.iterator();
					while (outArrows.hasNext()) {
						PSDArrow outArrow = outArrows.next();
						outArrow.setSourceBlock(block);
					}
				}
			}
		}
		if (!strict) {
			sortBlocks(blockSet);
		} else {
			sortBlocksStrictEnd(blockSet);
			sortBlocksStrictBegin(blockSet);
		}
	}

	/**
	 * This methods sorts the sortedDataEltBlocks ascendingly on data-element,
	 * in case of more data-element blocks of same data-element also on, begin
	 * time ascendingly
	 * 
	 * @param blockSet
	 *            HashSet : set containing the blocks in this sequence
	 */
	private void sortBlocks(Set<PSDSequenceBlock> blockSet) {
		sortedDataEltBlocks = new ArrayList<PSDSequenceBlock>();
		List<String> eltList = new ArrayList<String>();
		Map<String, Set<Integer>> eltToPartsMap = new HashMap<String, Set<Integer>>();
		Iterator<PSDSequenceBlock> it = blockSet.iterator();
		while (it.hasNext()) {
			PSDSequenceBlock bl = it.next();
			sortedDataEltBlocks.add(bl);
			//put data-element in eltList for sorting purposes
			if (!eltList.contains(bl.getDataElement())) {
				eltList.add(bl.getDataElement());
			}
			Set<Integer> partSet1 = new HashSet<Integer>();
			if (eltToPartsMap.containsKey(bl.getDataElement())) {
				partSet1 = eltToPartsMap.get(bl.getDataElement());
			}
			//store current index in partSet
			partSet1.add(sortedDataEltBlocks.size() - 1);
			//and map data element to the partSet
			eltToPartsMap.put(bl.getDataElement(), partSet1);
		}
		//get original sortedDataEltBlocks
		List<PSDSequenceBlock> originalList = (List<PSDSequenceBlock>) ((ArrayList) sortedDataEltBlocks).clone();
		int index = 0;
		//sort blocks on data element name (alphabetically ASC)
		Collections.sort(eltList);
		Iterator<String> lit = eltList.listIterator();
		//run through data-element
		while (lit.hasNext()) {
			String elt = lit.next();
			Set<Integer> parts = eltToPartsMap.get(elt);
			if (parts != null) {
				//sort on begin times
				List<Date> beginTimesList = new ArrayList<Date>();
				Map<Date, Set<Integer>> beginTimeToPartsMap = new HashMap<Date, Set<Integer>>();
				Iterator<Integer> eltparts = parts.iterator();
				//run through data-element blocks of each data-element
				while (eltparts.hasNext()) {
					//get index of current data-element block
					int ind = eltparts.next().intValue();
					//get beginTime of current data element block
					Date beginTime = originalList.get(ind).getBeginTimestamp();
					//check whether beginTime already in list of begin times of the current data-element
					if (!beginTimesList.contains(beginTime)) {
						//if not, add
						beginTimesList.add(beginTime);
					}
					Set<Integer> partSet = new HashSet<Integer>();
					if (beginTimeToPartsMap.containsKey(beginTime)) {
						partSet = beginTimeToPartsMap.get(beginTime);
					}
					partSet.add(ind);
					//place (beginTime, set of indexes of data-element blocks that
					//have this begin time) - pair in map
					beginTimeToPartsMap.put(beginTime, partSet);
				}
				//sort begin times list
				Collections.sort(beginTimesList);
				int similarIndex = 0;
				Iterator<Date> lit0 = beginTimesList.listIterator();
				while (lit0.hasNext()) {
					Date beginTime = lit0.next();
					//obtain set of indexes of data-element blocks (of the current data-elt)
					//that have this beginTime
					Set<Integer> prts = beginTimeToPartsMap.get(beginTime);
					if (prts != null) {
						Iterator<Integer> blocks = prts.iterator();
						//initialize to sort on endtime
						//run through data-element blocks of current data-element,
						// with current begin time
						while (blocks.hasNext()) {
							//obtain index of the current block
							int ind = blocks.next().intValue();
							PSDSequenceBlock block = originalList.get(ind);
							//place data-element block in sortedDataEltBlocks in its sorted order
							//(sorted on data-element name, begin time, end time in that order)
							sortedDataEltBlocks.set(index++, block);
							block.setSimilarIndex(similarIndex++);
						}
					}
				}
			}
		}
	}

	/**
	 * This methods sorts the sortedsortedDataEltBlocks ordered ascendingly on
	 * (begin time, end time, data-element)
	 * 
	 * @param blockSet
	 *            HashSet
	 */
	private void sortBlocksStrictBegin(Set<PSDSequenceBlock> blockSet) {
		sortedDataEltBlocks = new ArrayList<PSDSequenceBlock>();
		List<Date> beginTimesList = new ArrayList<Date>();
		Map<Date, Set<Integer>> timeToPartsMap = new HashMap<Date, Set<Integer>>();
		Iterator<PSDSequenceBlock> it = blockSet.iterator();
		while (it.hasNext()) {
			PSDSequenceBlock bl = it.next();
			sortedDataEltBlocks.add(bl);
			if (!beginTimesList.contains(bl.getBeginTimestamp())) {
				beginTimesList.add(bl.getBeginTimestamp());
			}
			Set<Integer> partSet0 = new HashSet<Integer>();
			if (timeToPartsMap.containsKey(bl.getBeginTimestamp())) {
				partSet0 = timeToPartsMap.get(bl.getBeginTimestamp());
			}
			partSet0.add(sortedDataEltBlocks.size() - 1);
			timeToPartsMap.put(bl.getBeginTimestamp(), partSet0);
		}
		//get original sortedDataEltBlocks
		List<PSDSequenceBlock> originalList = (List<PSDSequenceBlock>) ((ArrayList) sortedDataEltBlocks).clone();
		int index = 0;
		//sort begin times list
		Collections.sort(beginTimesList);
		Iterator<Date> lit = beginTimesList.listIterator();
		//run through begin times
		while (lit.hasNext()) {
			Date beginTime = lit.next();
			Set<Integer> parts = timeToPartsMap.get(beginTime);
			if (parts != null) {
				Iterator<Integer> eltparts = parts.iterator();
				//sort on end times
				List<Date> endTimesList = new ArrayList<Date>();
				Map<Date, Set<Integer>> endTimeToPartsMap = new HashMap<Date, Set<Integer>>();
				while (eltparts.hasNext()) {
					int ind = eltparts.next().intValue();
					Date endTime = originalList.get(ind).getEndTimestamp();
					if (!endTimesList.contains(endTime)) {
						endTimesList.add(endTime);
					}
					Set<Integer> partSet = new HashSet<Integer>();
					if (endTimeToPartsMap.containsKey(endTime)) {
						partSet = endTimeToPartsMap.get(endTime);
					}
					partSet.add(ind);
					endTimeToPartsMap.put(endTime, partSet);
				}
				//sort end times list
				Collections.sort(endTimesList);
				Iterator<Date> lit0 = endTimesList.listIterator();
				while (lit0.hasNext()) {
					Date endTime = lit0.next();
					Set<Integer> prts = endTimeToPartsMap.get(endTime);
					if (prts != null) {
						Iterator<Integer> elts = prts.iterator();
						//sort on data-elts
						List<String> eltList = new ArrayList<String>();
						Map<String, Integer> eltToPartMap = new HashMap<String, Integer>();
						while (elts.hasNext()) {
							int ind = elts.next().intValue();
							String elt = originalList.get(ind).getDataElement();
							if (!eltList.contains(elt)) {
								eltList.add(elt);
							}
							eltToPartMap.put(elt, ind);
						}
						//sort eltList
						Collections.sort(eltList);
						Iterator<String> lit1 = eltList.listIterator();
						while (lit1.hasNext()) {
							int ind = eltToPartMap.get(lit1.next()).intValue();
							PSDSequenceBlock block = originalList.get(ind);
							block.setSimilarIndex(index);
							sortedDataEltBlocks.set(index++, block);

						}
					}
				}
			}
		}
	}

	/**
	 * This methods sorts the sortedsortedDataEltBlocks ordered ascendingly on
	 * (end time, begin time, data-element)
	 * 
	 * @param blockSet
	 *            HashSet
	 */
	private void sortBlocksStrictEnd(Set<PSDSequenceBlock> blockSet) {
		sortedOnEndDataEltBlocks = new ArrayList<PSDSequenceBlock>();
		sortedDataEltBlocks = new ArrayList<PSDSequenceBlock>();
		List<Date> endTimesList = new ArrayList<Date>();
		Map<Date, Set<Integer>> timeToPartsMap = new HashMap<Date, Set<Integer>>();
		Iterator<PSDSequenceBlock> it = blockSet.iterator();
		while (it.hasNext()) {
			PSDSequenceBlock bl = it.next();
			sortedDataEltBlocks.add(bl);
			if (!endTimesList.contains(bl.getEndTimestamp())) {
				endTimesList.add(bl.getEndTimestamp());
			}
			Set<Integer> partSet0 = new HashSet<Integer>();
			if (timeToPartsMap.containsKey(bl.getEndTimestamp())) {
				partSet0 = timeToPartsMap.get(bl.getEndTimestamp());
			}
			partSet0.add(sortedDataEltBlocks.size() - 1);
			timeToPartsMap.put(bl.getEndTimestamp(), partSet0);
		}
		//get original sortedDataEltBlocks
		List<PSDSequenceBlock> originalList = (List<PSDSequenceBlock>) ((ArrayList) sortedDataEltBlocks).clone();
		int index = 0;
		//sort end times list
		Collections.sort(endTimesList);
		Iterator<Date> lit = endTimesList.listIterator();
		//run through end times
		while (lit.hasNext()) {
			Date endTime = lit.next();
			Set<Integer> parts = timeToPartsMap.get(endTime);
			if (parts != null) {
				Iterator<Integer> eltparts = parts.iterator();
				//sort on begin times
				List<Date> beginTimesList = new ArrayList<Date>();
				Map<Date, Set<Integer>> beginTimeToPartsMap = new HashMap<Date, Set<Integer>>();
				while (eltparts.hasNext()) {
					int ind = eltparts.next().intValue();
					Date beginTime = originalList.get(ind).getBeginTimestamp();
					if (!beginTimesList.contains(beginTime)) {
						beginTimesList.add(beginTime);
					}
					Set<Integer> partSet = new HashSet<Integer>();
					if (beginTimeToPartsMap.containsKey(beginTime)) {
						partSet = beginTimeToPartsMap.get(beginTime);
					}
					partSet.add(ind);
					beginTimeToPartsMap.put(beginTime, partSet);
				}
				//sort end times list
				Collections.sort(beginTimesList);
				Iterator<Date> lit0 = beginTimesList.listIterator();
				while (lit0.hasNext()) {
					Date beginTime = lit0.next();
					Set<Integer> prts = beginTimeToPartsMap.get(beginTime);
					if (prts != null) {
						Iterator<Integer> elts = prts.iterator();
						//sort on data-elts
						List<String> eltList = new ArrayList<String>();
						Map<String, Integer> eltToPartMap = new HashMap<String, Integer>();
						while (elts.hasNext()) {
							int ind = elts.next().intValue();
							String elt = originalList.get(ind).getDataElement();
							if (!eltList.contains(elt)) {
								eltList.add(elt);
							}
							eltToPartMap.put(elt, ind);
						}
						//sort eltList
						Collections.sort(eltList);
						Iterator<String> lit1 = eltList.listIterator();
						while (lit1.hasNext()) {
							int ind = eltToPartMap.get(lit1.next()).intValue();
							sortedOnEndDataEltBlocks.add(index++, originalList.get(ind));
						}
					}
				}
			}
		}
	}

	/////////////////////GET AND SET METHODS/////////////////
	/**
	 * Sets color of the sequence
	 * 
	 * @param thisColor
	 *            Color
	 */
	public void setColor(Color thisColor) {
		this.thisColor = thisColor;
	}

	/**
	 * Returns the color of the sequence
	 * 
	 * @return Color
	 */
	public Color getColor() {
		return thisColor;
	}

	/**
	 * Returns the list of arrows of this sequence
	 * 
	 * @return ArrayList
	 */
	public List<PSDSequenceArrow> getArrowList() {
		return arrowList;
	}

	/**
	 * Returns the sorted list of data-element blocks
	 * 
	 * @return ArrayList
	 */
	public List<PSDSequenceBlock> getSortedDataEltBlocks() {
		return sortedDataEltBlocks;
	}

	/**
	 * Returns the list of data-element blocks, sorted on end time, begin time
	 * and finally on data-element name.
	 * 
	 * @return ArrayList
	 */
	public List<PSDSequenceBlock> getSortedOnEndDataEltBlocks() {
		return sortedOnEndDataEltBlocks;
	}

	/**
	 * Returns the time spend in data-element block number num of the sequence
	 * 
	 * @param num
	 *            int
	 * @return long
	 */
	public long getTimePart(int num) {
		try {
			PSDSequenceBlock block = sortedDataEltBlocks.get(num);
			return block.getTimeIn();
		} catch (NullPointerException npe) {
			return 0;
		}
	}

	/**
	 * Returns the begin position of data-element block number 'num' of the
	 * sequence
	 * 
	 * @param num
	 *            int
	 * @return double
	 */
	public double getBeginPositionBlock(int num) {
		try {
			Date bDate = sortedDataEltBlocks.get(num).getBeginTimestamp();
			return (bDate.getTime() - beginDate.getTime());
		} catch (NullPointerException npe) {
			return 0;
		}
	}

	/**
	 * Returns the beginposition of arrow number 'number'
	 * 
	 * @param number
	 *            int
	 * @return double
	 */
	public double[] getArrowPosition(int number) {
		double[] returnArray = new double[2];
		returnArray[0] = -1;
		returnArray[1] = -1;
		if (number < arrowList.size()) {
			PSDSequenceArrow arrow = arrowList.get(number);
			Date firstTimestamp = null, secondTimestamp = null;
			try {
				firstTimestamp = arrow.getBeginTimestamp();
				secondTimestamp = arrow.getEndTimestamp();
			} catch (NullPointerException npe) {
				return returnArray;
			}
			if (beginDate != null) {
				if (firstTimestamp != null) {
					returnArray[0] = (firstTimestamp.getTime() - beginDate.getTime());
				}
				if (secondTimestamp != null) {
					returnArray[1] = (secondTimestamp.getTime() - beginDate.getTime());
				}
			}
		}
		return returnArray;
	}

	/**
	 * Returns the begin date of the sequence
	 * 
	 * @return Date
	 */
	public Date getBeginDate() {
		return beginDate;
	}

	/**
	 * Returns the end date of the sequence
	 * 
	 * @return Date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Returns the name of the process instance to which this sequence
	 * corresponds
	 * 
	 * @return String
	 */
	public String getPiName() {
		return piName;
	}

	/**
	 * Returns the throughput time of the sequence
	 * 
	 * @return double
	 */
	public double getThroughputTime() {
		if (beginDate != null && endDate != null) {
			return (endDate.getTime() - beginDate.getTime());
		} else {
			return 0;
		}
	}

	/**
	 * Returns the starting position of the sequence
	 * 
	 * @return double
	 */
	public double getStartY() {
		return startY;
	}

	/**
	 * returns the end position of the sequence
	 * 
	 * @return double
	 */
	public double getEndY() {
		return endY;
	}

	/**
	 * Sets the start position of the sequence
	 * 
	 * @param startY
	 *            double
	 */
	public void setStartY(double startY) {
		this.startY = startY;
	}

	////////////////////////DRAW-RELATED METHODS/////////////////////////
	/**
	 * Initializes the data-element blocks of the sequence so they can easily be
	 * drawn
	 * 
	 * @param timePerPixel
	 *            double : time per pixel
	 * @param firstDate
	 *            Date : the starting date of the first sequence in the sequence
	 *            diagram
	 */
	public void initializeDrawSequence(double timePerPixel, Date firstDate) {
		Iterator<PSDSequenceBlock> lit = sortedDataEltBlocks.listIterator();
		startY = 0;
		//initialize data element blocks
		while (lit.hasNext()) {
			PSDSequenceBlock block = lit.next();
			Date beginTimestamp = block.getBeginTimestamp();
			Date endTimestamp = block.getEndTimestamp();
			//check if all required values for drawing exist
			if (firstDate != null && beginTimestamp != null && endTimestamp != null) {
				//determine the start position at which the block is to be drawn
				double startAt = 60 + (beginTimestamp.getTime() - firstDate.getTime()) / timePerPixel;
				//determine the length of the block
				double length = (endTimestamp.getTime() - beginTimestamp.getTime()) / timePerPixel;
				//set positions
				block.setStartAt(startAt);
				block.setEndAt(startAt + length);
				if (startAt + length > endY) {
					endY = startAt + length;
				}
				//make sure startY contains the Y-coordinate at which the sequence starts
				if (startY == 0 || startY > startAt) {
					startY = startAt;
				}
			}
		}
		//initialize arrows
		for (int i = 0; i < arrowList.size(); i++) {
			PSDSequenceArrow arrow = arrowList.get(i);
			Date beginTimestamp = arrow.getBeginTimestamp();
			Date endTimestamp = arrow.getEndTimestamp();
			if (firstDate != null && beginTimestamp != null && endTimestamp != null) {
				//only draw rectangles if all required values exist
				double startAt = 60 + (beginTimestamp.getTime() - firstDate.getTime()) / timePerPixel;
				arrow.setStartAt(startAt);
				double endAt = 60 + (endTimestamp.getTime() - firstDate.getTime()) / timePerPixel;
				arrow.setEndAt(endAt);
				//make sure startY contains the Y-coordinate at which the sequence starts
				if (startY == 0 || startY > startAt) {
					startY = startAt;
				}
			}
		}
	}

	/**
	 * @param lifeLines
	 *            HashMap
	 * @param g
	 *            Graphics2D
	 * 
	 */
	public void drawSequence(Map<String, PSDLifeLine> lifeLines, Graphics2D g) {
		g.setColor(thisColor);
		ListIterator<PSDSequenceBlock> lit = sortedDataEltBlocks.listIterator();
		//draw data element blocks of sequence
		while (lit.hasNext()) {
			PSDBlock block = lit.next();
			try {
				block.drawBlock(lifeLines.get(block.getDataElement()).getMiddle() - 10, thisColor, g);
			} catch (NullPointerException ex) {
				//should not occur
			}
		}
		//draw arrows of sequence
		for (int i = 0; i < arrowList.size(); i++) {
			PSDSequenceArrow arrow = arrowList.get(i);
			arrow.drawArrow(lifeLines, thisColor, g);
		}
	}

	/**
	 * Draws a rectangle of width 20, height length and starting point
	 * (startX,startY) in the northwest corner of the rectangle. In case
	 * logicSteps is true, the height is 10.
	 * 
	 * @param startX
	 *            double
	 * @param startY
	 *            double
	 * @param length
	 *            double
	 * @param logicSteps
	 *            boolean
	 * @param g
	 *            Graphics2D
	 */
	public void drawRectangle(double startX, double startY, double length, boolean logicSteps, Graphics2D g) {
		Rectangle2D r = new Rectangle2D.Double(startX, startY, 20, length);
		if (logicSteps) {
			r = new Rectangle2D.Double(startX, startY, 20, 10);
		}
		Paint initialPaint = g.getPaint();
		GradientPaint towhite = new GradientPaint(((Double) startX).floatValue(), ((Double) startY).floatValue(),
				thisColor, ((Double) startX).floatValue() + 20, ((Double) startY).floatValue(), Color.WHITE);
		g.setPaint(towhite);
		g.fill(r);
		g.setPaint(initialPaint);
		g.setColor(Color.BLACK);
		g.draw(r);
		g.setColor(thisColor);
	}

}
