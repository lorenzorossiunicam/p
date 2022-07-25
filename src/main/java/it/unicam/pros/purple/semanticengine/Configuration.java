package it.unicam.pros.purple.semanticengine;

import java.util.Map;
import java.util.Set;


/**
 * @author Lorenzo Rossi
 *
 */
public interface Configuration {

	/**
	 * @return
	 */
	Map<String, String> getGlobalData();

	/**
	 * @return
	 */
	Map<String, String> getLocalData();

	/**
	 * @param proc
	 * @return
	 */
	Set<String> getInstances(String proc);

	/**
	 * @return
	 */
	Set<String> getProcesses(); 

	boolean equals(Object c);
}
