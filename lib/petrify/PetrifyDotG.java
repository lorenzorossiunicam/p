package org.processmining.plugins.petrify;

/**
 * Support for the Petrify/Genet Petri net file format (.g).
 * 
 * @author HVERBEEK
 * 
 */
public class PetrifyDotG extends PetrifyDotAny {

	/**
	 * Creates a, say handle, to the file with the given absolute path.
	 * 
	 * @param absolutePath
	 *            The given absolute path.
	 */
	public PetrifyDotG(String absolutePath) {
		super(absolutePath);
	}

	/**
	 * Returns the file extension (.g) for this file format.
	 * 
	 * @return The file extension for Petrify/Genet Petri net files.
	 */
	public final static String getFileExtension() {
		return "g";
	}
}
