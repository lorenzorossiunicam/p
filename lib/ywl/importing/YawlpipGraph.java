package org.processmining.plugins.ywl.importing;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Map;

import javax.swing.ToolTipManager;

import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;
import org.processmining.models.jgraph.ProMGraphModel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;

/**
 * @author David Piessens
 * @email d.a.m.piessens@student.tue.nl
 * @version May 29, 2010
 */
public class YawlpipGraph extends JGraph implements GraphModelListener, GraphLayoutCacheListener,
		GraphSelectionListener {

	private static final long serialVersionUID = -8477633603192312230L;

	public static final String PIPVIEWATTRIBUTE = "signalPIPView";

	private final YawlpipGraphModel model;
	private JGraphLayout layout;

	public YawlpipGraph(YawlpipGraphModel model, boolean isPIP) {
		super(model, model.getGraph().getGraphLayoutCache());

		getGraphLayoutCache().setMovesChildrenOnExpand(true);
		getGraphLayoutCache().setResizesParentsOnCollapse(true);
		getGraphLayoutCache().setMovesParentsOnCollapse(true);
		getGraphLayoutCache().setAutoSizeOnValueChange(true);

		this.model = model;

		this.setAntiAliased(true);
		this.setDisconnectable(false);
		this.setConnectable(false);
		this.setGridEnabled(false);
		this.setDoubleBuffered(true);
		this.setSelectionEnabled(!isPIP);
		this.setMoveBelowZero(false);
		this.setPortsVisible(true);
		this.setPortsScaled(true);
		this.setPortsOnTop(true);

		registerAsListener();

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	public boolean doViewLayout() {
		if (layout != null) {
			JGraphFacade facade = new JGraphFacade(this);
			facade.resetControlPoints();
			facade.run(layout, true);
			getGraphLayoutCache().edit(facade.createNestedMap(true, true));
			repositionToOrigin();// model.getGraph().getBounds());
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return model.toString();
	}

	public void graphChanged(GraphModelEvent e) {
		changeHandled();
	}

	/**
	 * Might be overridden to signal that a change was handled
	 */
	public void changeHandled() {

	}

	public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
		changeHandled();
	}

	public void valueChanged(GraphSelectionEvent e) {
	}

	public void repositionToOrigin() {
		JGraphFacade facade = new JGraphFacade(this);
		/*
		 * First, push everything towards the lower left corner. Provided that
		 * we will not have many groups inside groups, 100 should be sufficient.
		 * This step is needed to assure that we do not have negative
		 * coordinates for the important second step. For some some reason,
		 * getGraphOrigin() returns 0.0 instead of negative coordinates.
		 */
		// facade.translateCells(facade.getVertices(), 100.0, 100.0);
		// facade.translateCells(facade.getEdges(), 100.0, 100.0);
		// getGraphLayoutCache().edit(facade.createNestedMap(true, false));
		/*
		 * Second, pull everything back to (2,2). Works like a charm, even when
		 * a hack...
		 */
		double x = facade.getGraphOrigin().getX();
		double y = facade.getGraphOrigin().getY();
		facade.translateCells(facade.getVertices(), 2.0 - x, 2.0 - y);
		facade.translateCells(facade.getEdges(), 2.0 - x, 2.0 - y);
		getGraphLayoutCache().edit(facade.createNestedMap(true, false));
	}

	private void registerAsListener() {
		model.addGraphModelListener(this);
		addGraphSelectionListener(this);
		getGraphLayoutCache().addGraphLayoutCacheListener(this);
	}

	public int hashCode() {
		return model.getGraph().hashCode();
	}

	public JGraphLayout getUpdateLayout() {
		return layout;
	}

	public void setUpdateLayout(JGraphLayout layout) {
		this.layout = layout;
	}

}

class Change implements GraphModelEvent.GraphModelChange {

	private final Collection<Object> added;
	private final Collection<Object> removed;
	private final Collection<Object> changed;
	private final ProMGraphModel source;
	private final Rectangle2D dirtyRegion;

	public Change(ProMGraphModel source, Collection<Object> added, Collection<Object> removed,
			Collection<Object> changed, Rectangle2D dirtyRegion) {
		this.source = source;
		this.added = added;
		this.removed = removed;
		this.changed = changed;
		this.dirtyRegion = dirtyRegion;

	}

	public ConnectionSet getConnectionSet() {
		return null;
	}

	public ParentMap getParentMap() {
		return null;
	}

	public ConnectionSet getPreviousConnectionSet() {
		return null;
	}

	public ParentMap getPreviousParentMap() {
		return null;
	}

	public CellView[] getViews(GraphLayoutCache view) {
		return null;
	}

	public void putViews(GraphLayoutCache view, CellView[] cellViews) {

	}

	public Map<?, ?> getAttributes() {
		return null;
	}

	public Object[] getChanged() {
		return changed.toArray();
	}

	public Object[] getContext() {
		return null;
	}

	public Rectangle2D getDirtyRegion() {
		return dirtyRegion;
	}

	public Object[] getInserted() {
		return added.toArray();
	}

	public Map<?, ?> getPreviousAttributes() {
		return null;
	}

	public Object[] getRemoved() {
		return removed.toArray();
	}

	public Object getSource() {
		return source;
	}

	public void setDirtyRegion(Rectangle2D dirty) {

	}

}