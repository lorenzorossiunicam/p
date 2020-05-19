package org.processmining.plugins.ilpminer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Set;

import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverType;

public class ILPMinerStrategyManager {
	public static Class<?>[] getILPMinerStrategies(SolverType solver) {
		switch (solver) {
		case CPLEX:
			return findClasses(ILPModelCPLEX.class, ILPMinerStrategy.class)
					.toArray(new Class[0]);
		default: // LPSOLVE
			return findClasses(ILPModelJavaILP.class, ILPMinerStrategy.class)
					.toArray(new Class[0]);
		}
	}

	public static Class<?>[] getILPMinerStrategyExtensions() {
		return findClasses(ILPModelExtension.class,
				ILPMinerStrategyExtension.class).toArray(new Class[0]);
	}

	public static ArrayList<Class<?>> findClasses(Class<?> type,
			Class<? extends Annotation> annotation) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		Set<Class<?>> set = PluginManagerImpl.getInstance()
				.getKnownClassesAnnotatedWith(annotation);

		for (Class<?> clazz : set) {
			if (isSubclass(clazz, type)) {
				classes.add(clazz);
			}
		}

		return classes;
	}

	protected static boolean isSubclass(Class<?> clazz, Class<?> superClazz) {
		Class<?> currentClazz = clazz;
		while (currentClazz != null) {
			if (currentClazz.getSuperclass() == superClazz) {
				return true;
			}
			currentClazz = currentClazz.getSuperclass();
		}
		return false;
	}
}
