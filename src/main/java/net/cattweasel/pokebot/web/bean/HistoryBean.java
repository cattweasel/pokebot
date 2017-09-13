package net.cattweasel.pokebot.web.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.cattweasel.pokebot.object.AuditAction;
import net.cattweasel.pokebot.object.AuditEvent;
import net.cattweasel.pokebot.object.ExtendedAttributes;
import net.cattweasel.pokebot.object.Filter;
import net.cattweasel.pokebot.object.QueryOptions;
import net.cattweasel.pokebot.object.User;
import net.cattweasel.pokebot.object.QueryOptions.OrderValue;
import net.cattweasel.pokebot.server.Auditor;
import net.cattweasel.pokebot.tools.GeneralException;
import net.cattweasel.pokebot.tools.Localizer;

public class HistoryBean extends BaseBean {

	public static class HistoryItem {
		
		private String date;
		private String action;
		private String source;
		private String target;
		
		public String getDate() {
			return date;
		}
		
		public void setDate(String date) {
			this.date = date;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}
	}
	
	public List<HistoryItem> getHistoryItems() throws GeneralException {
		List<HistoryItem> items = new ArrayList<HistoryItem>();
		QueryOptions qo = new QueryOptions();
		qo.addFilter(Filter.or(Filter.eq(ExtendedAttributes.AUDIT_EVENT_SOURCE, getLoggedInUser().getName()),
				Filter.eq(ExtendedAttributes.AUDIT_EVENT_TARGET, getLoggedInUser().getName())));
		qo.setOrder(ExtendedAttributes.POKE_OBJECT_CREATED, OrderValue.DESC);
		qo.setLimit(500);
		Iterator<String> it = getContext().search(AuditEvent.class, qo);
		if (it != null) {
			while (it.hasNext()) {
				items.add(createHistoryItem(getContext().getObjectById(AuditEvent.class, it.next())));
			}
		}
		return items;
	}
	
	private HistoryItem createHistoryItem(AuditEvent event) throws GeneralException {
		HistoryItem item = new HistoryItem();
		item.setDate(Localizer.localize(getLoggedInUser(), event.getCreated(), true));
		item.setAction(event.getAction().getName());
		String source = event.getSource();
		if (getLoggedInUser().getName().equals(source)) {
			source = resolveUsername();
		} else if (Auditor.SYSTEM.equals(source)) {
			source = String.format("<i>%s</i>", source);
		}
		item.setSource(source);
		String target = event.getTarget();
		if (getLoggedInUser().getName().equals(target)) {
			target = resolveUsername();
		} else if (Auditor.SYSTEM.equals(target)) {
			target = String.format("<i>%s</i>", target);
		} else if (AuditAction.UPDATE_LOCATION.equals(item.getAction())) {
			String[] parts = target.split(":");
			target = String.format("%s.XXXXXX:%s.XXXXXX", parts[0].split("\\.")[0], parts[1].split("\\.")[0]);
		}
		item.setTarget(target);
		return item;
	}
	
	private String resolveUsername() throws GeneralException {
		User user = getLoggedInUser();
		return String.format("<u>%s</u>", user.getUsername() == null
				? user.getName() : user.getUsername());
	}
}
