package com.noah.with_curator;


import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;


public class JettyServer {
	
	private Server server;
	private GreetService greetService;
	private String id;
	private Integer port;
	
	public JettyServer(String id, Integer port){
		this.id = id;
		this.port = port;
		this.server = new Server(port);
		this.greetService = new GreetService(port, "127.0.0.1");
	}
	
	public void start() throws Exception{
		
		ResourceConfig config = new ResourceConfig();
		config.register(greetService);
		ServletHolder servletHolder = new ServletHolder(new ServletContainer(config));
		ServletContextHandler contextHandler = new ServletContextHandler(server,"/");
		contextHandler.addServlet(servletHolder, "/*");
		server.start();
		
		System.out.println("Jetty Server:" + id + ":" + port + " Start Up Successfully !!!");
		
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		
		ServiceInstance<GreetService> instance = ServiceInstance.<GreetService>builder()
				.id(id)
				.name(Constant.GREET_SERVICE_NAME)
				.port(port)
				.payload(greetService)
				.uriSpec(new UriSpec("{scheme}://{address}:{port}"))
				.build();
		
		serviceHelper.registerGreetService(instance);
		
		System.out.println("Jetty Server:" + id + ":" + port + " Register Successfully !!!");
		
		
		
	}
	
	public void stop() throws Exception{
		if(server!=null){
			server.stop();
		}
	}

}
