package net.cattweasel.pokebot.rest;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.UserNotification;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

@Path("/session")
@Produces({"application/json"})
public class SessionResource extends BaseResource {

	private static final Logger LOG = Logger.getLogger(SessionResource.class);
	
	@GET
	@Path("list")
	@SuppressWarnings("unchecked")
	public String list(@QueryParam("start") int start, @QueryParam("page") int page, @QueryParam("limit") int limit,
			@QueryParam("query") String query, @QueryParam("sort") String order, @QueryParam("userId") String userId) {
		JSONObject result = createListResponse(BotSession.class, start, page, limit, query, order,
				Arrays.asList("name"), userId, Arrays.asList(Capability.SYSTEM_ADMINISTRATOR));
		JSONArray oldObjects = (JSONArray) result.get("objects");
		JSONArray newObjects = new JSONArray();
		if (oldObjects != null) {
			for (int i=0; i<oldObjects.size(); i++) {
				JSONObject session = (JSONObject) oldObjects.get(i);
				session.put("notifications", calculateNotifications(session));
				newObjects.add(session);
			}
			result.put("objects", newObjects);
		}
		return result.toJSONString();
	}

	private int calculateNotifications(JSONObject session) {
		int result = 0;
		QueryOptions qo = new QueryOptions();
		String username = Util.otos(((JSONObject) session.get("user")).get("name"));
		qo.addFilter(Filter.like(ExtendedAttributes.POKE_OBJECT_NAME,
				String.format("%s:", username), Filter.MatchMode.START));
		try {
			result = getContext().countObjects(UserNotification.class, qo);
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
		return result;
	}
}
