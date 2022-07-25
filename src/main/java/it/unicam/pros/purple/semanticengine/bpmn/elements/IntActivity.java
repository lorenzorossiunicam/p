package it.unicam.pros.purple.semanticengine.bpmn.elements;

import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.Collection;

public interface IntActivity {
	public String getId();

	public Task getMiTask();

	public void setCopies(Collection<IntActivity> parallels);

	public Collection<IntActivity> getCopies();
}
