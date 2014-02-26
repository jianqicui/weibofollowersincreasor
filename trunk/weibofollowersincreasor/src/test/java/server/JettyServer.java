package server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public final class JettyServer {

	public static void main(String[] args) throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setDefaultsDescriptor("src/test/java/server/webdefault.xml");
		webAppContext.setResourceBase("src/main/webapp");
		webAppContext.setContextPath("/weibofollowersincreasor/");
		webAppContext.setDescriptor("src/main/webapp/WEB-INF/web.xml");

		Server server = new Server(8080);
		server.setHandler(webAppContext);

		server.start();
	}

}
