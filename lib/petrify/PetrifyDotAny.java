package org.processmining.plugins.petrify;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Generic support for both file formats supported by Petrify and Genet: 1. The
 * state graph format (.sg), and 2. The Petri net format (.g).
 * 
 * @author HVERBEEK
 * 
 */
public class PetrifyDotAny {
	/**
	 * The absolute path to the file.
	 */
	private final String absolutePath;
	private final long length;

	/**
	 * Creates a (say) handle to a file given the absolute path to this file.
	 * 
	 * @param absolutePath
	 *            The absolute path to the file.
	 */
	PetrifyDotAny(String absolutePath) {
		this.absolutePath = absolutePath;
		length = new File(absolutePath).length();
	}

	/**
	 * Gets the name of this file.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return absolutePath;
	}

	/**
	 * Returns an input stream to this file.
	 * 
	 * @return An input stream for this file.
	 * @throws Exception
	 */
	public InputStream getInputStream() throws Exception {
		return new FileInputStream(new File(absolutePath));
	}

	public long getLength() {
		return length;
	}

}
