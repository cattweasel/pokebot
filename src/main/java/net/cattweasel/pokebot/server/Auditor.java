package net.cattweasel.pokebot.server;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.AuditEvent;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Util;

public class Auditor {
	
	private final PokeContext context;
	
	public Auditor(PokeContext context) {
		this.context = context;
	}

	public void log(String source, String action, String target) throws GeneralException {
		AuditAction a = context.getObjectByName(AuditAction.class, action);
		if (a != null && a.isEnabled()) {
			log(context, source, a, target);
		}
	}

	private void log(PokeContext context, String source,
			AuditAction action, String target) throws GeneralException {
		AuditEvent event = new AuditEvent();
		event.setName(Util.uuid());
		event.setSource(source);
		event.setAction(action);
		event.setTarget(target);
		context.saveObject(event);
	}
}
