package net.cattweasel.pokebot.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import net.cattweasel.pokebot.api.PokeContext;
import net.cattweasel.pokebot.api.PokeFactory;
import net.cattweasel.pokebot.tools.GeneralException;

/**
 * Filter for providing a @{link net.cattweasel.pokebot.api.PokeContext} in every thread.
 * 
 * @author Benjamin Wesp
 *
 */
public class PokeContextFilter implements Filter {

	private static final Logger LOG = Logger.getLogger(PokeContextFilter.class);

	public void init(FilterConfig config) throws ServletException {
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		PokeContext context = null;
		try {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpSession httpSession = httpRequest.getSession(true);
			Object o = httpSession.getAttribute("username");
			String username = o != null ? o.toString() : null;
			context = PokeFactory.createContext(username);
			chain.doFilter(request, response);
		} catch (Throwable ex) {
			LOG.error(ex.getMessage(), ex);
			throw new ServletException(ex);
		} finally {
			if (context != null) {
				try {
					PokeFactory.releaseContext(context);
				} catch (GeneralException ex) {
					LOG.warn("Failed releasing PokeContext: " + ex.getMessage(), ex);
				}
			}
		}
	}

	public void destroy() {
	}
}
