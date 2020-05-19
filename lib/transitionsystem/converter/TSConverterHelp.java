package org.processmining.plugins.transitionsystem.converter;

public class TSConverterHelp {

	public final static String TEXT = ""
			+ "Mollifies the given transition system in four different ways: <ol>"
			+ "<li>Remove self loops from the transition system.<li>"
			+ "<li>Insert missing edges to complete \"diamonds\", like insert A-&gt;B if A-$gt;C, C-$gt;D, and B-$gtD are already present.</li>"
			+ "<li>Merge states with identical outgoing labels.</li>"
			+ "<li>Merge states with identical incoming labels.</li>"
			+ "</ol>.";
}
