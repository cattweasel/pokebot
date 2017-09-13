package net.cattweasel.pokebot.rest;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Profile;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.tools.GeneralException;

@Path("/profile")
@Produces({"application/json"})
public class ProfileResource extends BaseResource {

	@GET
	@Path("list")
	public String list(@QueryParam("start") int start, @QueryParam("page") int page,
			@QueryParam("limit") int limit, @QueryParam("query") String query, @QueryParam("sort") String order,
			@QueryParam("userId") String userId) throws GeneralException {
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.eq(ExtendedAttributes.PROFILE_USER, getContext().getObjectById(User.class, userId)));
		return createListResponse(Profile.class, qo, start, page, limit, query, order,
				Arrays.asList("name"), userId, null).toJSONString();
	}
}
