package com.noah.with_curator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.curator.x.discovery.ServiceInstance;

import com.netflix.client.ClientFactory;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.RandomRule;

import feign.Feign;
import feign.Request.Options;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.ribbon.LBClient;
import feign.ribbon.LBClientFactory;
import feign.ribbon.RibbonClient;

public class Main {

	public static void main(String[] args) throws Exception {

		ServiceHelper serviceHelper = ServiceHelper.getHelper();

		JettyServer server_1 = new JettyServer("Jetty_1", 7777);
		JettyServer server_2 = new JettyServer("Jetty_2", 8888);
		server_1.start();
		server_2.start();

		ProxyJettyServer proxyServer = new ProxyJettyServer(9999);
		proxyServer.start();

		System.out.println("\n\n\nRegisted instances info...");
		Collection<ServiceInstance<GreetService>> instances = serviceHelper
				.queryGreetServiceInstances(Constant.GREET_SERVICE_NAME);
		instances.stream().forEach(instance -> {
			System.out.println("id:" + instance.getId());
			System.out.println("name:" + instance.getName());
			System.out.println("port:" + instance.getPort());
		});

		System.out.println("\n\n\nGet service from provider randomly for 10 times...");
		for (int i = 0; i < 10; i++) {
			ServiceInstance<GreetService> instance = serviceHelper
					.getGreetServiceInstanceViaProvider(Constant.GREET_SERVICE_NAME);
			String ip = instance.getId().equals("Jetty_1") || instance.getId().equals("Jetty_2") ? "127.0.0.1" : "";
			Client client = ClientBuilder.newClient();
			System.out.println(client.target("http://" + ip + ":" + instance.getPort() + "/greet").request().get(String.class));
		}

		System.out.println("\n\n\nGet service from proxy server for 10 times...");
		for (int i = 0; i < 10; i++) {
			Client client = ClientBuilder.newClient();
			System.out.println(client.target("http://127.0.0.1:9999/" + Constant.GREET_SERVICE_NAME + "/greet").request().get(String.class));
		}
		
		System.out.println("\n\n\nGet service with feign client(hard code to http://127.0.0.1:7777)...");
		FeignGreetService feignGreetService = Feign.builder()
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder())
				.options(new Options(1000, 3500))
				.retryer(new Retryer.Default(5000, 5000, 3))
				.target(FeignGreetService.class, "http://127.0.0.1:7777");
		for(int i = 0 ; i< 10; i++){
			System.out.println(feignGreetService.get());
		}
	
		
		System.out.println("\n\n\nGet service with feign client and ribbon load balancer...");
		System.out.println("the listOfServers in ribbon config is still hard code to \"127.0.0.1:7777,127.0.0.1:8888\"...");
		System.out.println("LB strategy is equally selection");
		ConfigurationManager.loadPropertiesFromResources("load_balance.properties");
		FeignGreetService feignGreetServiceWithRibbon = Feign.builder()
				.client(RibbonClient.create())
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder())
				.options(new Options(1000, 3500))
				.retryer(new Retryer.Default(5000, 5000, 3))
				.target(FeignGreetService.class, "http://GreetService");
		for(int i = 0 ; i< 10; i++){
			System.out.println(feignGreetServiceWithRibbon.get());
		}
		
		
		System.out.println("\n\n\nGet service with feign client and ribbon load balancer...");
		System.out.println("listOfServers in ribbon config is still hard code to \"127.0.0.1:7777,127.0.0.1:8888\"...");
		System.out.println("LB strategy is randomly selection");
		ConfigurationManager.loadPropertiesFromResources("load_balance.properties");
		RibbonClient ribbonClientRandomSelect = RibbonClient.builder().lbClientFactory(new LBClientFactory() {
			@Override
			public LBClient create(String clientName) {
				IClientConfig config = ClientFactory.getNamedConfig(clientName);
				BaseLoadBalancer lb = (BaseLoadBalancer)ClientFactory.getNamedLoadBalancer(clientName);
				lb.setRule(new RandomRule()); //if not defined, it default to RoundRobinRule
                return LBClient.create(lb, config);
			}
		}).build();
		FeignGreetService feignGreetServiceWithRibbonRandomly = Feign.builder()
				.client(ribbonClientRandomSelect)
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder())
				.options(new Options(1000, 3500))
				.retryer(new Retryer.Default(5000, 5000, 3))
				.target(FeignGreetService.class, "http://GreetService");
		for(int i = 0 ; i< 10; i++){
			System.out.println(feignGreetServiceWithRibbonRandomly.get());
		}
		
		
		System.out.println("\n\n\nGet service with feign client and ribbon load balancer...");
		System.out.println("listOfServers in ribbon NOT hard coded, it is auto discovered");
		System.out.println("LB strategy is randomly selection");
		ConfigurationManager.loadPropertiesFromResources("load_balance.properties");
		RibbonClient ribbonClientRandomSelectAndAutoDiscovered = RibbonClient.builder().lbClientFactory(new LBClientFactory() {
			@Override
			public LBClient create(String clientName) {
				IClientConfig config = ClientFactory.getNamedConfig(clientName);
				BaseLoadBalancer lb = (BaseLoadBalancer)ClientFactory.getNamedLoadBalancer(clientName);
				lb.setRule(new RandomRule());
				List<com.netflix.loadbalancer.Server> serverList = new ArrayList<>();
				for (int i = 0; i < 10; i++) {
					ServiceInstance<GreetService> instance;
					try {
						instance = serviceHelper.getGreetServiceInstanceViaProvider(Constant.GREET_SERVICE_NAME);
						String ip = instance.getId().equals("Jetty_1") || instance.getId().equals("Jetty_2") ? "127.0.0.1" : "";
						com.netflix.loadbalancer.Server server = new com.netflix.loadbalancer.Server (ip, instance.getPort());
						serverList.add(server);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				lb.setServersList(serverList);
                return LBClient.create(lb, config);
			}
		}).build();
		FeignGreetService feignGreetServiceWithRibbonRandomlyAndAutoDiscovered = Feign.builder()
				.client(ribbonClientRandomSelectAndAutoDiscovered)
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder())
				.options(new Options(1000, 3500))
				.retryer(new Retryer.Default(5000, 5000, 3))
				.target(FeignGreetService.class, "http://GreetService");
		for(int i = 0 ; i< 10; i++){
			System.out.println(feignGreetServiceWithRibbonRandomlyAndAutoDiscovered.get());
		}
		
		
	}

}
