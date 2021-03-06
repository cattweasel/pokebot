package net.cattweasel.pokebot.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.Gym;
import net.cattweasel.pokebot.object.Pokemon;
import net.cattweasel.pokebot.object.Spawn;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.object.Team;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

/**
 * This task is used to get the latest updates from gopmap.
 * 
 * @author Benjamin Wesp
 *
 */
public class GomapRefreshTask implements TaskExecutor {
	
	private static final String ARG_GOMAP_URL = "gomapUrl";
	private static final String ARG_POKEMONS = "pokemons";
	private static final String ARG_GYMS = "gyms";
	private static final String ARG_GYM_ID = "gym_id";
	private static final String ARG_IS_IN_BATTLE = "is_in_battle";
	private static final String ARG_LATITUDE = "latitude";
	private static final String ARG_LONGITUDE = "longitude";
	private static final String ARG_NAME = "name";
	private static final String ARG_RAID_CP = "rcp";
	private static final String ARG_RAID_END = "re";
	private static final String ARG_RAID_LEVEL = "lvl";
	private static final String ARG_RAID_START = "rs";
	private static final String ARG_RAID_POKEMON_ID = "rpid";
	private static final String ARG_GYM_POINTS = "gym_points";
	private static final String ARG_TEAM_ID = "team_id";
	private static final String ARG_TIME_OCUPPIED = "time_ocuppied";
	private static final String ARG_DISAPPEAR_TIME = "disappear_time";
	private static final String ARG_EID = "eid";
	private static final String ARG_POKEMON_ID = "pokemon_id";
	
	private Map<Integer, Pokemon> pokemonCache = new HashMap<Integer, Pokemon>();
	private Map<Integer, Team> teamCache = new HashMap<Integer, Team>();
	
	private static final Logger LOG = Logger.getLogger(GomapRefreshTask.class);
	
	private boolean running = true;
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		LOG.debug("Starting GomapRefreshTask..");
		LOG.debug("Fetching data from gomap.eu..");
		String line;
		StringBuilder sb = new StringBuilder();
		URL url = new URL(attributes.getString(ARG_GOMAP_URL));
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
			while (running && Util.isNotNullOrEmpty((line = br.readLine()))) {
				sb.append(line);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(sb.toString());
		if (running) {
			LOG.debug("Processing spawns..");
			processSpawns(context, (JSONArray) json.get(ARG_POKEMONS));
			LOG.debug("Committing spawns..");
			context.commitTransaction();
		}
		if (running) {
			LOG.debug("Processing gyms..");
			processGyms(context, (JSONArray) json.get(ARG_GYMS));
			LOG.debug("Committing gyms..");
			context.commitTransaction();
		}
		LOG.debug("Finished GomapRefreshTask!");
	}
	
	@Override
	public boolean terminate() {
		running = false;
		return true;
	}
	
	private void processSpawns(PokeContext context, JSONArray spawns) {
		if (spawns != null && !spawns.isEmpty()) {
			int counter = 1;
			for (int i=0; i<spawns.size(); i++) {
				if (running) {
					try {
						processSpawn(context, (JSONObject) spawns.get(i));
						if (counter % 500 == 0) {
							context.commitTransaction();
						}
						counter++;
					} catch (GeneralException ex) {
						LOG.error("Error processing spawn: " + spawns.get(i), ex);
					}
				}
			}
		}
	}
	
	private void processSpawn(PokeContext context, JSONObject spawn) throws GeneralException {
		String name = Util.otos(spawn.get(ARG_EID));
		Date disappearTime = new Date(Util.atol(Util.otos(spawn.get(ARG_DISAPPEAR_TIME))) * 1000L);
		if (disappearTime.getTime() > new Date().getTime()) {
			if (context.getObjectByName(Spawn.class, name) == null) {
				Spawn result = new Spawn();
				result.setDisappearTime(disappearTime);
				result.setName(name);
				result.setLatitude(Util.atod(Util.otos(spawn.get(ARG_LATITUDE))));
				result.setLongitude(Util.atod(Util.otos(spawn.get(ARG_LONGITUDE))));
				result.setPokemon(resolvePokemon(context, Util.otoi(spawn.get(ARG_POKEMON_ID))));
				context.saveObject(result);
			}
		}
	}
	
	private void processGyms(PokeContext context, JSONArray gyms) {
		if (gyms != null && !gyms.isEmpty()) {
			for (int i=0; i<gyms.size(); i++) {
				if (running) {
					try {
						processGym(context, (JSONObject) gyms.get(i));
					} catch (GeneralException ex) {
						LOG.error("Error processing gym: " + gyms.get(i), ex);
					}
				}
			}
		}
	}
	
	private void processGym(PokeContext context, JSONObject gym) throws GeneralException {
		Gym result = new Gym();
		result.setName(Util.otos(gym.get(ARG_GYM_ID)));
		result.setInBatte(Util.otob(gym.get(ARG_IS_IN_BATTLE)));
		result.setLatitude(Util.atod(Util.otos(gym.get(ARG_LATITUDE))));
		result.setLongitude(Util.atod(Util.otos(gym.get(ARG_LONGITUDE))));
		result.setDisplayName(Util.otos(gym.get(ARG_NAME)));
		result.setRaidCp(Util.otoi(gym.get(ARG_RAID_CP)));
		if (gym.get(ARG_RAID_END) != null) {
			result.setRaidEnd(new Date(Util.atol(Util.otos(gym.get(ARG_RAID_END))) * 1000L));
		}
		result.setRaidLevel(Util.otoi(gym.get(ARG_RAID_LEVEL)));
		result.setRaidPokemon(resolvePokemon(context, Util.otoi(gym.get(ARG_RAID_POKEMON_ID))));
		if (gym.get(ARG_RAID_START) != null) {
			result.setRaidStart(new Date(Util.atol(Util.otos(gym.get(ARG_RAID_START))) * 1000L));
		}
		result.setScore(Util.otoi(gym.get(ARG_GYM_POINTS)));
		result.setTeam(resolveTeam(context, Util.otoi(gym.get(ARG_TEAM_ID))));
		result.setTimeOcuppied(Util.atol(Util.otos(gym.get(ARG_TIME_OCUPPIED))));
		saveGym(context, result);
	}
	
	private Pokemon resolvePokemon(PokeContext context, Integer id) throws GeneralException {
		Pokemon result = null;
		if (id != null && id != 0) {
			result = pokemonCache.get(id);
			if (result == null) {
				result = context.getUniqueObject(Pokemon.class, Filter.eq(
						ExtendedAttributes.POKEMON_POKEMON_ID, id));
				if (result != null) {
					pokemonCache.put(id, result);
				}
			}
		}
		return result;
	}
	
	private Team resolveTeam(PokeContext context, Integer id) throws GeneralException {
		Team result = teamCache.get(id);
		if (result == null) {
			result = context.getUniqueObject(Team.class, Filter.eq(
					ExtendedAttributes.TEAM_TEAM_ID, id));
			if (result != null) {
				teamCache.put(id, result);
			}
		}
		return result;
	}
	
	private void saveGym(PokeContext context, Gym gym) throws GeneralException {
		Gym existing = context.getObjectByName(Gym.class, gym.getName());
		if (existing != null) {
			gym = mergeGym(existing, gym);
		}
		context.saveObject(gym);
	}
	
	private Gym mergeGym(Gym existing, Gym current) {
		Gym result = existing;
		result.setInBatte(current.getInBatte());
		result.setRaidCp(current.getRaidCp());
		result.setRaidEnd(current.getRaidEnd());
		result.setRaidLevel(current.getRaidLevel());
		result.setRaidPokemon(current.getRaidPokemon());
		result.setRaidStart(current.getRaidStart());
		result.setScore(current.getScore());
		result.setTeam(current.getTeam());
		result.setTimeOcuppied(current.getTimeOcuppied());
		return result;
	}
}
