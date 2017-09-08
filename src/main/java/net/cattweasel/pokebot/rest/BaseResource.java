package net.cattweasel.pokebot.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.PokeObject;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class BaseResource {

	private PokeContext context;
	
	private static final Logger LOG = Logger.getLogger(BaseResource.class);
	
	protected PokeContext getContext() {
		if (context == null) {
			try {
				context = PokeFactory.getCurrentContext();
			} catch (GeneralException ex) {
				LOG.error(ex);
			}
		}
		return context;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends PokeObject> JSONObject createListResponse(Class<T> clazz, int start, int page, int limit,
			String query, String order, List<String> searchFields, String userId, List<String> requiredCapabilities) {
		JSONObject result = new JSONObject();
		if (requiredCapabilities != null && !accessGranted(userId, requiredCapabilities)) {
			result.put("success", false);
			result.put("message", "User not authorized or permitted!");
		} else {
			QueryOptions qo = new QueryOptions();
			try {
				int total = getContext().countObjects(clazz, qo);
				qo.setFirstResult(start);
				qo.setLimit(limit);
				if (query != null && !query.trim().isEmpty()
						&& searchFields != null && !searchFields.isEmpty()) {
					addQuery(qo, query, searchFields);
				}
				if (Util.isNotNullOrEmpty(order)) {
					try {
						JSONArray json = (JSONArray) new JSONParser().parse(order);
						if (json != null && !json.isEmpty()) {
							qo.setOrder(Util.otos(((JSONObject) json.get(0)).get("property")),
									Util.otos(((JSONObject) json.get(0)).get("direction")));
						}
					} catch (ParseException ex) {
						LOG.error(ex);
					}
				}
				JSONArray objects = new JSONArray();
				for (PokeObject object : getContext().getObjects(clazz, qo)) {
					objects.add(object.toJson());
				}
				result.put("success", true);
				result.put("total", total);
				result.put("objects", objects);
			} catch (GeneralException ex) {
				result.put("success", false);
				result.put("message", ex.getMessage());
			}
		}
		return result;
	}
	
	private boolean accessGranted(String userId, List<String> requiredCapabilities) {
		boolean result = false;
		try {
			User user = getContext().getObjectById(User.class, userId);
			if (user != null) {
				boolean check = true;
				for (String cap : requiredCapabilities) {
					if (!user.hasCapability(cap)) {
						check = false;
					}
				}
				result = check;
			}
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
		return result;
	}

	private void addQuery(QueryOptions qo, String query, List<String> fields) {
		List<Filter> filters = new ArrayList<Filter>();
		for (String field : fields) {
			filters.add(Filter.like(field, query));
		}
		Filter filter = null;
		for (Filter f : filters) {
			filter = filter == null ? f : Filter.or(filter, f);
		}
		qo.addFilter(filter);
	}
}
