package org.processmining.plugins.ilpminer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.processmining.plugins.ilpminer.templates.PetriNetILPModelSettings;
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetILPModel;

/**
 * Storage class for the general settings of the iLog ILP miner
 * 
 * @author T. van der Wiel
 * 
 */
public class ILPMinerSettings {
	public enum SolverType {
		CPLEX, JAVAILP_CPLEX, JAVAILP_LPSOLVE;
		// we need to store these in the registry as a string. therefore conversion to/from int is easiest
		public static SolverType fromInt(int ordinal) {
			if (CPLEX.ordinal() == ordinal) {
				return CPLEX;
			}
			if (JAVAILP_CPLEX.ordinal() == ordinal) {
				return JAVAILP_CPLEX;
			}
			return JAVAILP_LPSOLVE;
		}
	};

	public enum SolverSetting {
		TYPE, LICENSE_DIR
	};

	private Class<?> ILPVariant;
	private ArrayList<Class<?>> ILPExtensions;
	private ILPModelSettings modelSettings;
	private final Map<SolverSetting, Object> solverSettings;

	public ILPMinerSettings() {
		// default values
		ILPVariant = PetriNetILPModel.class;
		ILPExtensions = new ArrayList<Class<?>>();
		modelSettings = new PetriNetILPModelSettings();
		solverSettings = new HashMap<SolverSetting, Object>();
		solverSettings.put(SolverSetting.TYPE, SolverType.JAVAILP_LPSOLVE);
		solverSettings.put(SolverSetting.LICENSE_DIR, "c:\\ILOG\\ILM");
	}

	public Class<?> getVariant() {
		return ILPVariant;
	}

	public Class<?>[] getExtensions() {
		Class<?>[] e = new Class[ILPExtensions.size()];
		for (int i = 0; i < ILPExtensions.size(); i++) {
			e[i] = ILPExtensions.get(i);
		}
		return e;
	}

	public ILPModelSettings getModelSettings() {
		return modelSettings;
	}

	public Object getSolverSetting(SolverSetting s) {
		return solverSettings.get(s);
	}

	public Map<SolverSetting, Object> getSolverSettings() {
		return solverSettings;
	}

	public void setVariant(Class<?> variant) {
		ILPVariant = variant;
	}

	public void addExtension(Class<?> extension) {
		ILPExtensions.add(extension);
	}

	public void setExtensions(Class<?>[] extensions) {
		clearExtensions();
		for (Class<?> extension : extensions) {
			addExtension(extension);
		}
	}

	public void clearExtensions() {
		ILPExtensions = new ArrayList<Class<?>>();
	}

	public void setModelSettings(ILPModelSettings settings) {
		modelSettings = settings;
	}

	public void setSolverSetting(SolverSetting s, Object data) {
		solverSettings.put(s, data);
	}
}
