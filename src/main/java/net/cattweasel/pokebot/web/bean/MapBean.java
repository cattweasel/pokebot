package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class MapBean extends BaseBean {

	public class Location {
		
		private Double x;
		private Double y;
		
		public Double getX() {
			return x;
		}
		
		public void setX(Double x) {
			this.x = x;
		}
		
		public Double getY() {
			return y;
		}
		
		public void setY(Double y) {
			this.y = y;
		}
	}
	
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	
	public List<Location> getUserLocations() throws GeneralException {
		List<Location> result = new ArrayList<Location>();
		if (getLoggedInUser() != null && getLoggedInUser().hasCapability(Capability.SYSTEM_ADMINISTRATOR)) {
			Iterator<String> it = getContext().search(BotSession.class);
			if (it != null) {
				while (it.hasNext()) {
					BotSession session = getContext().getObjectById(BotSession.class, it.next());
					if (session.get(LATITUDE) != null && session.get(LONGITUDE) != null) {
						Location l = new Location();
						l.setX(Util.atod(Util.otos(session.get(LATITUDE))));
						l.setY(Util.atod(Util.otos(session.get(LONGITUDE))));
						result.add(l);
					}
				}
			}
		}
		return result;
	}
}
