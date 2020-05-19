package org.processmining.plugins.ptmerge.ptmerge;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.ptmerge.conversions.CoSeNet2PT;
import org.processmining.plugins.ptmerge.conversions.PT2CoSeNet;
import org.processmining.plugins.ptmerge.cosemap.CMap;
import org.processmining.plugins.ptmerge.cosemap.CoSeMap;
import org.processmining.plugins.ptmerge.cosenet.CoSeNet;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.arrays.ProcessTreeArray;

public class PlugIn {
	@Plugin(
		name = "Merge Process Trees Activity Map",
		parameterLabels = {"Process Tree 1", "Process Tree 2"},
		returnLabels = { "Merged Process Tree" },
		returnTypes = { ProcessTree.class },
		userAccessible = true,
		help = "Merges two Process Trees"
	)
	@UITopiaVariant(
		affiliation = UITopiaVariant.EHV,
		author = "D.M.M. Schunselaar",
		email = "D.M.M.Schunselaar@tue.nl"
	)
	public ProcessTree mergeActMap(PluginContext context, ProcessTree pt1, ProcessTree pt2){
		return mergeDAGSActMap(context, pt1, pt2);
		/*
		TaskMap map = new TaskMap();
		map.computeMap(pt1, pt2);
		Merge merge = new Merge();
		return merge.merge(pt1, pt2, map);
		*/
	}
	
	public static ProcessTree mergeDAGSActMap(PluginContext context, ProcessTree PTin, ProcessTree... PTinRest){
		try {
			CoSeNet result = PT2CoSeNet.convert(context, PTin);
			for(ProcessTree pt: PTinRest){
				CoSeNet D = PT2CoSeNet.convert(context, pt);
				CoSeMap cosemap = new CoSeMap();
				cosemap.activityCoSeMap(result, D);
				CMap cmap = new CMap();
				cmap.cmap.putAll(cosemap.cosemap);
				result = org.processmining.plugins.ptmerge.merge.Merge.CoSeMerge(result, D, cmap);
			}
			return CoSeNet2PT.convert(result);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 
	
	@Plugin(
			name = "Merge Process Trees Activity Map (PTA)",
			parameterLabels = {"ProcessTreeArray"},
			returnLabels = { "The Process Tree obtained after merging the input Process Trees using an activity PTMap" },
			returnTypes = { ProcessTree.class },
			userAccessible = true,
			help = "Merges an array of Process Trees using the activity map"
		)
		@UITopiaVariant(
			affiliation = UITopiaVariant.EHV,
			author = "D.M.M. Schunselaar",
			email = "D.M.M.Schunselaar@tue.nl"
		)
		public static ProcessTree mergeDAGSActMap(PluginContext context, ProcessTreeArray pta){
			try {
				CoSeNet result = PT2CoSeNet.convert(pta.getElement(0));
				for(int i = 1; i < pta.getSize(); i++){
					CoSeNet D = PT2CoSeNet.convert(pta.getElement(i));
					CoSeMap cosemap = new CoSeMap();
					cosemap.activityCoSeMap(result, D);
					CMap cmap = new CMap();
					cmap.cmap.putAll(cosemap.cosemap);
					result = org.processmining.plugins.ptmerge.merge.Merge.CoSeMerge(result, D, cmap);
				}
				return CoSeNet2PT.convert(result);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} 

	
	@Plugin(
		name = "Merge Process Trees Extended Map",
		parameterLabels = {"Process Tree 1", "Process Tree 2"},
		returnLabels = { "Merged Process Tree" },
		returnTypes = { ProcessTree.class },
		userAccessible = true,
		help = "Merges two Process Trees"
	)
	@UITopiaVariant(
		affiliation = UITopiaVariant.EHV,
		author = "D.M.M. Schunselaar",
		email = "D.M.M.Schunselaar@tue.nl"
	)
	public static ProcessTree mergeExtMap(PluginContext context, ProcessTree pt1, ProcessTree pt2) throws Exception{
		return mergeExtMap(pt1, pt2);
	}
	
	public static ProcessTree mergeExtMap(ProcessTree pt1, ProcessTree pt2) throws Exception{
		CoSeNet cosenet1 = PT2CoSeNet.convert(pt1);
		CoSeNet cosenet2 = PT2CoSeNet.convert(pt2);
		CoSeMap cosemap = new CoSeMap();
		cosemap.extendedCoSeMap(cosenet1, cosenet2);
		CoSeNet cosenet3 = org.processmining.plugins.ptmerge.merge.Merge.CoSeMerge(cosenet1, cosenet2, new CMap(cosenet1, cosenet2, cosemap));
		return CoSeNet2PT.convert(cosenet3);
	}
	
	@Plugin(
			name = "Merge Process Trees Extended Map (PTA)",
			parameterLabels = {"ProcessTreeArray"},
			returnLabels = { "Merged Process Tree" },
			returnTypes = { ProcessTree.class },
			userAccessible = true,
			help = "Merges an array of Process Trees using the extended map"
		)
		@UITopiaVariant(
			affiliation = UITopiaVariant.EHV,
			author = "D.M.M. Schunselaar",
			email = "D.M.M.Schunselaar@tue.nl"
		)
		public static ProcessTree mergeExtMap(PluginContext context, ProcessTreeArray pta) throws Exception{
			return mergeExtMap(pta);
		}
		
		public static ProcessTree mergeExtMap(ProcessTreeArray pta) throws Exception{
			try {
				CoSeNet result = PT2CoSeNet.convert(pta.getElement(0));
				for(int i = 1; i < pta.getSize(); i++){
					CoSeNet D = PT2CoSeNet.convert(pta.getElement(i));
					CoSeMap cosemap = new CoSeMap();
					cosemap.extendedCoSeMap(result, D);
					result = org.processmining.plugins.ptmerge.merge.Merge.CoSeMerge(result, D, new CMap(result, D, cosemap));
				}
				return CoSeNet2PT.convert(result);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
}
