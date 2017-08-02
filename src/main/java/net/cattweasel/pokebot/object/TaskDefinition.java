package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO implementation for a task definition.
 * 
 * @author Benjamin Wesp
 *
 */
@Entity
@Table(name = "db_task_definition")
@XmlRootElement(name = "TaskDefinition")
public class TaskDefinition extends PokeObject {

	private static final long serialVersionUID = -7220834391356710222L;

	@XmlEnum(String.class)
	public static enum ResultAction {

		@XmlEnumValue("Delete") DELETE,
		@XmlEnumValue("Rename") RENAME,
		@XmlEnumValue("Cancel") CANCEL,
		@XmlEnumValue("RenameNew") RENAME_NEW
	}

	private String executor;
	private ResultAction resultAction;
	private Attributes<String, Object> attributes;
	private String description;

	/**
	 * Return the description of this task definition.
	 * 
	 * @return The description of this task definition
	 */
	@Column(unique = false, nullable = true, length = 255)
	@XmlElement(name = "Description")
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description for this task definition
	 * 
	 * @param description The description for this task definition
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the executor of this task definition.
	 * 
	 * @return The executor of this task definition
	 */
	@Column(unique = false, nullable = false, length = 255)
	@XmlAttribute
	public String getExecutor() {
		return executor;
	}

	/**
	 * Sets the executor for this task definition.
	 * 
	 * @param executor The executor for this task definition
	 */
	public void setExecutor(String executor) {
		this.executor = executor;
	}

	/**
	 * Returns the result action of this task definition.
	 * 
	 * @return The result action of this task definition
	 */
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public ResultAction getResultAction() {
		return resultAction;
	}

	/**
	 * Sets the result action for this task definition
	 * 
	 * @param resultAction The result action for this task definition
	 */
	public void setResultAction(ResultAction resultAction) {
		this.resultAction = resultAction;
	}
	
	/**
	 * Returns the attributes of this task definition.
	 * 
	 * @return The attributes of this task definition
	 */
	@Column(unique = false, nullable = true)
	@XmlElement(name = "Attributes")
	public Attributes<String, Object> getAttributes() {
		return attributes;
	}
	
	/**
	 * Sets the attributes for this task definition.
	 * 
	 * @param attributes The attributes for this task definition
	 */
	public void setAttributes(Attributes<String, Object> attributes) {
		this.attributes = attributes;
	}
}
