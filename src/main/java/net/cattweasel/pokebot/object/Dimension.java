package net.cattweasel.pokebot.object;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.cattweasel.pokebot.tools.xml.AbstractXmlObject;

@XmlRootElement(name = "Dimension")
public class Dimension extends AbstractXmlObject{

	private final Double lat1;
	private final Double lon1;
	private final Double lat2;
	private final Double lon2;
	private final Double lat3;
	private final Double lon3;
	private final Double lat4;
	private final Double lon4;
	
	public Dimension(Double lat1, Double lon1, Double lat2, Double lon2, Double lat3, Double lon3, Double lat4, Double lon4) {
		this.lat1 = lat1;
		this.lon1 = lon1;
		this.lat2 = lat2;
		this.lon2 = lon2;
		this.lat3 = lat3;
		this.lon3 = lon3;
		this.lat4 = lat4;
		this.lon4 = lon4;
	}
	
	@XmlAttribute
	public Double getLat1() {
		return lat1;
	}
	
	@XmlAttribute
	public Double getLon1() {
		return lon1;
	}
	
	@XmlAttribute
	public Double getLat2() {
		return lat2;
	}
	
	@XmlAttribute
	public Double getLon2() {
		return lon2;
	}
	
	@XmlAttribute
	public Double getLat3() {
		return lat3;
	}
	
	@XmlAttribute
	public Double getLon3() {
		return lon3;
	}
	
	@XmlAttribute
	public Double getLat4() {
		return lat4;
	}
	
	@XmlAttribute
	public Double getLon4() {
		return lon4;
	}
	
	@Override
	public String toString() {
		return String.format("Dimension[lat1 = %s, lon1 = %s, lat2 = %s, lon2 = %s, lat3 = %s"
				+ ", lon3 = %s, lat4 = %s, lon4 = %s]", lat1, lon1, lat2, lon2, lat3, lon3, lat4, lon4);
	}
}
