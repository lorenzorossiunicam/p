package org.processmining.plugins.ilpminer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ILPMinerStrategyExtension {
	/**
	 * Returns the name identifying this strategy in a human readable way.
	 * 
	 * @return
	 */
	String name() default "[Unnamed strategy]";

	/**
	 * Returns the description of this strategies resulting Petri net.
	 * 
	 * @return
	 */
	String description() default "[No description available]";

	/**
	 * Returns the author of this strategy.
	 * 
	 * @return
	 */
	String author() default "T. van der Wiel";

	/**
	 * Returns the extension that this extension builds on (e.g. requires for
	 * correct results).
	 * 
	 * @return
	 */
	Class<?> ParentExtensionClass() default ILPModelExtension.class;
}