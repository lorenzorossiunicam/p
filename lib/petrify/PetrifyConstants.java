package org.processmining.plugins.petrify;

import java.util.HashMap;

/**
 * Encoding and decoding strings for Petrify and Genet.
 * 
 * Both Petrify and Genet do not accept certain character in identifiers.
 * Therefore: 1. before calling either one of them, a identifier first needs to
 * be encoded, and 2. all identifiers in the result need to be decoded again.
 * 
 * @author HVERBEEK
 * 
 */
public class PetrifyConstants {
	/**
	 * Map for encoding strings.
	 */
	private static HashMap<String, String> encodeMap;
	/**
	 * Map for decoding strings.
	 */
	private static HashMap<String, String> decodeMap;

	static {
		encodeMap = new HashMap<String, String>();
		decodeMap = new HashMap<String, String>();

		encodeMap.put(" ", "_.00");
		encodeMap.put("-", "_.01");
		encodeMap.put(":", "_.02");
		encodeMap.put("/", "_.03");
		encodeMap.put("(", "_.04");
		encodeMap.put(")", "_.05");
		encodeMap.put("&", "_.06");
		encodeMap.put("%", "_.07");
		encodeMap.put("?", "_.08");
		encodeMap.put("!", "_.09");
		encodeMap.put("#", "_.10");
		encodeMap.put("+", "_.11");
		encodeMap.put("*", "_.12");
		encodeMap.put(";", "_.13");
		encodeMap.put(",", "_.14");
		encodeMap.put("{", "_.15");
		encodeMap.put("}", "_.16");
		encodeMap.put("~", "_.17");
		encodeMap.put("$", "_.18");
		encodeMap.put("§", "_.19");
		encodeMap.put("^", "_.20");

		decodeMap.put("_.00", " ");
		decodeMap.put("_.01", "-");
		decodeMap.put("_.02", ":");
		decodeMap.put("_.03", "/");
		decodeMap.put("_.04", "(");
		decodeMap.put("_.05", ")");
		decodeMap.put("_.06", "&");
		decodeMap.put("_.07", "%");
		decodeMap.put("_.08", "?");
		decodeMap.put("_.09", "!");
		decodeMap.put("_.10", "#");
		decodeMap.put("_.11", "+");
		decodeMap.put("_.12", "*");
		decodeMap.put("_.13", ";");
		decodeMap.put("_.14", ",");
		decodeMap.put("_.15", "{");
		decodeMap.put("_.16", "}");
		decodeMap.put("_.17", "~");
		decodeMap.put("_.18", "$");
		decodeMap.put("_.19", "§");
		decodeMap.put("_.20", "^");
	}

	private static String replace(String s, HashMap<String, String> map) {
		for (String key : map.keySet()) {
			s = s.replace(key, map.get(key));
		}
		return s;
	}

	/**
	 * Encodes the given string which is to be used by either Petrify and/or
	 * Genet.
	 * 
	 * @param s
	 *            The string to be encoded.
	 * @return The encoded string.
	 */
	public static String encode(String s) {
		return replace(s, encodeMap);
	}

	/**
	 * Decodes the given string which comes from either Petrify and/or Genet.
	 * 
	 * @param s
	 *            The string to be decoded.
	 * @return The decoded string.
	 */
	public static String decode(String s) {
		return replace(s, decodeMap);
	}
}
