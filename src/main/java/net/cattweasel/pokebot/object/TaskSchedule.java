package net.cattweasel.pokebot.object;

import java.util.Date;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.api.Resolver;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.xml.DateAdapter;

/**
 * POJO implementation for a task schedule.
 * 
 * @author Benjamin Wesp
 *
 */
@XmlRootElement(name = "TaskSchedule")
public class TaskSchedule extends PokeObject {

	private static final long serialVersionUID = 1L;

	@XmlEnum(String.class)
	public static enum State {

		@XmlEnumValue("Suspended") SUSPENDED,
		@XmlEnumValue("Error") ERROR,
		@XmlEnumValue("Executing") EXECUTING,
		@XmlEnumValue("Terminated") TERMINATED
	}

	private String definition;
	private String result;
	private String cronExpression;
	private String launcher;
	private Boolean runNow;
	private Boolean runOnce;
	private Date lastExecution;
	private Date nextExecution;
	private State state;
	private String description;

	/**
	 * Returns the description of this task schedule.
	 * 
	 * @return The description of this task schedule
	 */
	@Column(unique = false, nullable = true, length = 255)
	@XmlElement(name = "Description")
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description for this task schedule
	 * 
	 * @param description The description for this task schedule
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the name of the task definition of this task schedule.
	 * 
	 * @return The name of the task definition
	 */
	@XmlElement(name = "TaskDefinition")
	public String getDefinition() {
		return definition;
	}

	/**
	 * Sets the name of the task definition for this task schedule.
	 * 
	 * @param definition The name of the task definition
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Helper method to resolve the task definition by its name.
	 * 
	 * @param resolver The resolver to be used
	 * @return The task definition if found - null otherwise
	 * @throws GeneralException In case of the object can not be looked up
	 */
	public TaskDefinition getDefinition(Resolver resolver) throws GeneralException {
		return resolver.getObjectByName(TaskDefinition.class, getDefinition());
	}

	/**
	 * Returns the CRON expression of this task schedule.
	 * 
	 * @return The CRON expression of this task schedule
	 */
	@XmlElement(name = "CronExpression")
	public String getCronExpression() {
		return cronExpression;
	}

	/**
	 * Sets the CRON expression for this task schedule.
	 * 
	 * @param cronExpression The CRON Expression to be used
	 */
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	/**
	 * Returns the name of the task result of this task schedule.
	 * 
	 * @return The name of the task result
	 */
	@XmlElement(name = "TaskResult")
	public String getResult() {
		return result;
	}

	/**
	 * Helper method to resolve the task result by its name.
	 * 
	 * @param resolver The resolver to be used
	 * @return The task result if found - null otherwise
	 * @throws GeneralException In case if the object can not be looked up
	 */
	public TaskResult getResult(Resolver resolver) throws GeneralException {
		return resolver.getObject(TaskResult.class, getResult());
	}

	/**
	 * Sets the name of the task resul for this task schedule.
	 * 
	 * @param result The name of the task result
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Returns the launcher of this task schedule.
	 * 
	 * @return The name of the launcher
	 */
	@XmlElement(name = "Launcher")
	public String getLauncher() {
		return launcher;
	}

	/**
	 * Sets the name of the launcher of this task schedule.
	 * 
	 * @param launcher The name of the launcher
	 */
	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	/**
	 * Determine if this schedule should run immediately or not.
	 * 
	 * @return True if it should run immediately - false otherwise
	 */
	@XmlAttribute
	public Boolean isRunNow() {
		return runNow;
	}

	/**
	 * Determine if this schedule should run immediately or not.
	 * 
	 * @param runNow True if it should run immediately - false otherwise
	 */
	public void setRunNow(Boolean runNow) {
		this.runNow = runNow;
	}

	/**
	 * Determine if this schedule should run only one time.
	 * 
	 * @return True if it should run one time - false otherwise
	 */
	@XmlAttribute
	public Boolean isRunOnce() {
		return runOnce;
	}

	/**
	 * Determine if this schedule should run only one time.
	 * 
	 * @param runOnce True if it should run only one time - false otherwise
	 */
	public void setRunOnce(Boolean runOnce) {
		this.runOnce = runOnce;
	}

	/**
	 * Returns the last execution date of this task schedule.
	 * 
	 * @return The last execution date of this task schedule
	 */
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getLastExecution() {
		return lastExecution == null ? null : new Date(lastExecution.getTime());
	}

	/**
	 * Sets the last execution date of this task schedule.
	 * 
	 * @param lastExecution The last execution date of this schedule
	 */
	public void setLastExecution(Date lastExecution) {
		this.lastExecution = lastExecution == null ? null : new Date(lastExecution.getTime());
	}

	/**
	 * Retun the next execution date of this taks schedule
	 * 
	 * @return The next execution date of this task schedule
	 */
	@XmlAttribute
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getNextExecution() {
		return nextExecution == null ? null : new Date(nextExecution.getTime());
	}

	/**
	 * Sets the next execution date of this task schedule.
	 * 
	 * @param nextExecution The next execution date of this schedule
	 */
	public void setNextExecution(Date nextExecution) {
		this.nextExecution = nextExecution == null ? null : new Date(nextExecution.getTime());
	}

	/**
	 * Returns the current state of this task schedule.
	 * 
	 * @return The current state of this task schedule
	 */
	@XmlAttribute
	public State getState() {
		return state;
	}

	/**
	 * Sets the current state of this task schedule.
	 * 
	 * @param state The current state of this task schedule
	 */
	public void setState(State state) {
		this.state = state;
	}
}
