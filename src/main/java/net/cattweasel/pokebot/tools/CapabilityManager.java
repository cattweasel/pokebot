package net.cattweasel.pokebot.tools;

import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.object.User;

public class CapabilityManager {
	
	public static boolean hasCapability(User user, Capability cap) {
		boolean result = false;
		if (user.getCapabilities() != null) {
			for (Capability c : user.getCapabilities()) {
				if (Capability.SYSTEM_ADMINISTRATOR.equals(c.getName())
						|| cap.getName().equals(c.getName())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
