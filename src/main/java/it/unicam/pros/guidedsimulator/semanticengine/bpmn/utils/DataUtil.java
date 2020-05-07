package it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.datastate.DataStoreState;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.datastate.DataObjState;

public class DataUtil {

	public static MidaCollabsConfiguration initData(Configuration c) {
		MidaCollabsConfiguration conf = (MidaCollabsConfiguration) DeepCopy.copy(c);

		initDataStore(conf.getSigmaDS());
		for(String p : conf.getSigmaI().keySet()) {
			for(Integer i : conf.getSigmaI().get(p).keySet()) {
				 initDataObj(conf.getSigmaI().get(p).get(i).getSigmaDO());
			}
		}
		return conf;
	}

	private static void initDataObj(DataObjState dataObjState) {
		for(String x : dataObjState.getScope().keySet()) {
			if(dataObjState.getScope().get(x) == "null") {
				//TODO inizializzazione random datastore e dataobjs
			}
		}
	}

	private static void initDataStore(DataStoreState sigmaDS) {
		// TODO Auto-generated method stub

	}

}
