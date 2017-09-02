package net.cattweasel.pokebot.object;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.cattweasel.pokebot.tools.xml.WrappedReferenceAdapter;

@Entity
@Table(name = "db_audit_event")
@XmlRootElement(name = "AuditEvent")
public class AuditEvent extends PokeObject {

	private static final long serialVersionUID = -6668680489582597865L;

	private AuditAction action;
	private String source;
	private String target;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action", unique = false, nullable = false)
	@XmlElement(name = "Action")
	@XmlJavaTypeAdapter(WrappedReferenceAdapter.class)
	public AuditAction getAction() {
		return action;
	}
	
	public void setAction(AuditAction action) {
		this.action = action;
	}
	
	@Column(unique = false, nullable = false)
	@XmlAttribute
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	@Column(unique = false, nullable = false)
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
}
