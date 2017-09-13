package net.cattweasel.pokebot.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to specify options to a database query.
 * 
 * @author Benjamin Wesp
 *
 */
@XmlRootElement(name = "QueryOptions")
public class QueryOptions implements Serializable {

	private static final long serialVersionUID = -3063687392704653578L;
	
	public static enum OrderValue {
		
		ASC, DESC;
	}
	
	private List<Filter> filters;
	private int limit;
	private int firstResult;
	private String orderProperty;
	private OrderValue orderValue;

	public QueryOptions() {
		filters = new ArrayList<Filter>();
	}

	/**
	 * Adds a filter to this query options.
	 * 
	 * @param filter The filter to be added
	 */
	public void addFilter(Filter filter) {
		filters.add(filter);
	}

	/**
	 * Returns all filters of this query options.
	 * 
	 * @return A list of all filters
	 */
	@XmlElementWrapper(name = "Filters")
	@XmlElement(name = "Filter")
	public List<Filter> getFilters() {
		return filters;
	}

	/**
	 * Returns the limit of this query options.
	 * 
	 * @return The limit of this query options
	 */
	@XmlAttribute
	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the limit for this query options.
	 * 
	 * @param limit The limit for this query options
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Returns the first result of this query options.
	 * 
	 * @return The first result of this query options
	 */
	@XmlAttribute
	public int getFirstResult() {
		return firstResult;
	}

	/**
	 * Sets the first result for this query options.
	 * 
	 * @param firstResult The first result for this query options
	 */
	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	/**
	 * Sets the order for this quey options.
	 * 
	 * @param orderProperty The property to be ordered
	 * @param orderValue The direction for this order (ASC or DESC)
	 */
	public void setOrder(String orderProperty, OrderValue orderValue) {
		this.orderProperty = orderProperty;
		this.orderValue = orderValue;
	}

	/**
	 * Returns the order property of this query options.
	 * 
	 * @return The order property of this query options
	 */
	@XmlAttribute
	public String getOrderProperty() {
		return orderProperty;
	}

	/**
	 * Returns the order value of this query options.
	 * 
	 * @return The order value o this query options (ASC or DESC)
	 */
	@XmlAttribute
	public OrderValue getOrderValue() {
		return orderValue;
	}

	/**
	 * Overriding the default toString method for a nicer view.
	 * 
	 * @return The formated string to display
	 */
	@Override
	public String toString() {
		return String.format("[limit: %s, firstResult: %s, orderProperty: %s"
				+ ", orderValue: %s, filters: %s]",
				limit, firstResult, orderProperty, orderValue, filters);
	}

	/**
	 * Creates a copy of this quey options object.
	 * 
	 * @return A copy of this query options
	 */
	public QueryOptions copy() {
		QueryOptions qo = new QueryOptions();
		for (Filter filter : getFilters()) {
			qo.addFilter(filter);
		}
		qo.setLimit(limit);
		qo.setFirstResult(firstResult);
		qo.setOrder(orderProperty, orderValue);
		return qo;
	}
}
