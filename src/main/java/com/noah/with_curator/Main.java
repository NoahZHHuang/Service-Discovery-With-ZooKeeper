package com.noah.with_curator;

import java.util.Collection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.curator.x.discovery.ServiceInstance;

public class Main {
	
	public static void main(String [] args) throws Exception{
		
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		
		JettyServer server_1 = new JettyServer("Jetty_1", 7777);
		JettyServer server_2 = new JettyServer("Jetty_2", 8888);
		server_1.start();
		server_2.start();
		
		Collection<ServiceInstance<GreetService>> instances = serviceHelper.queryGreetServiceInstances(Constant.GREET_SERVICE_NAME);
		instances.stream().forEach(instance->{
			System.out.println("id:"+instance.getId());
			System.out.println("name:"+instance.getName());
			System.out.println("port:"+instance.getPort());
		});		
		
		System.out.println("\n\n\nRandomly select the service provider for 10 times...");
		for(int i = 0; i<10; i++){
			ServiceInstance<GreetService> instance = serviceHelper.getGreetServiceInstanceViaProvider(Constant.GREET_SERVICE_NAME);
			//System.out.println("id:"+instance.getId());
			//System.out.println("name:"+instance.getName());
			//System.out.println("port:"+instance.getPort());
			String ip = instance.getId().equals("Jetty_1") || instance.getId().equals("Jetty_2") ? "127.0.0.1" : "";  
			Client client = ClientBuilder.newClient();
			System.out.println(client.target("http://"+ ip +":"+instance.getPort()+"/greet").request().get(String.class));
		}
	}
	
	
}
