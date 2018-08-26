package com.noah.with_curator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.curator.x.discovery.ServiceInstance;

@Path("/")
public class ProxyGreetService {

	@Path("/greet")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResult() throws Exception {
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		ServiceInstance<GreetService> instance = serviceHelper.getGreetServiceInstanceViaProvider(Constant.GREET_SERVICE_NAME);
		String ip = instance.getId().equals("Jetty_1") || instance.getId().equals("Jetty_2") ? "127.0.0.1" : "";
		Client client = ClientBuilder.newClient();
		String result = client.target("http://"+ ip +":"+instance.getPort()+"/greet").request().get(String.class);
		return Response.ok(result).build();
	}
	
}
