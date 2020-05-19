/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performancematrix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.pnalignanalysis.visualization.performancematrix.PerfMatrixStats;

/**
 * @author aadrians Nov 14, 2012
 * 
 */
public class PerfMatrixManifestExporter {

	public static void appendStatsInBuilder(StringBuilder sb, int[] orderInProvider,
			PerfMatrixManifestProvider provider, PerfMatrixStats metric) {
		for (int i : orderInProvider) {
			for (int j : orderInProvider) {
				sb.append("\"");
				sb.append(provider.getTimeBetween(i, j, metric));
				sb.append("\",");
			}
		}
	}

	public static void appendFreqInBuilder(StringBuilder sb, int[] orderInProvider, PerfMatrixManifestProvider provider) {
		for (int i : orderInProvider) {
			for (int j : orderInProvider) {
				sb.append("\"");
				sb.append(provider.getFrequency(i, j));
				sb.append("\",");
			}
		}
	}

	public static void exportAsMatrix(TransClass[] desiredOrder, PerfMatrixManifestProvider provider, double divisor,
			File file) {
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {

			fstream = new FileWriter(file);
			out = new BufferedWriter(fstream);

			Map<TransClass, Integer> map = provider.constructMapTransClass2Int();
			int[] orderInProvider = new int[desiredOrder.length];
			String[] labels = new String[desiredOrder.length];
			for (int i = 0; i < orderInProvider.length; i++) {
				orderInProvider[i] = map.get(desiredOrder[i]);
				labels[i] = desiredOrder[i].getId();
			}

			// write throughput time
			out.write("AVERAGE");
			out.newLine();
			writeStatsMatrix(labels, orderInProvider, provider, out, PerfMatrixStats.AVG, divisor);
			out.newLine();

			out.write("MINIMUM");
			out.newLine();
			writeStatsMatrix(labels, orderInProvider, provider, out, PerfMatrixStats.MIN, divisor);
			out.newLine();

			out.write("MAXIMUM");
			out.newLine();
			writeStatsMatrix(labels, orderInProvider, provider, out, PerfMatrixStats.MAX, divisor);
			out.newLine();

			out.write("STD.DEV");
			out.newLine();
			writeStatsMatrix(labels, orderInProvider, provider, out, PerfMatrixStats.STDDEV, divisor);
			out.newLine();

			out.write("FREQUENCY");
			out.newLine();
			writeFreqMatrix(labels, orderInProvider, provider, out, PerfMatrixStats.STDDEV, divisor);
			out.newLine();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (fstream != null) {
				try {
					fstream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private static void writeStatsMatrix(String[] labels, int[] orderInProvider, PerfMatrixManifestProvider provider,
			BufferedWriter out, PerfMatrixStats metric, double divisor) throws IOException {
		out.write("\"From\\To\",");
		for (String label : labels) {
			out.write("\"");
			out.write(label);
			out.write("\",");
		}
		out.newLine();

		int i = 0;
		for (String label : labels) {
			out.write("\"");
			out.write(label);
			out.write("\",");
			for (int j : orderInProvider) {
				out.write("\"" + (provider.getTimeBetween(orderInProvider[i], j, metric) / divisor));
				out.write("\",");
			}
			out.newLine();
			i++;
		}
		out.newLine();
	}

	private static void writeFreqMatrix(String[] labels, int[] orderInProvider, PerfMatrixManifestProvider provider,
			BufferedWriter out, PerfMatrixStats metric, double divisor) throws IOException {
		out.write("\"From\\To\",");
		for (String label : labels) {
			out.write("\"");
			out.write(label);
			out.write("\",");
		}
		out.newLine();

		int i = 0;
		for (String label : labels) {
			out.write("\"");
			out.write(label);
			out.write("\",");
			for (int j : orderInProvider) {
				out.write("" + (provider.getFrequency(orderInProvider[i], j) / divisor) + "");
				out.write(",");
			}
			out.newLine();
			i++;
		}
		out.newLine();
	}
}
