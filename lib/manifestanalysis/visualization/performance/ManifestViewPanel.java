/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performance;

import info.clearthought.layout.TableLayout;

import javax.swing.JPanel;

import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;

/**
 * @author aadrians
 * Mar 5, 2012
 *
 */
public class ManifestViewPanel<N extends Manifest, C extends IPerfCounter<N>> extends JPanel {
	private static final long serialVersionUID = 826617115617858896L;

	private PIPManifestPanel<N,C> pip;
	private ZoomManifestPanel<N,C> zoom;
	public ManifestViewPanel(ManifestPerfPanel<N, C> mainPanel, int maxZoom) {
		double[][] size = new double[][]{ {TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED}} ;
		setLayout(new TableLayout(size));
		
		pip = new PIPManifestPanel<N,C>(mainPanel);
		zoom = new ZoomManifestPanel<N,C>(mainPanel, pip, maxZoom);

		add(pip, "0,0");
		add(zoom, "0,1");
	}

	
	public PIPManifestPanel<N,C> getPIP() {
		return pip;
	}
	
	public ZoomManifestPanel<N,C> getZoom(){
		return zoom;
	}
}
