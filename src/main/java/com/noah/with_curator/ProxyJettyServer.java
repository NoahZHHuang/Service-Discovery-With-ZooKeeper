package com.noah.with_curator;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class ProxyJettyServer {

	private Server server;
	private Integer port;

	public ProxyJettyServer(Integer port) {
		this.port = port;
		this.server = new Server(port);
	}

	public void start() throws Exception {

		ResourceConfig config = new ResourceConfig();
		config.register(new ProxyGreetService());
		ServletHolder servletHolder = new ServletHolder(new ServletContainer(config));
		ServletContextHandler contextHandler = new ServletContextHandler(server, "/");
		contextHandler.addServlet(servletHolder, "/"+Constant.GREET_SERVICE_NAME+"/*");
		server.start();

		System.out.println("Proxy Jetty Server: " + port + " Start Up Successfully !!!");
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

}
