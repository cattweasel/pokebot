package net.cattweasel.pokebot.rest;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.cattweasel.pokebot.object.AuditEvent;
import net.cattweasel.pokebot.object.Capability;

@Path("/audit")
@Produces({"application/json"})
public class AuditResource extends BaseResource {

	@GET
	@Path("event/list")
	public String list(@QueryParam("start") int start, @QueryParam("page") int page, @QueryParam("limit") int limit,
			@QueryParam("query") String query, @QueryParam("sort") String order, @QueryParam("userId") String userId) {
		return createListResponse(AuditEvent.class, start, page, limit, query, order, Arrays.asList("source", "action", "target"),
				userId, Arrays.asList(Capability.SYSTEM_ADMINISTRATOR)).toJSONString();
	}
}
