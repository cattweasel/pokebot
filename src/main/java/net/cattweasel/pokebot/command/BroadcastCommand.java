package net.cattweasel.pokebot.command;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.BotSession;
import net.cattweasel.pokebot.object.Capability;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.server.Environment;
import net.cattweasel.pokebot.server.TelegramBot;
import net.cattweasel.pokebot.tools.CapabilityManager;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;
import net.cattweasel.pokebot.tools.Util;

public class BroadcastCommand extends AbstractCommand {

	private static final Logger LOG = Logger.getLogger(BroadcastCommand.class);
	
	public BroadcastCommand() {
		super("broadcast", "Sendet eine Broadcast-Nachricht");
	}
	
	@Override
	public void execute(AbsSender sender, User user, Chat chat, String[] args) {
		PokeContext context = null;
		String msg = resolveMessage(args);
		try {
			context = PokeFactory.createContext(getClass().getSimpleName());
			net.cattweasel.pokebot.object.User usr = resolveUser(context, user);
			if (Util.isNullOrEmpty(msg)) {
				sendMessage(sender, chat, Localizer.localize(usr, "cmd_broadcast_missing_message"));
			} else {
				Capability cap = context.getObjectByName(Capability.class, Capability.BROADCAST_ADMINISTRATOR);
				if (!CapabilityManager.hasCapability(usr, cap)) {
					sendMessage(sender, chat, Localizer.localize(usr, "cmd_broadcast_not_authorized"));
				} else {
					int count = sendBroadcast(context, sender, msg, chat);
					sendMessage(sender, chat, String.format("%s: %s",
							Localizer.localize(usr, "cmd_broadcast_success_message"), Util.separateNumber(count)));
					Auditor auditor = new Auditor(context);
					auditor.log(usr.getName(), AuditAction.SEND_BROADCAST, msg);
					context.commitTransaction();
				}
			}
		} catch (GeneralException ex) {
			LOG.error("Error executing broadcast command: " + ex.getMessage(), ex);
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
	
	@SuppressWarnings("deprecation")
	private int sendBroadcast(PokeContext context, AbsSender sender, String msg, Chat chat) throws GeneralException {
		int count = 0;
		Iterator<String> it = context.search(BotSession.class);
		if (it != null) {
			while (it.hasNext()) {
				BotSession session = context.getObjectById(BotSession.class, it.next());
				if (session.getChatId() != chat.getId()) {
					TelegramBot bot = Environment.getEnvironment().getTelegramBot();
					GetChat getChat = new GetChat();
					getChat.setChatId(session.getChatId());
					try {
						Chat c = bot.getChat(getChat);
						sendMessage(sender, c, String.format("BROADCAST: %s", msg));
						count++;
					} catch (TelegramApiException ex) {
						LOG.error("Error sending broadcast: " + ex.getMessage(), ex);
					}
				}
			}
		}
		return count;
	}

	private String resolveMessage(String[] args) {
		StringBuilder sb = new StringBuilder();
		if (args != null && args.length > 0) {
			for (int i=0; i<args.length; i++) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(args[i]);
			}
		}
		return sb.toString();
	}
}
