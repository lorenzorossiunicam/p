package it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements;

import java.util.Collection;
import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.Task;

public class IntReceiveTask implements IntActivity{
	private String id;
	private Task miTask;
	private Collection<IntActivity> copies;
	
	public IntReceiveTask(String id, Task t) {
		this.id = id;
		this.miTask = t;
	}

	public String getId() {
		return id;
	}

	public Task getMiTask() {
		return miTask;
	}

	public void setCopies(Collection<IntActivity> parallels) {
		this.copies = parallels;
	}

	public Collection<IntActivity> getCopies() {
		return copies;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IntReceiveTask that = (IntReceiveTask) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(miTask.getId(), that.miTask.getId()) &&
				Objects.equals(copies, that.copies);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, miTask.getId(), copies);
	}
}
