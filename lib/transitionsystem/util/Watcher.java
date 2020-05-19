package org.processmining.plugins.transitionsystem.util;

public class Watcher {
	/**
	 * log executing class name, method name and line number.
	 * 
	 * @param pMessage
	 */
	public static void log() {
		String signature = getSignature();
		System.err.println(signature);
	}

	/**
	 * log a message with executing class name, method name and line number.
	 * 
	 * @param pMessage
	 */
	public static void log(String pMessage) {
		String signature = getSignature();
		System.err.print(signature);
		System.err.println(" [" + pMessage + "]");
	}

	/**
	 * Get the signature of caller conforming to eclipse error parser.
	 * 
	 * @return
	 */
	protected static String getSignature() {
		StackTraceElement trace = getCaller();
		String classNameParts[] = trace.getClassName().split("[.]");
		String className = classNameParts[classNameParts.length - 1];
		String signature = className + "." + trace.getMethodName();
		signature += "(" + trace.getFileName() + ":" + trace.getLineNumber()
				+ ")";
		return signature;
	}

	/**
	 * Get the caller by scanning the stack trace.
	 * 
	 * @return
	 */
	protected static StackTraceElement getCaller() {
		String thisClassName = Watcher.class.getSimpleName();
		StackTraceElement[] traces = new Throwable().getStackTrace();
		for (StackTraceElement trace : traces) {
			String className = trace.getClassName();
			if (className.endsWith(thisClassName) == false)
				return trace;
		}
		return null;
	}

}
