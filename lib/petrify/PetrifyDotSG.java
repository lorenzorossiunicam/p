package org.processmining.plugins.petrify;

/**
 * Support for the Petrify/Genet state graph file format (.sg).
 * 
 * @author HVERBEEK
 * 
 */
public class PetrifyDotSG extends PetrifyDotAny {

	/**
	 * Creates a, say handle, to the file with the given absolute path.
	 * 
	 * @param absolutePath
	 *            The given absolute path.
	 */
	public PetrifyDotSG(String absolutePath) {
		super(absolutePath);
	}

	/**
	 * Returns the file extension (.sg) for this file format.
	 * 
	 * @return The file extension for Petrify/Genet state graph files.
	 */
	public static String getFileExtension() {
		return "sg";
	}

}
