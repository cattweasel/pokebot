package net.cattweasel.pokebot.object;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A filter is an abstract class that is used to express boolean logical expressions.
 * 
 * @author Benjamin Wesp
 *
 */
public class Filter implements Serializable {

	private static final long serialVersionUID = -5929777557926122597L;

	public enum Mode {
        
        EQ, NE, LT, GT, LIKE, NOTNULL, IN, OR, ISNULL, AND, NOT;
    }
	
	public enum MatchMode {
		
		START, END, ANYWHERE
	}
    
    private Mode mode;
    private MatchMode matchMode;
    private String property;
    private Object value;
    private List<Filter> filters;
    
    private Filter(Mode mode, String property) {
        this.mode = mode;
        this.property = property;
    }
    
    private Filter(Mode mode, String property, String value) {
        this.mode = mode;
        this.property = property;
        this.value = value;
    }
    
    private Filter(Mode mode, String property, Long value) {
        this.mode = mode;
        this.property = property;
        this.value = value;
    }
    
    private Filter(Mode mode, String property, Object value) {
    		this(mode, property, value, MatchMode.ANYWHERE);
    }
    
    private Filter(Mode mode, String property, Object value, MatchMode matchMode) {
        this.mode = mode;
        this.property = property;
        this.value = value;
        this.matchMode = matchMode;
    }
    
    private Filter(Mode mode, Filter ... filters) {
    		this(mode, Arrays.asList(filters));
    }
    
    private Filter(Mode mode, List<Filter> filters) {
    		this.mode = mode;
    		this.filters = filters;
    }
    
    /**
     * Creates a new OR filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter eq(String property, String value) {
        return new Filter(Mode.EQ, property, value);
    }
    
    /**
     * Creates a new OR filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter eq(String property, Object value) {
    		return new Filter(Mode.EQ, property, value);
    }
    
    /**
     * Creates a new NOT-EQUALS filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter ne(String property, String value) {
        return new Filter(Mode.NE, property, value);
    }
    
    /**
     * Creates a new NOT-EQUALS filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter ne(String property, Object value) {
        return new Filter(Mode.NE, property, value);
    }
    
    /**
     * Creates a new LOWER-THAN filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter lt(String property, Object value)  {
    		return new Filter(Mode.LT, property, value);
    }
    
    /**
     * Creates a new GREATER-THAN filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter gt(String property, Object value)  {
    		return new Filter(Mode.GT, property, value);
    }
    
    /**
     * Creates a new LIKE filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter like(String property, String value) {
    		return new Filter(Mode.LIKE, property, value);
    }
    
    /**
     * Creates a new LIKE filter.
     * 
     * @param property The property for this filter
     * @param value The value for this filter
     * @param mode The match mode for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter like(String property, String value, MatchMode mode) {
    		return new Filter(Mode.LIKE, property, value, mode);
    }
    
    /**
     * Creates a new NOT-NULL filter.
     * 
     * @param property The property for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter notnull(String property) {
    		return new Filter(Mode.NOTNULL, property);
    }
    
    /**
     * Creates a new IS-NULL filter.
     * 
     * @param property The property for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter isnull(String property) {
    		return new Filter(Mode.ISNULL, property);
    }
    
    /**
     * Creates a new IN filter.
     * 
     * @param property The property for this filter
     * @param values The list of values for this filter
     * @return The compiled filter ready for usage
     */
    public static Filter in(String property, Collection<?> values) {
        return new Filter(Mode.IN, property, values);
    }
    
    /**
     * Creates a new OR filter.
     * 
     * @param filters A list of filters to connect
     * @return The compiled filter ready for usage
     */
    public static Filter or(Filter ... filters) {
    		return new Filter(Mode.OR, filters);
    }
    
    /**
     * Creates a new OR filter.
     * 
     * @param filters A list of filters to connect
     * @return The compiled filter ready for usage
     */
    public static Filter or(List<Filter> filters) {
    		return new Filter(Mode.OR, filters);
    }
    
    /**
     * Creates a new AND filter.
     * 
     * @param filters A list of filters to connect
     * @return The compiled filter ready for usage
     */
    public static Filter and(Filter ... filters) {
    		return new Filter(Mode.AND, filters);
    }
    
    /**
     * Creates a new AND filter.
     * 
     * @param filters A list of filters to connect
     * @return The compiled filter ready for usage
     */
    public static Filter and(List<Filter> filters) {
    		return new Filter(Mode.AND, filters);
    }
    
    /**
     * Created a new NOT filter.
     * 
     * @param filter The filter to negiate
     * @return The compiled filter ready for usage
     */
    public static Filter not(Filter filter) {
    		return new Filter(Mode.NOT, filter);
    }
    
    /**
     * Returns the mode of this filter.
     * 
     * @return The mode of this filter
     */
    public Mode getMode() {
        return mode;
    }
    
    /**
     * Returns the property of this filter.
     * 
     * @return The property of this filter
     */
    public String getProperty() {
        return property;
    }
    
    /**
     * Returns the value of this filter.
     * 
     * @return The value of this filter
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Returns the match mode of this filter.
     * 
     * @return The match mode of this filter
     */
    public MatchMode getMatchMode() {
    		return matchMode;
    }
    
    /**
     * Returns a list of child filter.
     * 
     * @return A list of child filters if available
     */
    public List<Filter> getFilters() {
    		return filters;
    }
    
    /**
	 * Overriding the default toString method for a nicer view.
	 * 
	 * @return The formated string to display
	 */
    @Override
    public String toString() {
	    	return String.format("[mode: %s, property: %s, value: %s, filters: %s]",
	    			mode.name(), property, value, filters);
    }
}
