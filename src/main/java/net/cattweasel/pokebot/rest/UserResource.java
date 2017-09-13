package net.cattweasel.pokebot.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.cattweasel.pokebot.api.Terminator;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Pokemon;
import net.cattweasel.pokebot.object.Profile;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.QueryOptions.OrderValue;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

@Path("/user")
@Produces({"application/json"})
public class UserResource extends BaseResource {

	private static final String ARG_POKEMON_ID = "pokemonId";
	private static final String ARG_DISPLAY_NAME = "displayName";
	private static final String ARG_ENABLED = "enabled";
	private static final String ARG_RANGE = "range";
	private static final String ARG_CREATE_PROFILE = "createProfile";
	private static final String ARG_LOAD_PROFILE = "loadProfile";
	private static final String ARG_SAVE_PROFILE = "saveProfile";
	private static final String ARG_DELETE_PROFILE = "deleteProfile";
	private static final String ARG_USER_ID = "userId";
	
	@GET
	@Path("list")
	public String list(@QueryParam("start") int start, @QueryParam("page") int page, @QueryParam("limit") int limit,
			@QueryParam("query") String query, @QueryParam("sort") String order, @QueryParam("userId") String userId) {
		return createListResponse(User.class, null, start, page, limit, query, order, Arrays.asList("name", "username"),
				userId, Arrays.asList(Capability.SYSTEM_ADMINISTRATOR)).toJSONString();
	}
	
	@POST
	@Path("settings/save")
	@SuppressWarnings("unchecked")
	public String saveSettings(MultivaluedMap<String, String> params) {
		JSONObject result = new JSONObject();
		try {
			User user = getContext().getObjectById(User.class, params.get(ARG_USER_ID).get(0));
			if (user != null) {
				Attributes<String, Object> settings = user.getSettings();
				settings = settings == null ? new Attributes<String, Object>() : settings;
				if (params.keySet().contains(ARG_CREATE_PROFILE) && !params.get(ARG_CREATE_PROFILE).isEmpty()
						&& Util.isNotNullOrEmpty(params.get(ARG_CREATE_PROFILE).get(0))) {
					createProfile(user, params.get(ARG_CREATE_PROFILE).get(0));
				} else if (params.keySet().contains(ARG_LOAD_PROFILE) && !params.get(ARG_LOAD_PROFILE).isEmpty()
						&& Util.isNotNullOrEmpty(params.get(ARG_LOAD_PROFILE).get(0))) {
					loadProfile(user, params.get(ARG_LOAD_PROFILE).get(0));
				} else if (params.keySet().contains(ARG_SAVE_PROFILE) && !params.get(ARG_SAVE_PROFILE).isEmpty()
						&& Util.isNotNullOrEmpty(params.get(ARG_SAVE_PROFILE).get(0))) {
					saveProfile(user, params.get(ARG_SAVE_PROFILE).get(0));
				} else if (params.keySet().contains(ARG_DELETE_PROFILE) && !params.get(ARG_DELETE_PROFILE).isEmpty()
						&& Util.isNotNullOrEmpty(params.get(ARG_DELETE_PROFILE).get(0))) {
					deleteProfile(user, params.get(ARG_DELETE_PROFILE).get(0));
				} else {
					for (Entry<String, List<String>> entry : params.entrySet()) {
						settings.put(entry.getKey(), entry.getValue().get(0));
					}
					Auditor auditor = new Auditor(getContext());
					auditor.log(user.getName(), AuditAction.UPDATE_SETTINGS, user.getName());
				}
				user.setSettings(settings);
				getContext().saveObject(user);
				getContext().commitTransaction();
			}
			result.put(ARG_SUCCESS, true);
		} catch (GeneralException ex) {
			result.put(ARG_SUCCESS, false);
			result.put(ARG_MESSAGE, ex.getMessage());
		}
		return result.toJSONString();
	}
	
	private void createProfile(User user, String profileName) throws GeneralException {
		Profile existing = getContext().getUniqueObject(Profile.class, Filter.and(
				Filter.eq(ExtendedAttributes.PROFILE_USER, user),
				Filter.eq(ExtendedAttributes.PROFILE_DISPLAY_NAME, profileName)));
		if (existing != null) {
			throw new GeneralException(Localizer.localize(user, "profile_already_exists"));
		} else {
			Profile profile = new Profile();
			profile.setDisplayName(profileName);
			profile.setName(Util.uuid());
			profile.setUser(user);
			getContext().saveObject(profile);
			Auditor auditor = new Auditor(getContext());
			auditor.log(user.getName(), AuditAction.CREATE_PROFILE, profile.getDisplayName());
			getContext().commitTransaction();
		}
	}

	private void loadProfile(User user, String profileId) throws GeneralException {
		Profile profile = getContext().getObjectById(Profile.class, profileId);
		if (profile != null) {
			user.setSettings(mergeSettings(user.getSettings(), profile.getSettings()));
			getContext().saveObject(user);
			Auditor auditor = new Auditor(getContext());
			auditor.log(user.getName(), AuditAction.LOAD_PROFILE, profile.getDisplayName());
			getContext().commitTransaction();
		}
	}

	private Attributes<String, Object> mergeSettings(Attributes<String, Object> master, Attributes<String, Object> slave) {
		Attributes<String, Object> result = master == null ? new Attributes<String, Object>() : master;
		if (slave != null) {
			for (Entry<String, Object> entry : slave.entrySet()) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private void saveProfile(User user, String profileId) throws GeneralException {
		Profile profile = getContext().getObjectById(Profile.class, profileId);
		profile.setSettings(user.getSettings());
		getContext().saveObject(profile);
		Auditor auditor = new Auditor(getContext());
		auditor.log(user.getName(), AuditAction.SAVE_PROFILE, profile.getDisplayName());
		getContext().commitTransaction();
	}

	private void deleteProfile(User user, String profileId) throws GeneralException {
		Profile profile = getContext().getObjectById(Profile.class, profileId);
		Auditor auditor = new Auditor(getContext());
		auditor.log(user.getName(), AuditAction.DELETE_PROFILE, profile.getDisplayName());
		Terminator terminator = new Terminator(getContext());
		terminator.deleteObject(profile);
		getContext().commitTransaction();
	}

	@GET
	@Path("settings/pokemon/list")
	@SuppressWarnings("unchecked")
	public String listPokemon(@QueryParam("start") int start, @QueryParam("page") int page, @QueryParam("limit") int limit,
			@QueryParam("query") String query, @QueryParam("sort") String order, @QueryParam("userId") String userId) {
		JSONObject result = new JSONObject();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.eq(ExtendedAttributes.POKEMON_ENABLED, true));
		qo.setOrder(ExtendedAttributes.POKEMON_POKEMON_ID, OrderValue.ASC);
		JSONObject pokemon = createListResponse(Pokemon.class, qo, start, page,
				500, query, order, Arrays.asList("name"), userId, null);
		if (Util.otob(pokemon.get(ARG_SUCCESS))) {
			try {
				User user = getContext().getObjectById(User.class, userId);
				Attributes<String, Object> settings = user == null || user.getSettings() == null ? null : user.getSettings();
				JSONArray pokemons = new JSONArray();
				JSONArray objects = (JSONArray) pokemon.get(ARG_OBJECTS);
				if (objects != null && !objects.isEmpty()) {
					for (Object object : objects) {
						pokemons.add(createPokemonSetting((JSONObject) object, user, settings));
					}
				}
				result.put(ARG_SUCCESS, true);
				result.put(ARG_TOTAL, pokemons.size());
				result.put(ARG_OBJECTS, pokemons);
			} catch (GeneralException ex) {
				result.put(ARG_SUCCESS, false);
				result.put(ARG_MESSAGE, ex.getMessage());
			}
		} else {
			result.put(ARG_SUCCESS, false);
			result.put(ARG_MESSAGE, pokemon.get(ARG_MESSAGE));
		}
		return result.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject createPokemonSetting(JSONObject pokemon, User user, Attributes<String, Object> settings) {
		JSONObject result = new JSONObject();
		result.put(ARG_POKEMON_ID, pokemon.get(ARG_POKEMON_ID));
		result.put(ARG_DISPLAY_NAME, Localizer.localize(user, String.format("pokemon_%s", pokemon.get(ARG_POKEMON_ID))));
		result.put(ARG_ENABLED, isEnabled(Util.otoi(pokemon.get(ARG_POKEMON_ID)), settings));
		result.put(ARG_RANGE, getRange(Util.otoi(pokemon.get(ARG_POKEMON_ID)), settings));
		return result;
	}
	
	private boolean isEnabled(Integer pokemonId, Attributes<String, Object> settings) {
		boolean result = false;
		String key = String.format("%s-enabled", pokemonId);
		if (settings != null && settings.get(key) != null) {
			result = Util.otob(settings.get(key));
		}
		return result;
	}
	
	private Integer getRange(Integer pokemonId, Attributes<String, Object> settings) {
		int result = 3000;
		String key = String.format("%s-range", pokemonId);
		if (settings != null && settings.get(key) != null) {
			result = Util.otoi(settings.get(key));
		}
		return result;
	}
	
	@POST
	@Path("settings/savePokemon")
	@SuppressWarnings("unchecked")
	public String savePokemon(MultivaluedMap<String, String> params) throws GeneralException {
		JSONObject result = new JSONObject();
		if (params != null && !params.isEmpty()) {
			User user = getContext().getObjectById(User.class, params.get(ARG_USER_ID).get(0));
			if (user != null) {
				Attributes<String, Object> settings = user.getSettings() == null
						? new Attributes<String, Object>() : user.getSettings();
				for (Entry<String, List<String>> entry : params.entrySet()) {
					settings.put(entry.getKey(), entry.getValue().get(0));
				}
				Auditor auditor = new Auditor(getContext());
				auditor.log(user.getName(), AuditAction.UPDATE_SETTINGS, user.getName());
				user.setSettings(settings);
				getContext().saveObject(user);
				getContext().commitTransaction();
			}
		}
		result.put(ARG_SUCCESS, true);
		return result.toJSONString();
	}
}
