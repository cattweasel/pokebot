package net.cattweasel.pokebot.api;

import java.util.List;
import java.util.Map;

import net.cattweasel.pokebot.object.Rule;
import net.cattweasel.pokebot.tools.GeneralException;

/**
 * Interface of an object that is able to execute rules and scripts.
 * 
 * @author Benjamin Wesp
 *
 */
public abstract interface RuleRunner {
	
	/**
	 * Run a rule and return the result or null if there is no result.
	 * 
	 * @param rule The rule to be executed
	 * @param args Possible parameters for the rule
	 * @return The result or null if there is no result
	 * @throws GeneralException In case of any error
	 */
	Object runRule(Rule rule, Map<String, Object> args) throws GeneralException;

	/**
	 * Run a rule and return the result or null if there is no result.
	 * 
	 * @param rule The rule to be executed
	 * @param args Possible parameters for the rule
	 * @param refs Possible referenced rules to be evaluated
	 * @return The result or null if there is no result
	 * @throws GeneralException In case of any error
	 */
	Object runRule(Rule rule, Map<String, Object> args, List<Rule> refs) throws GeneralException;
}
