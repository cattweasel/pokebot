package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.DateAdapter;

/**
 * POJO implementation for a task result.
 * 
 * @author Benjamin Wesp
 *
 */
@Entity
@Table(name = "db_task_result")
@XmlRootElement(name = "TaskResult")
public class TaskResult extends PokeObject {

	private static final long serialVersionUID = 1L;

	@XmlEnum(String.class)
	public static enum CompletionStatus {

		@XmlEnumValue("Success") SUCCESS,
		@XmlEnumValue("Terminated") TERMINATED
	}

	private String schedule;
	private String definition;
	private Date launched;
	private Date completed;
	private String launcher;
	private CompletionStatus completionStatus;

	/**
	 * Returns the name of the task schedule of this task result.
	 * 
	 * @return The name of the task schedule
	 */
	@Column(unique = false, nullable = false, length = 64)
	@XmlElement(name = "TaskSchedule")
	public String getSchedule() {
		return schedule;
	}

	/**
	 * Sets the name of the task schedule of this task result.
	 * 
	 * @param schedule The name of the task schedule
	 */
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	/**
	 * Returns the date when the task was launched.
	 * 
	 * @return The date when the task was launched
	 */
	@Column(unique = false, nullable = false)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getLaunched() {
		return launched == null ? null : new Date(launched.getTime());
	}

	/**
	 * Sets the date when the task was launched.
	 * 
	 * @param launched The date when this task was launched
	 */
	public void setLaunched(Date launched) {
		this.launched = launched == null ? null : new Date(launched.getTime());
	}

	/**
	 * Returns the date when this task was completed.
	 * 
	 * @return The date when this task was completed
	 */
	@Column(unique = false, nullable = true)
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getCompleted() {
		return completed == null ? null : new Date(completed.getTime());
	}

	/**
	 * Sets the date when this task was completed.
	 * 
	 * @param completed The date when this task was completed
	 */
	public void setCompleted(Date completed) {
		this.completed = completed == null ? null : new Date(completed.getTime());
	}

	/**
	 * Returns the name of the task definition of this task result.
	 * 
	 * @return The name of the task definition
	 */
	@Column(unique = false, nullable = false, length = 64)
	@XmlElement(name = "TaskDefinition")
	public String getDefinition() {
		return definition;
	}

	/**
	 * Sets the name of the task definition of this task result.
	 * 
	 * @param definition The name of the task definition
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Returns the name of launcher of this task.
	 * 
	 * @return The name of the launcher
	 */
	@Column(unique = false, nullable = false, length = 64)
	@XmlAttribute
	public String getLauncher() {
		return launcher;
	}

	/**
	 * Sets the name of the lanuncher of this task.
	 * 
	 * @param launcher The name of the launcher
	 */
	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	/**
	 * Returns the completion status of this task.
	 * 
	 * @return The completion status of this task
	 */
	@Column(unique = false, nullable = true)
	@XmlAttribute
	public CompletionStatus getCompletionStatus() {
		return completionStatus;
	}

	/**
	 * Sets the completion status of this task.
	 * 
	 * @param completionStatus The completion status of this task
	 */
	public void setCompletionStatus(CompletionStatus completionStatus) {
		this.completionStatus = completionStatus;
	}
}
