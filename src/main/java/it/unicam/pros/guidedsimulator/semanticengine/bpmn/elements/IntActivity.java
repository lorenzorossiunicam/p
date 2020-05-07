package it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.Task;

public interface IntActivity {
	public String getId();

	public Task getMiTask();

	public void setCopies(Collection<IntActivity> parallels);

	public Collection<IntActivity> getCopies();
}
