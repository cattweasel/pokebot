package net.cattweasel.pokebot.rest;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.object.User;

@Path("/user")
@Produces({"application/json"})
public class UserResource extends BaseResource {

	@GET
	@Path("list")
	public String list(@QueryParam("start") int start, @QueryParam("page") int page, @QueryParam("limit") int limit,
			@QueryParam("query") String query, @QueryParam("sort") String order, @QueryParam("userId") String userId) {
		return createListResponse(User.class, start, page, limit, query, order, Arrays.asList("name", "username"),
				userId, Arrays.asList(Capability.SYSTEM_ADMINISTRATOR)).toJSONString();
	}
}
