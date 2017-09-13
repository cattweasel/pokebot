package net.cattweasel.pokebot.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.TaskExecutor;
import net.cattweasel.pokebot.object.Attributes;
import net.cattweasel.pokebot.object.TaskResult;
import net.cattweasel.pokebot.object.TaskSchedule;
import net.cattweasel.pokebot.tools.Util;

public class UpdateRefreshTask implements TaskExecutor {

	private static final String ARG_UPDATE_URL = "updateUrl";
	
	boolean running = true;
	
	@Override
	public void execute(PokeContext context, TaskSchedule schedule, TaskResult result,
			Attributes<String, Object> attributes) throws Exception {
		String line;
		StringBuilder sb = new StringBuilder();
		URL url = new URL(attributes.getString(ARG_UPDATE_URL));
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
		
		System.out.println(sb.toString()); // TODO
		
	}

	@Override
	public boolean terminate() {
		running = false;
		return true;
	}
}
