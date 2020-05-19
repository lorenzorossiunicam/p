package org.processmining.plugins.ilpminer;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Provides an extension to a (set of) ILP problems by adding constraints only.
 * 
 * @author s071957
 * 
 */
public abstract class ILPModelExtension {
	private static ILPMinerStrategyExtension getAnnotation(Class<?> strategy) throws ClassNotFoundException {
		return Class.forName(strategy.getName()).getAnnotation(ILPMinerStrategyExtension.class);
	}

	public static String getName(Class<?> strategy) {
		try {
			return getAnnotation(strategy).name();
		} catch (Exception e) {
			return "[Unnamed strategy]";
		}
	}

	public static String getAuthor(Class<?> strategy) {
		try {
			return getAnnotation(strategy).author();
		} catch (Exception e) {
			return "T. van der Wiel";
		}
	}

	public static String getDescription(Class<?> strategy) {
		try {
			return getAnnotation(strategy).description();
		} catch (Exception e) {
			return "[No description available]";
		}
	}

	public static Class<?>[] getExtensionSuperClass(Class<?> strategy) {
		try {
			ArrayList<Class<?>> availableSuperClasses = new ArrayList<Class<?>>();
			Method[] methods = Class.forName(strategy.getName()).getMethods();
			for (Method m : methods) {
				if (m.isAnnotationPresent(ILPMinerStrategyExtensionImpl.class)) {
					ILPMinerStrategyExtensionImpl a = m.getAnnotation(ILPMinerStrategyExtensionImpl.class);
					availableSuperClasses.add(a.ExtensionSuperClass());
				}
			}
			return availableSuperClasses.toArray(new Class<?>[0]);
		} catch (Exception e) {
			return new Class<?>[0];
		}
	}

	public static Class<?> getParentExtensionClass(Class<?> strategy) {
		try {
			return getAnnotation(strategy).ParentExtensionClass();
		} catch (Exception e) {
			return null;
		}
	}
}
