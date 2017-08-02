package net.cattweasel.pokebot.object;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.ReferenceAdapter;

/**
 * POJO implementation for a rule.
 * 
 * @author Benjamin Wesp
 *
 */
@Entity
@Table(name = "db_rule")
@XmlRootElement(name = "Rule")
public class Rule extends PokeObject {

	private static final long serialVersionUID = -79914587143413243L;

	@XmlEnum(String.class)
	public enum Type {

		@XmlEnumValue("generic") GENERIC
	}

	private Type type;
	private String language;
	private String source;
	private List<Rule> referencedRules;

	/**
	 * Returns the language of this rule.
	 * 
	 * @return The language of this rule
	 */
	@Column(unique = false, nullable = false, length = 32)
	@XmlAttribute
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the language for this rule.
	 * 
	 * @param language The language for this rule
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Returns the source of this rule.
	 * 
	 * @return The source of this rule
	 */
	@Column(unique = false, nullable = false, columnDefinition = "TEXT")
	@XmlElement(name = "Source")
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source for this rule.
	 * 
	 * @param source The source for this rule
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Returns the references rules of this rule.
	 * 
	 * @return A list of referenced rules
	 */
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "db_rule_reference", joinColumns = {
			@JoinColumn(name = "rule_id", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "reference_id", nullable = false, updatable = false) })
	@XmlElementWrapper(name = "ReferencedRules")
	@XmlElement(name = "Reference")
	@XmlJavaTypeAdapter(ReferenceAdapter.class)
	public List<Rule> getReferencedRules() {
		return referencedRules;
	}

	/**
	 * Set the references rules for this rule.
	 * 
	 * @param referencedRules A list of referenced rules
	 */
	public void setReferencedRules(List<Rule> referencedRules) {
		this.referencedRules = referencedRules;
	}

	/**
	 * Returns the type of this rule.
	 * 
	 * @return The type of this rule
	 */
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public Type getType() {
		return type;
	}

	/**
	 * Sets the type for this rule
	 * 
	 * @param type The type for this rule
	 */
	public void setType(Type type) {
		this.type = type;
	}
}
