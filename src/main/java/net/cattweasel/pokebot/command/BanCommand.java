package net.cattweasel.pokebot.command;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.CapabilityManager;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class BanCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(BanCommand.class);
	
	public BanCommand() {
		super("/ban", "Sperrt einen Benutzer dauerhaft");
	}

	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			String userId = args.length == 0 ? null : args[0];
			if (userId == null) {
				sendMessage(sender, chat, Localizer.localize(resolveUser(context, user), "cmd_missing_user_id"));
			} else {
				Capability cap = context.getObjectByName(Capability.class, Capability.SYSTEM_ADMINISTRATOR);
				if (!CapabilityManager.hasCapability(resolveUser(context, user), cap)) {
					sendMessage(sender, chat, Localizer.localize(resolveUser(context, user), "cmd_not_authorized"));
				} else {
					net.cattweasel.pokebot.object.User usr = context.getObjectByName(
							net.cattweasel.pokebot.object.User.class, userId);
					if (usr == null) {
						sendMessage(sender, chat, Localizer.localize(resolveUser(context, user), "cmd_user_id_not_found"));
					} else {
						usr.setBanned(true);
						context.saveObject(usr);
						Auditor auditor = new Auditor(context);
						auditor.log(Util.otos(user.getId()), AuditAction.BAN_USER, usr.getName());
						context.commitTransaction();
						sendMessage(sender, chat, Localizer.localize(resolveUser(context, user), "cmd_ban_success_message"));
					}
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing ban command: " + ex.getMessage(), ex);
			sendErrorMessage(sender, chat, resolveUser(context, user), ex);
		} finally {
			if (context != null) {
				try {
					PokeFactory.releaseContext(context);
				} catch (GeneralException ex) {
					LOG.error("Error releasing context: " + ex.getMessage(), ex);
				}
			}
		}
	}
}
