package org.processmining.plugins.fuzzymodel.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMEdge;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.BestEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FastTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FuzzyEdgeTransformer;
import org.processmining.plugins.fuzzymodel.miner.replay.FuzzyDetailAnalysis;

public class FastFuzzyMinerAdapted {

	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String EDGE_TRANSFORMER_SELECTION = "EdgeTransformerSelection";
	private static final String EDGE_TRANSFORMER_SELECTION_BEST_EDGES = "EdgeTransformerSelectionBestEdges";
	private static final String EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES = "EdgeTransformerSelectionFuzzyEdges";
	private static final String CONCURRENCY_EDGE_TRANSFORMER_ACTIVE = "ConcurrencyEdgeTransformerActive";
	private static final String NODE_CUTOFF = "NodeCutoff";
	private static final String FUZZY_EDGE_RATIO = "FuzzyEdgeRatio";
	private static final String FUZZY_EDGE_CUTOFF = "FuzzyEdgeCutoff";
	private static final String CONCURRENCY_THRESHOLD = "ConcurrencyThreshold";
	private static final String CONCURRENCY_RATIO = "ConcurrencyRatio";
	private static final String EDGES_FUZZY_IGNORE_LOOPS = "EdgesFuzzyIgnoreLoops";
	private static final String EDGES_FUZZY_INTERPRET_ABSOLUTE = "EdgesFuzzyInterpretAbsolute";

	private Double ratio = 0.25;
	private Double preserve = 0.7;
	private Double cutoff = 0.6;
	private Double utility = 0.7;
	private Double nodeSig = 0.0;

	private XLog log = null;
	private MutableFuzzyGraph graph;
	private FastTransformer fastTransformer;
	private BestEdgeTransformer bestEdgeTransformer;
	private FuzzyEdgeTransformer fuzzyEdgeTransformer;
	private NewConcurrencyEdgeTransformer concurrencyEdgeTransformer;

	private FuzzyOptimalResult fuzzyOptimalResult;
	private ArrayList<FuzzyOptimalResult> fuzzyOptimalResults = new ArrayList<FuzzyOptimalResult>();
	private ArrayList<FuzzyOptimalResult> fuzzyOptimalResultsTemp = new ArrayList<FuzzyOptimalResult>();
	private ArrayList<FuzzyOptimalResult> fuzzyOptimalResultsAll = new ArrayList<FuzzyOptimalResult>();
	private ArrayList<FuzzyOptimalResult> randomFuzzyOptimalResults = new ArrayList<FuzzyOptimalResult>();

	private Conformance replay;
	private FuzzyDetailAnalysis detail;

	private Double conformance;
	private UnaryMetric nodeSignificance;
	private double originalEdgesDetail;
	private int tEdges;
	private NodesigListResult nodeSigOptimal;
	private ArrayList<NodesigListResult> nodeSigOptimals = new ArrayList<NodesigListResult>();
	private ArrayList<NodesigListResult> nodeSigOptimalsList = new ArrayList<NodesigListResult>();
	private CutoffResult cutoffResult;
	private ArrayList<CutoffResult> cutoffResults = new ArrayList<CutoffResult>();
	private ArrayList<CutoffResult> cutoffResultsList = new ArrayList<CutoffResult>();
	private double sN;
	private double[] preserveAll = null;
	private double[] ratioAll = null;
	private double opC;

	private double logComplexity;
	private int count;
	private double avgOfPreserve;
	private double avgOfRatio;
	private double mutaStepOfPreserve;
	private double mNodeSig;
	private int indexT;
	private int tS;
	private double[] nodeSigAll = null;
	private int visit;
	private int qualityPR;
	private int qualityN;
	private int qualityC;
	private int qualityU;
	private double timeCom;
	private int comCount;
	private double opCum;
	private int totalCom;
	private int cluster;
	private int type;
	private int countU;

	/*
	 * public FastFuzzyMinerjiaojiao3(PluginContext context, MetricsRepository
	 * metrics,Double conformance) { this(context, new
	 * MutableFuzzyGraph(metrics),conformance); }
	 * 
	 * public FastFuzzyMinerjiaojiao3(PluginContext context, MutableFuzzyGraph
	 * graph,Double conformance) { this(context, graph, null,conformance,null);
	 * }
	 */
	public FastFuzzyMinerAdapted(PluginContext context, MutableFuzzyGraph graph, XLog log, Double conformance,
			MetricsRepository metrics, double logcomplexity, int count, double avgOfPreserve, double avgOfRatio,
			double timeCom, int Tedges, double OriginalEdgesDetail, double[] preserveall, double[] ratioall, int visit,
			XLogInfo logsummary, double[] nodesigall) {

		this.log = log;
		this.graph = graph;
		//this.setBackground(new Color(240, 240, 240));
		this.conformance = conformance;
		this.nodeSignificance = metrics.getAggregateUnaryLogMetric();
		this.count = count;

		if (count > 0) {
			this.avgOfPreserve = avgOfPreserve;
			this.avgOfRatio = avgOfRatio;
		} else {
			//if there is no conflicting relations in the log, avgOfRatio and avgOfPreserve are empty.
			this.avgOfPreserve = 0.5;
			this.avgOfRatio = 0.5;
		}
		this.tEdges = Tedges;
		this.originalEdgesDetail = OriginalEdgesDetail;
		this.preserveAll = preserveall;
		this.ratioAll = ratioall;
		this.visit = visit;
		this.timeCom = timeCom;
		this.comCount = 0;
		this.opCum = 0.0;
		this.totalCom = 0;
		this.nodeSigAll = nodesigall;
		this.countU = 0;
		fastTransformer = new FastTransformer(context);
		bestEdgeTransformer = new BestEdgeTransformer(context);
		fuzzyEdgeTransformer = new FuzzyEdgeTransformer(context);
		concurrencyEdgeTransformer = new NewConcurrencyEdgeTransformer(context);

		//---determine the log complexity	
		this.logComplexity = logcomplexity;

		if (visit > 1) {
			ArrayList<FuzzyOptimalResult> fuzzyoptimalresultstemp2 = new ArrayList<FuzzyOptimalResult>();
			for (int i = 0; i < fuzzyOptimalResultsAll.size(); i++) {
				if (fuzzyOptimalResultsAll.get(i).conformance > conformance) {
					fuzzyoptimalresultstemp2.add(fuzzyOptimalResultsAll.get(i));
				}
			}
			if (fuzzyoptimalresultstemp2.size() > 0) {
				//keep the best setting as the default one for the searching next step
				FuzzyOptimalResult fuzzyoptimalresult_t = new FuzzyOptimalResult();
				fuzzyoptimalresult_t = getMaxOptimalvalueFuzzyResult(fuzzyoptimalresultstemp2);
				fuzzyOptimalResults.add(fuzzyoptimalresult_t);
				fuzzyOptimalResultsTemp.add(fuzzyoptimalresult_t);
			}
		} else {
			fuzzyOptimalResultsAll.clear();
		}

		setComNumber();
	}

	//Logs are classified into clusters and types which determine the total computation times and the settings for four control parameters
	private void setComNumber() {
		if (Math.abs(timeCom - 361593) < Math.abs(timeCom - 213155)) {
			cluster = 0;
		} else if (Math.abs(timeCom - 213155) < Math.abs(timeCom - 18625)) {
			cluster = 1;
		} else if (Math.abs(timeCom - 18625) < Math.abs(timeCom - 909.3)) {
			cluster = 2;
		} else {
			cluster = 3;
		}
		
		if (cluster == 0) { 
			totalCom = 30;
			qualityN = 2;
			qualityC = 2;
			qualityPR = 2;
			qualityU = 0;
			if (logComplexity > 88000) {
				type = 13;
			} else if (logComplexity > 2680) {
				type = 3;
			} else if (logComplexity > 50) {
				type = 2;
				qualityN = 3;
				qualityC = 5;
				qualityPR = 7;
				qualityU = 1;
			} else {
				type = 1;
				qualityC = 3;
				qualityN = 4;
				qualityPR = 5;
				qualityU = 1;
			}
		} else if (cluster == 1) {
			totalCom = 40;
			if (logComplexity >= 88000) {
				type = 14;
				qualityN = 3;
				qualityC = 4;
				qualityPR = 5;
				qualityU = 1;
			} else if (logComplexity >= 2680) {
				type = 6;
				qualityN = 3;
				qualityC = 4;
				qualityPR = 5;
				qualityU = 1;
			} else if (logComplexity >= 50) {
				type = 5;
				qualityN = 3;
				qualityC = 4;
				qualityPR = 5;
				qualityU = 1;
			} else {
				type = 4;
				qualityC = 3;
				qualityN = 4;
				qualityPR = 5;
				qualityU = 1;
			}
		} else if (cluster == 2) {
			totalCom = 40;
			if (logComplexity >= 88000) {
				type = 15;
				qualityN = 3;
				qualityC = 4;
				qualityPR = 5;
				qualityU = 1;
			} else if (logComplexity >= 2680) {
				type = 9;
				qualityN = 3;
				qualityC = 4;
				qualityPR = 7;
				qualityU = 1;
			} else if (logComplexity >= 50) {
				type = 8;
				qualityN = 3;
				qualityC = 5;
				qualityPR = 7;
				qualityU = 1;
			} else {
				type = 7;
				qualityC = 3;
				qualityN = 5;
				qualityPR = 7;
				qualityU = 1;
			}
		} else if (cluster == 3) {
			totalCom = 195;
			if (logComplexity >= 88000) {
				type = 16;
				qualityN = 4;
				qualityC = 8;
				qualityPR = 10;
				qualityU = 1;
			} else if (logComplexity >= 2680) {
				type = 12;
				qualityN = 4;
				qualityC = 6;
				qualityPR = 8;
				qualityU = 1;
			} else if (logComplexity >= 50) {
				type = 11;
				qualityN = 4;
				qualityC = 8;
				qualityPR = 10;
				qualityU = 1;
			} else {
				type = 10;
				qualityC = 4;
				qualityN = 8;
				qualityPR = 10;
				qualityU = 1;
			}
		}

	}

	public void calculation() {
		graph.initializeGraph();
		sN = graph.getMinimalNodeSignificance();

		tS = nodeSignificance.size();

		if (type == 19) {
			preserve = avgOfPreserve;
			ratio = avgOfRatio;
			cutoff = 0.02;
			utility = 0.8;
			nodeSig = 0.93;
			AdjustNodesigInitial(Math.max(tS - 10, 20 * tS / 21), tS - 2);
		} else {
			if (type == 15 || type == 16 || type == 9 || type == 3 || type == 6 || type == 13 || type == 14) {
				preserve = avgOfPreserve;
				ratio = avgOfRatio;
				cutoff = 0.02;
				utility = 0.8;
				if (type == 15 || type == 16) {
					AdjustNodesigInitial(4 * tS / 5, tS - 1);
				} else if (type == 13 || type == 14) {
					AdjustNodesigInitial(Math.max(tS - 10, 20 * tS / 21), tS - 2);
				} else {
					AdjustNodesigInitial(tS / 4, tS - 1);
				}
				if (fuzzyOptimalResults.size() <= qualityC) {
					getBetterOptimalvalueNodesig();
					for (int i = 0; i < nodeSigOptimalsList.size(); i++) {
						if (fuzzyOptimalResults.size() >= qualityC) {
							break;
						}
						nodeSig = nodeSigOptimalsList.get(i).nodesignificance;
						CutoffAdjustInitial(0.03, 0.1);
					}
				}
				if (fuzzyOptimalResults.size() == 0) {
					cutoff = 0.02;
					if (tS > 10) {
						AdjustNodesigInitial(tS - 10, (tS - 1));
					} else {
						AdjustNodesigInitial(3 * (tS - 1) / 4, (tS - 1));
					}
				}

				//preserve
				FuzzyOptimalResult fuzzyoptimalresult_t = new FuzzyOptimalResult();
				fuzzyoptimalresult_t = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResults);
				ratio = fuzzyoptimalresult_t.ratio;
				preserve = fuzzyoptimalresult_t.preserve;
				cutoff = fuzzyoptimalresult_t.cutoff;
				utility = fuzzyoptimalresult_t.utility;
				nodeSig = fuzzyoptimalresult_t.nodesig;

				AdjustOnlyPreserveInitial(avgOfPreserve, (preserveAll[preserveAll.length - 1] + 1.0) / 2.0);
				//JOptionPane.showMessageDialog(null, "You have to do pre-process of log, i.e., filter less important evennts and focus on the more interesting events!");
				if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
					//ratio
					fuzzyoptimalresult_t = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResults);
					ratio = fuzzyoptimalresult_t.ratio;
					preserve = fuzzyoptimalresult_t.preserve;
					cutoff = fuzzyoptimalresult_t.cutoff;
					utility = fuzzyoptimalresult_t.utility;
					nodeSig = fuzzyoptimalresult_t.nodesig;

					RatioAdjustInitial(ratioAll[ratioAll.length - count] / 2.0,
							ratioAll[ratioAll.length - count] / 2.0 + 0.2, preserve);
				}

				//utility
				FuzzyOptimalResult fuzzyoptimalresult_t1 = new FuzzyOptimalResult();
				fuzzyoptimalresult_t1 = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResultsTemp);
				ratio = fuzzyoptimalresult_t1.ratio;
				preserve = fuzzyoptimalresult_t1.preserve;
				cutoff = fuzzyoptimalresult_t1.cutoff;
				utility = fuzzyoptimalresult_t1.utility;
				nodeSig = fuzzyoptimalresult_t1.nodesig;
				opC = fuzzyoptimalresult_t1.optimalvalue;

				AdjustUtilityInitial(0.7, 0.85);

			} else {
				if (type == 8 || type == 11 || type == 7 || type == 12 || type == 2 || type == 5) {
					//rough adjustment					
					ratio = avgOfRatio;
					if (count == 0) {
						preserve = 0.002;
					} else {
						preserve = Math.max(preserveAll[preserveAll.length - count] / 2.0, 0.002);
					}
					//preserve=preserveall[preserveall.length-count]/2.0;	
					utility = 0.93;
					nodeSig = 0.51;
					cutoff = getInitialRoughCutoff(0.02, 0.6);
					//nodesig=getInitialRoughNodesig(S_N,E_N);
					mNodeSig = nodeSigAll[(tS - 1) / 2];
					double optimalValue_N_1 = getOptimalValue(ratio, preserve, cutoff, utility, sN, 0.0);
					comCount++;
					totalCom--;
					double optimalValue_N_2 = getOptimalValue(ratio, preserve, cutoff, utility, mNodeSig, 0.0);
					comCount++;
					totalCom--;
					double maxOp = Math.max(optimalValue_N_1, optimalValue_N_2);
					if (maxOp == optimalValue_N_1) {
						nodeSig = sN;
						indexT = 0;
					} else if (maxOp == optimalValue_N_2) {
						nodeSig = mNodeSig;
						indexT = (tS - 1) / 2;
					}

					updateGraphAttributesFromUI_jiaojiao(ratio, preserve, cutoff, utility, nodeSig);
					readGraphAttributes_jiaojiao();
					graph.initializeGraph();
					fastTransformer.transform(graph);
					//Set edges for Fuzzy Graph
					graph.setEdgeImpls();
					//---middle complex log
					double cutofftemp = cutoff;
					Conformance conf;
					try {
						conf = new Conformance(graph, log);
						if (getReadability() && conf.getValue() >= conformance) {
							qualityC = 5;
							fuzzyOptimalResult = new FuzzyOptimalResult();
							//fuzzyoptimalresult.setValues(ratio, E_P, cutoff, utility, nodesig, optimalValue_E_P);
							fuzzyOptimalResult.setValues(ratio, preserve, cutoff, utility, nodeSig, conf.getValue(),
									getDetail(), getComplexity(), maxOp, true);
							comCount++;
							totalCom--;
							AddToOptimalList(fuzzyOptimalResult);
							AddToOptimalListTemp(fuzzyOptimalResult);
							CutoffAdjustInitial(0.3, 0.7);
							if (fuzzyOptimalResults.size() < qualityC) {
								getBetterOptimalvalueCutoff();
								qualityN = 8;
								for (int i = 0; i < cutoffResultsList.size(); i++) {
									if (fuzzyOptimalResults.size() < qualityN) {
										cutoff = cutoffResultsList.get(i).cutoff;
										if (tS > 4) {
											AdjustNodesigInitial(1, (tS - 1) / 2);
										} else {
											AdjustNodesigInitial(0, tS - 1);
										}
									}
								}
							}
						} else {
							if (type == 7 || type == 8 || type == 11 || type == 12 || type == 2 || type == 5) {
								cutoff = 0.02;
								AdjustNodesigInitial(indexT, (tS - 1));
								getBetterOptimalvalueNodesig();
								for (int i = 0; i < nodeSigOptimalsList.size(); i++) {
									if (fuzzyOptimalResults.size() >= qualityC && totalCom > 0) {
										break;
									}
									nodeSig = nodeSigOptimalsList.get(i).nodesignificance;
									CutoffAdjustInitial(Math.max(cutofftemp - 0.3, 0.05),
											Math.min(cutofftemp + 0.1, 0.6));
								}
							} else {
								if (!getReadability() && conf.getValue() < conformance) {
									AdjustNodesigInitial(Math.min(indexT + 1, tS - 1), tS - 1);
									getBetterOptimalvalueNodesig();
									if (nodeSigOptimalsList.size() != 0 && totalCom > 0) {
										for (int i = 0; i < nodeSigOptimalsList.size(); i++) {
											nodeSig = nodeSigOptimalsList.get(i).nodesignificance;
											CutoffAdjustInitial(0.02, cutoff - 0.05);
										}
									}
								} else if (!getReadability()) {
									CutoffAdjustInitial(0.02, cutoff - 0.02);
								} else {
									CutoffAdjustInitial(cutoff, cutoff + 0.3);
								}
							}

						}
					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (fuzzyOptimalResults.size() == 0 && totalCom > 0) {
						cutoff = 0.02;
						if (tS > 10) {
							AdjustNodesigInitial(tS - 10, (tS - 1));
						} else {
							AdjustNodesigInitial(3 * (tS - 1) / 4, (tS - 1));
						}
					}
				} else if (type == 10 || type == 1 || type == 4) {
					ratio = avgOfRatio;
					if (count == 0) {
						preserve = 0.002;
					} else {
						preserve = Math.max(preserveAll[preserveAll.length - count] / 2.0, 0.002);
					}
					utility = 0.93;
					nodeSig = sN;
					cutoff = 0.5;

					updateGraphAttributesFromUI_jiaojiao(ratio, preserve, cutoff, utility, nodeSig);
					readGraphAttributes_jiaojiao();
					graph.initializeGraph();
					fastTransformer.transform(graph);
					//Set edges for Fuzzy Graph
					graph.setEdgeImpls();
					//---structural log
					Conformance conf;
					try {
						conf = new Conformance(graph, log);
						if (getReadability() && conf.getValue() >= conformance) {
							//Cquality=5;
							CutoffAdjustInitial(0.3, 0.7);
						} else if (!getReadability() && conf.getValue() < conformance) {
							//Nquality=3;
							//Cquality=5;
							AdjustNodesigInitial(Math.min(indexT + 1, tS - 1), tS - 1);
							getBetterOptimalvalueNodesig();
							if (nodeSigOptimalsList.size() != 0 && totalCom >= 0) {
								for (int i = 0; i < nodeSigOptimalsList.size(); i++) {
									nodeSig = nodeSigOptimalsList.get(i).nodesignificance;
									CutoffAdjustInitial(0.02, cutoff - 0.05);
								}
							}
						} else if (!getReadability()) {
							//Cquality=5;
							CutoffAdjustInitial(0.02, cutoff - 0.02);
						} else {
							CutoffAdjustInitial(cutoff, cutoff + 0.3);
						}
					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					//CutoffAdjustInitial(0.2,0.7);
					if (fuzzyOptimalResults.size() < qualityC && totalCom > 0) {
						getBetterOptimalvalueCutoff();
						qualityN = 8;
						for (int i = 0; i < cutoffResultsList.size(); i++) {
							if (fuzzyOptimalResults.size() < qualityN && totalCom > 0) {
								cutoff = cutoffResultsList.get(i).cutoff;
								if (tS > 4) {
									AdjustNodesigInitial(1, (tS - 1) / 2);
								} else {
									AdjustNodesigInitial(0, tS - 1);
								}
							}
						}
					}
				}
				//preserev&&ratio
				if (count > 0 && fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
					if (fuzzyOptimalResults.size() != 0) {
						FuzzyOptimalResult fuzzyoptimalresult_t = new FuzzyOptimalResult();
						fuzzyoptimalresult_t = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResults);
						ratio = fuzzyoptimalresult_t.ratio;
						preserve = fuzzyoptimalresult_t.preserve;
						cutoff = fuzzyoptimalresult_t.cutoff;
						utility = fuzzyoptimalresult_t.utility;
						nodeSig = fuzzyoptimalresult_t.nodesig;
						opC = fuzzyoptimalresult_t.optimalvalue;

						//---preserve && ratio 
						double defaultOptimal = opC;
						double tempOptimal_1 = getOptimalValue((1.0 + ratioAll[ratioAll.length - 1]) / 2.0,
								(1.0 + preserveAll[preserveAll.length - 1]) / 2.0, cutoff, utility, nodeSig, 0.0);
						double tempOptimal_2 = getOptimalValue((ratioAll[ratioAll.length - count]) / 2.0,
								avgOfPreserve, cutoff, utility, nodeSig, 0.0);
						double tempOptimal_3 = getOptimalValue((1.0 + ratioAll[ratioAll.length - 1]) / 2.0,
								avgOfPreserve, cutoff, utility, nodeSig, 0.0);
						double tempOptimal_4 = getOptimalValue(avgOfRatio,
								(1.0 + preserveAll[preserveAll.length - 1]) / 2.0, cutoff, utility, nodeSig, 0.0);
						double tempOptimal_5 = getOptimalValue(avgOfRatio, avgOfPreserve, cutoff, utility, nodeSig, 0.0);
						double tempOptimal_6 = getOptimalValue(ratioAll[ratioAll.length - count] / 2.0,
								(1.0 + preserveAll[preserveAll.length - 1]) / 2.0, cutoff, utility, nodeSig, 0.0);
						double sign = -1;
						comCount = comCount + 6;
						totalCom = totalCom - 6;
						if (defaultOptimal <= tempOptimal_1) {
							defaultOptimal = tempOptimal_1;
							ratio = (1.0 + ratioAll[ratioAll.length - 1]) / 2.0;
							preserve = (1.0 + preserveAll[preserveAll.length - 1]) / 2.0;
							sign = 1;
						}
						if (defaultOptimal <= tempOptimal_2) {
							defaultOptimal = tempOptimal_2;
							ratio = (ratioAll[ratioAll.length - count]) / 2.0;
							preserve = avgOfPreserve;
							sign = 2;
						}
						if (defaultOptimal <= tempOptimal_3) {
							defaultOptimal = tempOptimal_3;
							ratio = (1.0 + ratioAll[ratioAll.length - 1]) / 2.0;
							preserve = avgOfPreserve;
							sign = 3;
						}
						if (defaultOptimal <= tempOptimal_4) {
							defaultOptimal = tempOptimal_4;
							ratio = avgOfRatio;
							preserve = (1.0 + preserveAll[preserveAll.length - 1]) / 2.0;
							sign = 4;
						}
						if (defaultOptimal <= tempOptimal_5) {
							defaultOptimal = tempOptimal_5;
							ratio = avgOfRatio;
							preserve = avgOfPreserve;
							sign = 5;
						}
						if (defaultOptimal <= tempOptimal_6) {
							defaultOptimal = tempOptimal_6;
							ratio = avgOfRatio;
							preserve = (1.0 + preserveAll[preserveAll.length - 1]) / 2.0;
							sign = 6;
						}
						PreserveAdjustInitial(Math.max(preserve - 0.05, 0.003),
								Math.min(preserve + 0.15, (1.0 + preserveAll[preserveAll.length - 1]) / 2.0), sign);
					}

				}
				//utility adjustment
				FuzzyOptimalResult fuzzyoptimalresult_t1 = new FuzzyOptimalResult();
				fuzzyoptimalresult_t1 = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResultsTemp);
				ratio = fuzzyoptimalresult_t1.ratio;
				preserve = fuzzyoptimalresult_t1.preserve;
				cutoff = fuzzyoptimalresult_t1.cutoff;
				utility = fuzzyoptimalresult_t1.utility;
				nodeSig = fuzzyoptimalresult_t1.nodesig;
				opC = fuzzyoptimalresult_t1.optimalvalue;

				AdjustUtilityInitial(0.7, 0.85);
			}

			// once none best settings found, we have to use the default setting
			if (fuzzyOptimalResults.size() == 0) {
				FuzzyOptimalResult fuzzyoptimalresult_t = new FuzzyOptimalResult();
				if (fuzzyOptimalResultsAll.size() != 0) {
					fuzzyoptimalresult_t = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResultsAll);
					fuzzyOptimalResults.add(fuzzyoptimalresult_t);
				}
				FuzzyOptimalResult frz = new FuzzyOptimalResult();
				frz.preserve = avgOfPreserve;
				frz.ratio = avgOfRatio;
				frz.cutoff = 0.02;
				frz.utility = 0.8;
				frz.nodesig = 0.75;
				double conf1 = getConformanceValue(frz.ratio, frz.preserve, frz.cutoff, frz.utility, frz.nodesig, log);
				double optemp1 = getOptimalValue(frz.ratio, frz.preserve, frz.cutoff, frz.utility, frz.nodesig, conf1);
				comCount++;
				frz.setValues(frz.ratio, frz.preserve, frz.cutoff, frz.utility, frz.nodesig, conf1, getDetail(),
						getComplexity(), optemp1, getReadability());
				fuzzyOptimalResults.add(frz);
				fuzzyOptimalResultsTemp.add(frz);
				fuzzyOptimalResultsAll.add(frz);
			}

		}

	}

	//-----------
	protected void AdjustUtilityInitial(double S_U, double E_U) {
		double optimalValue_S_U = 0.0;
		double optimalValue_E_U = 0.0;
		if (totalCom > 0) {
			double conf_S_U = getConformanceValue(ratio, preserve, cutoff, S_U, nodeSig, log);
			optimalValue_S_U = getOptimalValue(ratio, preserve, cutoff, S_U, nodeSig, conf_S_U);
			comCount++;
			totalCom--;
			if (conf_S_U > conformance && getReadability() && countU < qualityU) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(S_R, preservetemp, cutoff, utility, nodesig, optimalValue_S_R);
				fuzzyOptimalResult.setValues(ratio, preserve, cutoff, S_U, nodeSig, conf_S_U, getDetail(),
						getComplexity(), optimalValue_S_U, getReadability());
				if (optimalValue_S_U >= opC || Math.abs(optimalValue_S_U - opC) < 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					countU++;
				}
				AddToOptimalListTemp(fuzzyOptimalResult);

			}
		}
		if (totalCom > 0) {
			double conf_E_U = getConformanceValue(ratio, preserve, cutoff, E_U, nodeSig, log);
			optimalValue_E_U = getOptimalValue(ratio, preserve, cutoff, E_U, nodeSig, conf_E_U);
			comCount++;
			totalCom--;
			if (conf_E_U > conformance && getReadability() && countU < qualityU) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(E_R, preservetemp, cutoff, utility, nodesig, optimalValue_E_R);
				fuzzyOptimalResult.setValues(ratio, preserve, cutoff, E_U, nodeSig, conf_E_U, getDetail(),
						getComplexity(), optimalValue_E_U, getReadability());
				if (optimalValue_E_U >= opC || Math.abs(optimalValue_E_U - opC) < 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					countU++;
				}
				AddToOptimalListTemp(fuzzyOptimalResult);
			}
		}

		if (totalCom > 0 && countU < qualityU) {
			AdjustUtility(S_U, E_U, optimalValue_S_U, optimalValue_E_U);
		}

	}

	public void AdjustUtility(double S_U, double E_U, double optimalValue_S_U, double optimalValue_E_U) {
		double utilitytemp = (S_U + E_U) / 2.0;
		if (Math.abs(E_U - utilitytemp) >= 0.02 && totalCom > 0 && countU < qualityU) {
			double conf = getConformanceValue(ratio, preserve, cutoff, utilitytemp, nodeSig, log);
			double optimalValue = getOptimalValue(ratio, preserve, cutoff, utilitytemp, nodeSig, conf);
			comCount++;
			totalCom--;
			if (conf > conformance && getReadability()) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				fuzzyOptimalResult.setValues(ratio, preserve, cutoff, utilitytemp, nodeSig, conf, getDetail(),
						getComplexity(), optimalValue, getReadability());
				if (optimalValue >= opC || Math.abs(optimalValue - opC) < 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					countU++;
				}
				AddToOptimalListTemp(fuzzyOptimalResult);
			}
			AdjustUtility(utilitytemp, E_U, optimalValue, optimalValue_E_U);
			AdjustUtility(S_U, utilitytemp, optimalValue_S_U, optimalValue);
		}
	}

	public ArrayList<FuzzyOptimalResult> getfuzzyoptimalresults() {
		return fuzzyOptimalResultsTemp;
	}

	public void randomGeneration() {
		for (int i = 0; i < comCount; i++) {
			Random generator = new Random();
			FuzzyOptimalResult frz1 = new FuzzyOptimalResult();
			ratio = generator.nextDouble();
			preserve = generator.nextDouble();
			cutoff = generator.nextDouble();
			utility = generator.nextDouble();
			nodeSig = generator.nextDouble();
			/*
			 * updateGraphAttributesFromUI_jiaojiao(ratio,preserve,cutoff,utility
			 * ,nodesig); readGraphAttributes_jiaojiao();
			 * graph.initializeGraph(); fastTransformer.transform(graph); //Set
			 * edges for Fuzzy Graph graph.setEdgeImpls(); graph.signalViews();
			 * double nodesN=graph.getNodes().size(); double
			 * abstracL=nodesN/graph.getNumberOfInitialNodes();
			 * 
			 * Calendar calstart = Calendar.getInstance(); calstart =
			 * Calendar.getInstance(); double startTime, endTime;
			 * startTime=calstart.getTimeInMillis(); Calendar calend =
			 * Calendar.getInstance(); calend = Calendar.getInstance(); endTime
			 * =calend.getTimeInMillis(); double msec=endTime-startTime;
			 * timeMins = (msec/(1000.0)); java.text.DecimalFormat df =new
			 * java.text.DecimalFormat("#.00000"); String
			 * avetime=df.format(timeMins); JOptionPane.showMessageDialog(null,
			 * "The candidate searching process takes: 0"+avetime+" seconds");
			 */
			double conf = getConformanceValue(ratio, preserve, cutoff, utility, nodeSig, log);
			double optemp = getOptimalValue(ratio, preserve, cutoff, utility, nodeSig, conf);
			opCum = opCum + optemp;
			if (conf >= conformance && getReadability()) {
				frz1.setValues(ratio, preserve, cutoff, utility, nodeSig, conf, getDetail(), getComplexity(), optemp,
						getReadability());
				randomFuzzyOptimalResults.add(frz1);
				fuzzyOptimalResultsTemp.add(frz1);
			}

		}
	}

	protected void AdjustOnlyPreserveInitial(double S_P, double E_P) {
		double optimalValue_S_n = 0.0;
		double optimalValue_E_n = 0.0;
		if (totalCom > 0) {
			double conf_S_n = getConformanceValue(ratio, S_P, cutoff, utility, nodeSig, log);
			optimalValue_S_n = getOptimalValue(ratio, S_P, cutoff, utility, nodeSig, conf_S_n);
			comCount++;
			totalCom--;
			if (conf_S_n > conformance && getReadability() && fuzzyOptimalResults.size() < qualityPR) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(ratio, preserve, cutoff, utility, nodesig, optimalValue_S_n);
				fuzzyOptimalResult.setValues(ratio, S_P, cutoff, utility, nodeSig, conf_S_n, getDetail(),
						getComplexity(), optimalValue_S_n, getReadability());
				if (optimalValue_S_n >= opC) {
					fuzzyOptimalResultsTemp.add(fuzzyOptimalResult);
				}
				AddToOptimalList(fuzzyOptimalResult);
			}
		}
		if (totalCom > 0) {
			double conf_E_n = getConformanceValue(ratio, E_P, cutoff, utility, nodeSig, log);
			optimalValue_E_n = getOptimalValue(ratio, E_P, cutoff, utility, nodeSig, conf_E_n);
			comCount++;
			totalCom--;
			if (conf_E_n > conformance && getReadability() && fuzzyOptimalResults.size() < qualityPR) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//.setValues(ratio, preserve, cutoff, utility, nodesig, optimalValue_E_n);
				fuzzyOptimalResult.setValues(ratio, E_P, cutoff, utility, nodeSig, conf_E_n, getDetail(),
						getComplexity(), optimalValue_E_n, getReadability());
				if (optimalValue_E_n >= opC) {
					fuzzyOptimalResultsTemp.add(fuzzyOptimalResult);
				}
				AddToOptimalList(fuzzyOptimalResult);
			}
		}
		//AdjustNodesigComplex(0.0,1.0,optimalValue_S_n,optimalValue_E_n);
		if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
			AdjustOnlyPreserve(S_P, E_P, optimalValue_S_n, optimalValue_E_n);
		}
	}

	protected void AdjustOnlyPreserve(double S_P, double E_P, double optimalValue_S_P, double optimalValue_E_P) {
		double preservetemp = (S_P + E_P) / 2.0;
		if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
			if (Math.abs(E_P - preservetemp) >= 0.02) {
				double conf = getConformanceValue(ratio, preservetemp, cutoff, utility, nodeSig, log);
				double optimalValue = getOptimalValue(ratio, preservetemp, cutoff, utility, nodeSig, conf);
				comCount++;
				totalCom--;
				//if(Math.max(Math.abs(optimalValue-optimalValue_S_P), Math.abs(optimalValue-optimalValue_E_P))>0.0000001){
				if (optimalValue > opC && conf > conformance && getReadability()) {
					fuzzyOptimalResult = new FuzzyOptimalResult();
					//fuzzyoptimalresult.setValues(ratio, preservetemp, cutoff, utility, nodesig, optimalValue);
					fuzzyOptimalResult.setValues(ratio, preservetemp, cutoff, utility, nodeSig, conf, getDetail(),
							getComplexity(), optimalValue, getReadability());
					if (optimalValue >= opC) {
						fuzzyOptimalResults.add(fuzzyOptimalResult);
						//AddToOptimalList(fuzzyoptimalresult);
					}
					AddToOptimalListTemp(fuzzyOptimalResult);
				}
				if (Math.abs(optimalValue - optimalValue_E_P) < 0.0000001) {
					AdjustOnlyPreserve(S_P, preservetemp, optimalValue_S_P, optimalValue);
				} else if (Math.abs(optimalValue - optimalValue_S_P) < 0.0000001) {
					AdjustOnlyPreserve(preservetemp, E_P, optimalValue, optimalValue_E_P);
				} else {
					//if (optimalValue>optimalValue_S_P && optimalValue<optimalValue_E_P){
					//AdjustOnlyPreserve(preservetemp,E_P,optimalValue,optimalValue_E_P);
					//}
					//else if(optimalValue<optimalValue_S_P && optimalValue>optimalValue_E_P){
					//AdjustOnlyPreserve(S_P,preservetemp,optimalValue_S_P,optimalValue);
					//}
					//else if(optimalValue>optimalValue_S_P && optimalValue>optimalValue_E_P){
					AdjustOnlyPreserve(S_P, preservetemp, optimalValue_S_P, optimalValue);
					AdjustOnlyPreserve(preservetemp, E_P, optimalValue, optimalValue_E_P);
					//}

				}
				//} 
			}
		}
	}

	protected void RatioAdjustInitial(double S_R, double E_R, double preservetemp) {
		double optimalValue_S_R = 0.0;
		double optimalValue_E_R = 0.0;
		if (totalCom > 0) {
			double conf_S_R = getConformanceValue(S_R, preservetemp, cutoff, utility, nodeSig, log);
			optimalValue_S_R = getOptimalValue(S_R, preservetemp, cutoff, utility, nodeSig, conf_S_R);
			comCount++;
			totalCom--;
			if (conf_S_R > conformance && getReadability() && fuzzyOptimalResults.size() < qualityPR) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(S_R, preservetemp, cutoff, utility, nodesig, optimalValue_S_R);
				fuzzyOptimalResult.setValues(S_R, preservetemp, cutoff, utility, nodeSig, conf_S_R, getDetail(),
						getComplexity(), optimalValue_S_R, getReadability());
				if (optimalValue_S_R >= opC || Math.abs(optimalValue_S_R - opC) < 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					//fuzzyoptimalresults.add(fuzzyoptimalresult);
				}
				AddToOptimalListTemp(fuzzyOptimalResult);
			}
		}
		if (totalCom > 0) {
			double conf_E_R = getConformanceValue(E_R, preservetemp, cutoff, utility, nodeSig, log);
			optimalValue_E_R = getOptimalValue(E_R, preservetemp, cutoff, utility, nodeSig, conf_E_R);
			comCount++;
			totalCom--;
			if (conf_E_R > conformance && getReadability() && fuzzyOptimalResults.size() < qualityPR) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(E_R, preservetemp, cutoff, utility, nodesig, optimalValue_E_R);
				fuzzyOptimalResult.setValues(E_R, preservetemp, cutoff, utility, nodeSig, conf_E_R, getDetail(),
						getComplexity(), optimalValue_E_R, getReadability());
				if (optimalValue_E_R >= opC || Math.abs(optimalValue_S_R - opC) < 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					//fuzzyoptimalresults.add(fuzzyoptimalresult);
				}
				AddToOptimalListTemp(fuzzyOptimalResult);
			}
		}
		if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
			AdjustRatio(S_R, E_R, preservetemp, optimalValue_S_R, optimalValue_E_R);
		}
	}

	protected void AdjustPreserve(double S_P, double E_P) {
		double preservetemp = (S_P + E_P) / 2.0;
		if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
			if (Math.abs(E_P - preservetemp) >= (mutaStepOfPreserve / 2.0)) {
				//AdjustRatio(ratioall[ratioall.length-count],ratioall[ratioall.length-1],preservetemp);
				//RatioAdjustInitial(Math.max(avgOfRatio-mutastepOfRatio,0.0),Math.min(avgOfRatio+mutastepOfRatio,1.0),preservetemp);
				//RatioAdjustInitial(Math.max(avgOfRatio-0.2,0.0),Math.min(avgOfRatio+0.2,1.0),preservetemp);
				//'RatioAdjustInitial(0.0,ratioall[ratioall.length-1],preservetemp);
				RatioAdjustInitial(Math.max(ratio - 0.15, ratioAll[ratioAll.length - count] / 2.0),
						Math.min(ratio + 0.1, (1.0 + ratioAll[ratioAll.length - 1]) / 2.0), preservetemp);
				//AdjustRatioStep(preservetemp);
				AdjustPreserve(preservetemp, E_P);
				AdjustPreserve(S_P, preservetemp);
			}
		}
	}

	protected void AdjustRatio(double S_R, double E_R, double preserve_t, double optimalValue_S_R,
			double optimalValue_E_R) {
		double ratiotemp = (S_R + E_R) / 2.0;
		if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
			if (Math.abs(E_R - ratiotemp) >= 0.02) {
				double conf = getConformanceValue(ratiotemp, preserve_t, cutoff, utility, nodeSig, log);
				double optimalValue = getOptimalValue(ratiotemp, preserve_t, cutoff, utility, nodeSig, conf);
				comCount++;
				totalCom--;
				//if(Math.max(Math.abs(optimalValue-optimalValue_S_R), Math.abs(optimalValue-optimalValue_E_R))>0.0000001){
				if (conf > conformance && getReadability()) {
					fuzzyOptimalResult = new FuzzyOptimalResult();
					fuzzyOptimalResult.setValues(ratiotemp, preserve_t, cutoff, utility, nodeSig, conf, getDetail(),
							getComplexity(), optimalValue, getReadability());
					if (optimalValue >= opC || Math.abs(optimalValue - opC) <= 0.003) {
						//fuzzyoptimalresults.add(fuzzyoptimalresult);
						AddToOptimalList(fuzzyOptimalResult);
					}
					AddToOptimalListTemp(fuzzyOptimalResult);
					AdjustRatio(S_R, ratiotemp, preserve_t, optimalValue_S_R, optimalValue);
					AdjustRatio(ratiotemp, E_R, preserve_t, optimalValue, optimalValue_E_R);
				} else if (!getReadability()) {
					AdjustRatio(S_R, ratiotemp, preserve_t, optimalValue_S_R, optimalValue);
				} else {
					if (Math.abs(optimalValue - optimalValue_E_R) < 0.0000001) {
						AdjustRatio(S_R, ratiotemp, preserve_t, optimalValue_S_R, optimalValue);
					} else if (Math.abs(optimalValue - optimalValue_S_R) < 0.0000001) {
						AdjustRatio(ratiotemp, E_R, preserve_t, optimalValue, optimalValue_E_R);
					} else {
						if (optimalValue > optimalValue_S_R && optimalValue < optimalValue_E_R) {
							AdjustRatio(ratiotemp, E_R, preserve_t, optimalValue, optimalValue_E_R);
						} else if (optimalValue < optimalValue_S_R && optimalValue > optimalValue_E_R) {
							AdjustRatio(S_R, ratiotemp, preserve_t, optimalValue_S_R, optimalValue);
						} else if (optimalValue > optimalValue_S_R && optimalValue > optimalValue_E_R) {
							AdjustRatio(S_R, ratiotemp, preserve_t, optimalValue_S_R, optimalValue);
							AdjustRatio(ratiotemp, E_R, preserve_t, optimalValue, optimalValue_E_R);
						}
					}
				}
				//}
			}
		}
	}

	protected void PreserveAdjustInitial(double S_P, double E_P, double sign) {
		if (totalCom > 0) {
			double conf_S_P = getConformanceValue(ratio, S_P, cutoff, utility, nodeSig, log);
			double optimalValue_S_P = getOptimalValue(ratio, S_P, cutoff, utility, nodeSig, conf_S_P);
			comCount++;
			totalCom--;
			if (conf_S_P > conformance && getReadability() && fuzzyOptimalResults.size() < qualityPR) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(ratio, S_P, cutoff, utility, nodesig, optimalValue_S_P);
				fuzzyOptimalResult.setValues(ratio, S_P, cutoff, utility, nodeSig, conf_S_P, getDetail(),
						getComplexity(), optimalValue_S_P, getReadability());
				if (optimalValue_S_P >= opC || Math.abs(optimalValue_S_P - opC) <= 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					//fuzzyoptimalresults.add(fuzzyoptimalresult);
				}
				AddToOptimalListTemp(fuzzyOptimalResult);
				//fuzzyoptimalresults.add(fuzzyoptimalresult);

			}
		}
		if (totalCom > 0) {
			double conf_E_P = getConformanceValue(ratio, E_P, cutoff, utility, nodeSig, log);
			double optimalValue_E_P = getOptimalValue(ratio, E_P, cutoff, utility, nodeSig, conf_E_P);
			comCount++;
			totalCom--;
			if (conf_E_P > conformance && getReadability() && fuzzyOptimalResults.size() < qualityPR) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(ratio, E_P, cutoff, utility, nodesig, optimalValue_E_P);
				fuzzyOptimalResult.setValues(ratio, E_P, cutoff, utility, nodeSig, conf_E_P, getDetail(),
						getComplexity(), optimalValue_E_P, getReadability());
				if (optimalValue_E_P >= opC || Math.abs(optimalValue_E_P - opC) <= 0.003) {
					AddToOptimalList(fuzzyOptimalResult);
					//fuzzyoptimalresults.add(fuzzyoptimalresult);
				}
				AddToOptimalListTemp(fuzzyOptimalResult);
			}
		}
		if (fuzzyOptimalResults.size() < qualityPR && totalCom >= 0) {
			if (sign != -1) {
				AdjustPreserve2(S_P, E_P);
			} else {
				AdjustPreserve2(S_P, E_P);
			}
		}
	}

	public void AdjustPreserve2(double S_P, double E_P) {
		double preservetemp = (S_P + E_P) / 2.0;
		if (fuzzyOptimalResults.size() < qualityPR && totalCom > 0) {
			if (Math.abs(E_P - preservetemp) >= 0.02) {
				//AdjustRatio(ratioall[ratioall.length-count],ratioall[ratioall.length-1],preservetemp);
				//RatioAdjustInitial(Math.max(avgOfRatio-mutastepOfRatio,0.0),Math.min(avgOfRatio+mutastepOfRatio,1.0),preservetemp);
				//RatioAdjustInitial(Math.max(avgOfRatio-0.2,0.0),Math.min(avgOfRatio+0.2,1.0),preservetemp);
				//'RatioAdjustInitial(0.0,ratioall[ratioall.length-1],preservetemp);
				//' RatioAdjustInitial(Math.max(ratio-0.12,0.01),Math.min(ratio+0.1,1.0),preservetemp);
				RatioAdjustInitial(Math.max(ratio - 0.25, ratioAll[ratioAll.length - count] / 2.0),
						Math.min(ratio + 0.1, (1.0 + ratioAll[ratioAll.length - 1]) / 2.0), preservetemp);
				//AdjustRatioStep(preservetemp);
				AdjustPreserve2(preservetemp, E_P);
				AdjustPreserve2(S_P, preservetemp);
			}
		}
	}

	public void getBetterOptimalvalueCutoff() {
		double[] optimalvalues = getSortedBetterOptimalvalueCutoffs();
		for (int j = 0; j < optimalvalues.length; j++) {
			double op_temp = optimalvalues[optimalvalues.length - j - 1];
			for (int i = 0; i < cutoffResults.size(); i++) {
				double opmv = cutoffResults.get(i).optimalvalue;
				if (opmv == op_temp) {
					cutoffResultsList.add(cutoffResults.get(i));
				}
			}
		}
	}

	public double[] getSortedBetterOptimalvalueCutoffs() {
		double[] optimalvalues = new double[cutoffResults.size()];
		for (int i = 0; i < cutoffResults.size(); i++) {
			optimalvalues[i] = cutoffResults.get(i).optimalvalue;
		}
		Arrays.sort(optimalvalues); // in ascending numerical order
		return optimalvalues;
	}

	public double[] getSortedBetterOptimalvalueNodesigs() {
		double[] optimalvalues = new double[nodeSigOptimals.size()];
		for (int i = 0; i < nodeSigOptimals.size(); i++) {
			optimalvalues[i] = nodeSigOptimals.get(i).optimalvalue;
		}
		Arrays.sort(optimalvalues); // in ascending numerical order
		return optimalvalues;
	}

	public void getBetterOptimalvalueNodesig() {
		double[] optimalvalues = getSortedBetterOptimalvalueNodesigs();
		for (int j = 0; j < optimalvalues.length; j++) {
			double op_temp = optimalvalues[optimalvalues.length - j - 1];
			for (int i = 0; i < nodeSigOptimals.size(); i++) {
				double opmv = nodeSigOptimals.get(i).optimalvalue;
				if (opmv == op_temp) {
					nodeSigOptimalsList.add(nodeSigOptimals.get(i));
				}
			}
		}

	}

	protected void CutoffAdjustInitial(double S_C, double E_C) {
		double optimalValue_S_C = 0.0;
		double optimalValue_E_C = 0.0;
		if (totalCom > 0) {
			double conf_S_C = getConformanceValue(ratio, preserve, S_C, utility, nodeSig, log);
			optimalValue_S_C = getOptimalValue(ratio, preserve, S_C, utility, nodeSig, conf_S_C);
			comCount++;
			totalCom--;
			if (conf_S_C > conformance && getReadability() && fuzzyOptimalResults.size() < qualityC) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(ratio, preserve, S_C, utility, nodesig, optimalValue_S_C);
				fuzzyOptimalResult.setValues(ratio, preserve, S_C, utility, nodeSig, conf_S_C, getDetail(),
						getComplexity(), optimalValue_S_C, getReadability());
				AddToOptimalListTemp(fuzzyOptimalResult);
				AddToOptimalList(fuzzyOptimalResult);
			}
		}
		if (totalCom > 0) {
			double conf_E_C = getConformanceValue(ratio, preserve, E_C, utility, nodeSig, log);
			optimalValue_E_C = getOptimalValue(ratio, preserve, E_C, utility, nodeSig, conf_E_C);
			comCount++;
			totalCom--;
			if (conf_E_C > conformance && getReadability() && fuzzyOptimalResults.size() < qualityC) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(ratio, preserve, E_C, utility, nodesig, optimalValue_E_C);
				fuzzyOptimalResult.setValues(ratio, preserve, E_C, utility, nodeSig, conf_E_C, getDetail(),
						getComplexity(), optimalValue_E_C, getReadability());
				AddToOptimalListTemp(fuzzyOptimalResult);
				AddToOptimalList(fuzzyOptimalResult);
			}
		}
		if (fuzzyOptimalResults.size() < qualityC && totalCom > 0) {
			AdjustCutoff(S_C, E_C, optimalValue_S_C, optimalValue_E_C);
		}
	}

	public void AdjustCutoff(double S_C, double E_C, double optimalValue_S_C, double optimalValue_E_C) {
		double cutofftemp = (S_C + E_C) / 2.0;
		if (fuzzyOptimalResults.size() < qualityC && totalCom > 0) {
			if (Math.abs(E_C - cutofftemp) > 0.02) {
				double conf = getConformanceValue(ratio, preserve, cutofftemp, utility, nodeSig, log);
				double optimalValue = getOptimalValue(ratio, preserve, cutofftemp, utility, nodeSig, conf);
				comCount++;
				totalCom--;
				if (cutoffResults.size() < 3) {
					cutoffResult = new CutoffResult();
					cutoffResult.setValues(cutofftemp, optimalValue);
					cutoffResults.add(cutoffResult);
				}
				if (conf >= conformance && getReadability()) {
					fuzzyOptimalResult = new FuzzyOptimalResult();
					fuzzyOptimalResult.setValues(ratio, preserve, cutofftemp, utility, nodeSig, conf, getDetail(),
							getComplexity(), optimalValue, getReadability());
					AddToOptimalListTemp(fuzzyOptimalResult);
					AddToOptimalList(fuzzyOptimalResult);

					if (optimalValue == optimalValue_E_C) {
						AdjustCutoff(S_C, cutofftemp, optimalValue_S_C, optimalValue);
					} else if (optimalValue == optimalValue_S_C) {
						AdjustCutoff(cutofftemp, E_C, optimalValue, optimalValue_E_C);
					} else {
						if (optimalValue > optimalValue_S_C && optimalValue > optimalValue_E_C) {
							AdjustCutoff(S_C, cutofftemp, optimalValue_S_C, optimalValue);
							AdjustCutoff(cutofftemp, E_C, optimalValue, optimalValue_E_C);
						} else if (optimalValue > optimalValue_S_C && optimalValue < optimalValue_E_C) {
							AdjustCutoff(cutofftemp, E_C, optimalValue, optimalValue_E_C);
						} else if (optimalValue < optimalValue_S_C && optimalValue > optimalValue_E_C) {
							AdjustCutoff(S_C, cutofftemp, optimalValue_S_C, optimalValue);
						}
						//AdjustCutoff(S_C,cutofftemp,optimalValue_S_C,optimalValue);
						//AdjustCutoff(cutofftemp,E_C,optimalValue,optimalValue_E_C);		
					}
				} else if (!getReadability()) {
					if (conf > conformance) {
						AdjustCutoff(S_C, cutofftemp, optimalValue_S_C, optimalValue);
					} else {
						/*
						 * NodesigAdjustInitial(S_N,E_N);
						 * getBetterOptimalvalueNodesig(); if
						 * (NodesigOptimalsList.size()!=0){ for (int
						 * i=0;i<NodesigOptimalsList.size();i++){
						 * nodesig=NodesigOptimalsList.get(i).nodesignificance;
						 * CutoffAdjustInitial(0.02,1.0); } }
						 */
					}
				} else {
					AdjustCutoff(cutofftemp, E_C, optimalValue, optimalValue_E_C);
				}
			}
		}
	}

	public void AdjustNodesigInitial(int S_I, int E_I) {
		double S_N = nodeSigAll[S_I];
		double E_N = nodeSigAll[E_I];
		double optimalValue_S_N = 0.0;
		double optimalValue_E_N = 0.0;
		if (totalCom > 0) {
			double conf_S_N = getConformanceValue(ratio, preserve, cutoff, utility, S_N, log);
			optimalValue_S_N = getOptimalValue(ratio, preserve, cutoff, utility, S_N, conf_S_N);
			comCount++;
			totalCom--;
			if (conf_S_N > conformance && getReadability() && fuzzyOptimalResults.size() < qualityN) {
				fuzzyOptimalResult = new FuzzyOptimalResult();
				//fuzzyoptimalresult.setValues(ratio, preserve, cutoff, utility, S_N, optimalValue_S_N);
				fuzzyOptimalResult.setValues(ratio, preserve, cutoff, utility, S_N, conf_S_N, getDetail(),
						getComplexity(), optimalValue_S_N, getReadability());
				AddToOptimalListTemp(fuzzyOptimalResult);
				AddToOptimalList(fuzzyOptimalResult);
			}
		}
		if (totalCom > 0) {
			double conf_E_N = getConformanceValue(ratio, preserve, cutoff, utility, E_N, log);
			optimalValue_E_N = getOptimalValue(ratio, preserve, cutoff, utility, E_N, conf_E_N);
			comCount++;
			totalCom--;
			if (E_N != 1.0 && fuzzyOptimalResults.size() < qualityN) {
				if (conf_E_N > conformance && getReadability()) {
					fuzzyOptimalResult = new FuzzyOptimalResult();
					//fuzzyoptimalresult.setValues(ratio, preserve, cutoff, utility, E_N, optimalValue_E_N);
					fuzzyOptimalResult.setValues(ratio, preserve, cutoff, utility, E_N, conf_E_N, getDetail(),
							getComplexity(), optimalValue_E_N, getReadability());
					AddToOptimalListTemp(fuzzyOptimalResult);
					AddToOptimalList(fuzzyOptimalResult);
				}
			}
		}

		if (fuzzyOptimalResults.size() < qualityN && totalCom > 0) {
			AdjustNodesig(S_I, E_I, optimalValue_S_N, optimalValue_E_N);
		}
	}

	public void AdjustNodesig(int S_I, int E_I, double optimalValue_S_N, double optimalValue_E_N) {
		int nodesigIndexTemp = (S_I + E_I) / 2;
		double testIndex = (S_I + E_I) / 2.0;
		double nodesignificancetemp = nodeSigAll[nodesigIndexTemp];

		if (fuzzyOptimalResults.size() < qualityN && totalCom > 0) {
			if (Math.abs(E_I - testIndex) >= 1 && totalCom > 0) {
				double conf = getConformanceValue(ratio, preserve, cutoff, utility, nodesignificancetemp, log);
				double optimalValue = getOptimalValue(ratio, preserve, cutoff, utility, nodesignificancetemp, conf);
				comCount++;
				totalCom--;
				if (conformance - conf <= 0.08 && getReadability()) {
					if (nodeSigOptimals.size() < 4) {
						nodeSigOptimal = new NodesigListResult();
						nodeSigOptimal.setValues(nodesignificancetemp, optimalValue);
						nodeSigOptimals.add(nodeSigOptimal);
					}
					if (conf > conformance) {
						fuzzyOptimalResult = new FuzzyOptimalResult();
						fuzzyOptimalResult.setValues(ratio, preserve, cutoff, utility, nodesignificancetemp, conf,
								getDetail(), getComplexity(), optimalValue, getReadability());
						AddToOptimalListTemp(fuzzyOptimalResult);
						AddToOptimalList(fuzzyOptimalResult);
						//AdjustNodesig(S_I,nodesigIndexTemp,optimalValue_S_N,optimalValue);
						//AdjustNodesig(nodesigIndexTemp,E_I,optimalValue,optimalValue_E_N);
					}
					/*
					 * else{ AdjustNodesig(nodesigIndexTemp,E_I,optimalValue,
					 * optimalValue_E_N); }
					 */

					if (optimalValue > optimalValue_S_N && optimalValue > optimalValue_E_N) {
						AdjustNodesig(S_I, nodesigIndexTemp, optimalValue_S_N, optimalValue);
						AdjustNodesig(nodesigIndexTemp, E_I, optimalValue, optimalValue_E_N);
					}
					if (optimalValue > optimalValue_S_N && optimalValue < optimalValue_E_N) {
						AdjustNodesig(nodesigIndexTemp, E_I, optimalValue, optimalValue_E_N);
					} else if (optimalValue < optimalValue_S_N && optimalValue > optimalValue_E_N) {
						AdjustNodesig(S_I, nodesigIndexTemp, optimalValue_S_N, optimalValue);
					}

				} else {
					AdjustNodesig(nodesigIndexTemp, E_I, optimalValue, optimalValue_E_N);
				}
			}
		}

	}

	public double getDetail() {
		return (detail.getDetail() + getEdgesDetail()) / 2.0;
	}

	public double getComplexity() {
		//return (graph.getFMEdges().size()/(graph.getNodes().size()*1.0))/(graph.getNumberOfInitialNodes()*1.0);
		return (graph.getFMEdges().size() + graph.getNodes().size())
				/ (tEdges * 1.0 + graph.getNumberOfInitialNodes() * 1.0);
		//return (0.8*getEdgesNumberRatio()+0.2*getPrimitiveNodeNumberRatio());
	}

	protected void AddToOptimalListTemp(FuzzyOptimalResult fopr) {
		int i = 0;
		for (i = 0; i < fuzzyOptimalResultsTemp.size(); i++) {
			if (fopr.ratio == fuzzyOptimalResultsTemp.get(i).ratio
					&& fopr.preserve == fuzzyOptimalResultsTemp.get(i).preserve
					&& fopr.cutoff == fuzzyOptimalResultsTemp.get(i).cutoff
					&& fopr.utility == fuzzyOptimalResultsTemp.get(i).utility
					&& fopr.nodesig == fuzzyOptimalResultsTemp.get(i).nodesig) {
				break;
			}
		}
		if (i >= fuzzyOptimalResultsTemp.size()) {
			fuzzyOptimalResultsTemp.add(fopr);
			fuzzyOptimalResultsAll.add(fopr);
		}
	}

	protected void AddToOptimalList(FuzzyOptimalResult fopr) {
		int i = 0;
		if (fuzzyOptimalResults.size() > 0) {
			for (i = 0; i < fuzzyOptimalResults.size(); i++) {
				if (fopr.ratio == fuzzyOptimalResults.get(i).ratio
						&& fopr.preserve == fuzzyOptimalResults.get(i).preserve
						&& fopr.cutoff == fuzzyOptimalResults.get(i).cutoff
						&& fopr.utility == fuzzyOptimalResults.get(i).utility
						&& fopr.nodesig == fuzzyOptimalResults.get(i).nodesig) {
					break;
				}
			}
			if (i >= fuzzyOptimalResults.size()) {
				fuzzyOptimalResults.add(fopr);
				if (visit > 1) {
				}
			}
		} else {
			fuzzyOptimalResults.add(fopr);
		}
	}

	public double getConformanceValue(double ratio, double preserve, double cutoff, double utility, double nodesig,
			XLog log) {
		//get new graph
		updateGraphAttributesFromUI_jiaojiao(ratio, preserve, cutoff, utility, nodesig);
		readGraphAttributes_jiaojiao();
		graph.initializeGraph();
		fastTransformer.transform(graph);
		//Set edges for Fuzzy Graph
		graph.setEdgeImpls();
		try {
			replay = new Conformance(graph, log);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return replay.getValue();
	}

	public boolean getReadability() {
		int selfLoopEdges = 0;
		FMNode node;
		for (int i = 0; i < graph.getNumberOfInitialNodes(); i++) {
			node = graph.getNodeMappedTo(i);
			if (node != null && (node instanceof FMClusterNode) == false) {
				if (graph.getBinarySignificance(i, i) != 0) {
					selfLoopEdges++;
				}
			}
		}
		double avgEdges = (graph.getFMEdges().size() - selfLoopEdges) / (graph.getNodes().size() * 1.0);
		boolean readable = false;
		if ((graph.getNodes().size() <= 18 && avgEdges <= 2.2)
				|| (graph.getNodes().size() > 18 && graph.getNodes().size() <= 24 && avgEdges <= 2.0)
				|| (graph.getNodes().size() > 24 && graph.getNodes().size() <= 70 && avgEdges <= 1.5)
				|| (graph.getNodes().size() > 70 && avgEdges <= 1.1)) {
			readable = true;
		} else {
			readable = false;
		}
		return readable;
	}

	public double getInitialRoughCutoff(double S_C, double E_C) {
		double cutoff1 = (S_C + E_C) / 2.0;
		double cutofftemp = 0.02;
		double optimalValue_N_1 = getOptimalValue(ratio, preserve, S_C, utility, nodeSig, 0.0);
		double optimalValue_N_2 = getOptimalValue(ratio, preserve, cutoff1, utility, nodeSig, 0.0);
		double optimalValue_N_3 = getOptimalValue(ratio, preserve, E_C, utility, nodeSig, 0.0);
		comCount = comCount + 3;
		totalCom = totalCom - 3;
		double maxOp = Math.max(optimalValue_N_1, optimalValue_N_2);
		if (maxOp == optimalValue_N_1) {
			cutofftemp = S_C;
		} else if (maxOp == optimalValue_N_2) {
			cutofftemp = cutoff1;
		} else if (maxOp == optimalValue_N_3) {
			cutofftemp = E_C;
		}
		return cutofftemp;
	}

	public double getInitialRoughNodesig(double S_n, double E_n) {
		double nodesigtemp = mNodeSig;
		double optimalValue_N_1 = getOptimalValue(ratio, preserve, cutoff, utility, S_n, 0.0);
		double optimalValue_N_2 = getOptimalValue(ratio, preserve, cutoff, utility, mNodeSig, 0.0);
		double optimalValue_N_3 = getOptimalValue(ratio, preserve, cutoff, utility, E_n, 0.0);
		double maxOp = Math.max(optimalValue_N_1, optimalValue_N_2);
		if (maxOp == optimalValue_N_1) {
			nodesigtemp = S_n;
		} else if (maxOp == optimalValue_N_2) {
			nodesigtemp = mNodeSig;
		} else if (maxOp == optimalValue_N_3) {
			nodesigtemp = E_n;
		}
		return nodesigtemp;
	}

	public double getOptimalValue(double ratio, double preserve, double cutoff, double utility, double nodesig,
			double conf) {
		updateGraphAttributesFromUI_jiaojiao(ratio, preserve, cutoff, utility, nodesig);
		readGraphAttributes_jiaojiao();
		graph.initializeGraph();
		fastTransformer.transform(graph);
		//Set edges for Fuzzy Graph
		graph.setEdgeImpls();

		try {
			detail = new FuzzyDetailAnalysis(graph, log);
			if (conf == 0.0) {
				replay = new Conformance(graph, log);
				conf = replay.getValue();
			}

		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double Detail = (detail.getDetail() + getEdgesDetail()) / 2.0;
		double absoluteComplex = (graph.getFMEdges().size() + graph.getNodes().size())
				/ (tEdges * 1.0 + graph.getNumberOfInitialNodes() * 1.0);
		//double absoluteComplex=(graph.getFMEdges().size()/(graph.getNodes().size()*1.0))/(graph.getNumberOfInitialNodes()*1.0);
		//double absoluteComplex=(0.8*getEdgesNumberRatio()+0.2*getPrimitiveNodeNumberRatio());
		double complex = absoluteComplex;
		double fitness = Detail * conf;
		double optimalvalue = ((1 - complex) + fitness) / 2.0;
		//double nomalizedOp=(optimalvalue-0.5)/0.3;
		return optimalvalue;
	}

	public double getEdgesDetail() {
		double currentEdgesDetail = 0.0;
		for (FMEdge<FMNode, FMNode> fmEdge : graph.getEdgeImpls()) {
			currentEdgesDetail += fmEdge.getSignificance();
		}
		return currentEdgesDetail / originalEdgesDetail;
	}

	public double getEdgesNumberRatio() {
		return graph.getFMEdges().size() / (tEdges * 1.0);
	}

	public double getPrimitiveNodeNumberRatio() {
		return graph.getNodes().size() / (graph.getNumberOfInitialNodes() * 1.0);
	}

	protected void updateGraphAttributesFromUI_jiaojiao(Double ratio, Double preserve, Double cutoff, Double utility,
			Double nodesig) {
		graph.setAttribute(EDGES_FUZZY_IGNORE_LOOPS, TRUE);
		graph.setAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE, FALSE);
		graph.setAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE, TRUE);
		graph.setAttribute(CONCURRENCY_RATIO, Double.toString(ratio));
		graph.setAttribute(CONCURRENCY_THRESHOLD, Double.toString(preserve));
		graph.setAttribute(FUZZY_EDGE_CUTOFF, Double.toString(cutoff));
		graph.setAttribute(FUZZY_EDGE_RATIO, Double.toString(utility));
		graph.setAttribute(NODE_CUTOFF, Double.toString(nodesig));
		graph.setAttribute(EDGE_TRANSFORMER_SELECTION, EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES);
	}

	protected boolean readGraphAttributes_jiaojiao() {
		String edgeTransformerSelection = graph.getAttribute(EDGE_TRANSFORMER_SELECTION);
		if (edgeTransformerSelection != null) {
			if (edgeTransformerSelection.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_BEST_EDGES)) {
				//edgesBestRadioButton.setSelected(true);
				fastTransformer.addInterimTransformer(bestEdgeTransformer);
			} else if (edgeTransformerSelection.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES)) {
				//edgesFuzzyRadioButton.setSelected(true);
				fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			}
		}
		String concurrencyTransformerActive = graph.getAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE);
		if (concurrencyTransformerActive != null) {
			if (concurrencyTransformerActive.equals(TRUE)) {
				//edgesConcurrencyActiveBox.setSelected(true);
				fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			} else if (concurrencyTransformerActive.equals(FALSE)) {
				//edgesConcurrencyActiveBox.setSelected(false);
				fastTransformer.removePreTransformer(concurrencyEdgeTransformer);
			}
		}
		String nodeCutoff = graph.getAttribute(NODE_CUTOFF);
		if (nodeCutoff != null) {
			double nodeThreshold = Double.parseDouble(nodeCutoff);
			//nodeSignificanceSlider.setValue((int) (nodeThreshold * 1000.0));
			//nodeSignificanceLabel.setText(MutableFuzzyGraph.format(nodeThreshold));
			fastTransformer.setThreshold(nodeThreshold);
		}
		String fuzzyEdgeRatio = graph.getAttribute(FUZZY_EDGE_RATIO);
		if (fuzzyEdgeRatio != null) {
			double fuzzyERatio = Double.parseDouble(fuzzyEdgeRatio);
			//edgesFuzzyRatioSlider.setValue((int) (fuzzyERatio * 1000.0));
			//edgesFuzzyRatioLabel.setText(MutableFuzzyGraph.format(fuzzyERatio));
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(fuzzyERatio);
		}
		String fuzzyEdgeCutoff = graph.getAttribute(FUZZY_EDGE_CUTOFF);
		if (fuzzyEdgeCutoff != null) {
			double fuzzyECutoff = Double.parseDouble(fuzzyEdgeCutoff);
			//edgesFuzzyPercentageSlider.setValue((int) (fuzzyECutoff * 1000.0));
			//edgesFuzzyPercentageLabel.setText(MutableFuzzyGraph.format(fuzzyECutoff));
			fuzzyEdgeTransformer.setPreservePercentage(fuzzyECutoff);
		}
		String concurrencyThreshold = graph.getAttribute(CONCURRENCY_THRESHOLD);
		if (concurrencyThreshold != null) {
			double threshold = Double.parseDouble(concurrencyThreshold);
			//edgesConcurrencyThresholdSlider.setValue((int) (threshold * 1000.0));
			//edgesConcurrencyThresholdLabel.setText(MutableFuzzyGraph.format(threshold));
			concurrencyEdgeTransformer.setPreserveThreshold(threshold);
		}
		String concurrencyRatio = graph.getAttribute(CONCURRENCY_RATIO);
		if (concurrencyRatio != null) {
			double ratio = Double.parseDouble(concurrencyRatio);
			//edgesConcurrencyRatioSlider.setValue((int) (ratio * 1000.0));
			//edgesConcurrencyRatioLabel.setText(MutableFuzzyGraph.format(ratio));
			concurrencyEdgeTransformer.setRatioThreshold(ratio);
		}
		String ignoreLoops = graph.getAttribute(EDGES_FUZZY_IGNORE_LOOPS);
		if (ignoreLoops != null) {
			if (ignoreLoops.equals(TRUE)) {
				//edgesFuzzyIgnoreLoopBox.setSelected(true);
				fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			} else if (ignoreLoops.equals(FALSE)) {
				//edgesFuzzyIgnoreLoopBox.setSelected(false);
				fuzzyEdgeTransformer.setIgnoreSelfLoops(false);
			}
		}
		String interpretAbsolute = graph.getAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE);
		if (interpretAbsolute != null) {
			if (interpretAbsolute.equals(TRUE)) {
				//edgesFuzzyInterpretAbsoluteBox.setSelected(true);
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(true);
			} else if (interpretAbsolute.equals(FALSE)) {
				//edgesFuzzyInterpretAbsoluteBox.setSelected(false);
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			}
		}
		return false;
	}

	public FuzzyOptimalResult getMaxOptimalvalueFuzzyResult(ArrayList<FuzzyOptimalResult> fuzzyoptimalresults2) {
		FuzzyOptimalResult frz = new FuzzyOptimalResult();
		if (fuzzyoptimalresults2.size() != 0) {
			frz = fuzzyoptimalresults2.get(0);
			for (int i = 0; i < fuzzyoptimalresults2.size(); i++) {
				if (frz.optimalvalue < fuzzyoptimalresults2.get(i).optimalvalue
						&& Math.abs(frz.optimalvalue - fuzzyoptimalresults2.get(i).optimalvalue) > 0.00002) {
					frz = fuzzyoptimalresults2.get(i);
				} else if (Math.abs(frz.optimalvalue - fuzzyoptimalresults2.get(i).optimalvalue) <= 0.00002) {
					if (frz.utility > fuzzyoptimalresults2.get(i).utility) {
						frz = fuzzyoptimalresults2.get(i);
					}
				}
			}
		} else {
			if (fuzzyOptimalResultsAll.size() != 0) {
				frz = getMaxOptimalvalueFuzzyResult(fuzzyOptimalResultsAll);
			} else {
				frz.preserve = 0.5;
				frz.ratio = 0.5;
				frz.cutoff = 0.02;
				frz.utility = 0.8;
				frz.nodesig = nodeSig;
			}
		}
		return frz;
	}

	public void randomcalculation(FuzzyOptimalResult fuzzyoptimalresult) {
		Random generator = new Random();
		FuzzyOptimalResult frz1 = new FuzzyOptimalResult();
		ratio = generator.nextDouble();
		preserve = generator.nextDouble();
		cutoff = generator.nextDouble();
		utility = generator.nextDouble();
		nodeSig = generator.nextDouble();

		double conf = getConformanceValue(ratio, preserve, cutoff, utility, nodeSig, log);
		double optemp = getOptimalValue(ratio, preserve, cutoff, utility, nodeSig, conf);
		while ((conf < conformance
				|| (optemp < fuzzyoptimalresult.optimalvalue && Math.abs(optemp - fuzzyoptimalresult.optimalvalue) >= 0.0002) || !getReadability())) {
			generator = new Random();
			ratio = generator.nextDouble();
			preserve = generator.nextDouble();
			cutoff = generator.nextDouble();
			utility = generator.nextDouble();
			nodeSig = generator.nextDouble();

			conf = getConformanceValue(ratio, preserve, cutoff, utility, nodeSig, log);
			optemp = getOptimalValue(ratio, preserve, cutoff, utility, nodeSig, conf);

			if (conf >= conformance
					&& (optemp >= fuzzyoptimalresult.optimalvalue || Math.abs(optemp - fuzzyoptimalresult.optimalvalue) < 0.0002)
					&& getReadability()) {
				frz1.setValues(ratio, preserve, cutoff, utility, nodeSig, conf, getDetail(), getComplexity(), optemp,
						getReadability());
				randomFuzzyOptimalResults.add(frz1);
				break;
			}
		}
		frz1.setValues(ratio, preserve, cutoff, utility, nodeSig, conf, getDetail(), getComplexity(), optemp,
				getReadability());
		//randomfuzzyoptimalresults.add(frz1);
		randomFuzzyOptimalResults.add(frz1);
	}

}
