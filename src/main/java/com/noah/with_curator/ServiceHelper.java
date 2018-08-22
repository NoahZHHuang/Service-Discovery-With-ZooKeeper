package com.noah.with_curator;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

public class ServiceHelper {

	private static ServiceHelper helper = null;
	
	private CuratorFramework client;
	
	private ServiceDiscovery<GreetService> greetServiceDiscovery;
	
	private Map<String, ServiceProvider<GreetService>> greetServiceProviderMap = new ConcurrentHashMap<>();

	public ServiceHelper(CuratorFramework client, String basePath) {
	
		this.client = client;
		this.client.start();
		this.greetServiceDiscovery = ServiceDiscoveryBuilder.builder(GreetService.class)
				.client(client)
				.serializer(new JsonInstanceSerializer<>(GreetService.class))
				.basePath(basePath)
				.build();
		
	}
	
	synchronized public static ServiceHelper getHelper() throws Exception{
		if(helper==null){
			helper = new ServiceHelper(CuratorHelper.getCuratorClient(), Constant.BASE_PATH);
			helper.start();
		}
		return helper;
	}
	
	public void updateGreetService(ServiceInstance<GreetService> instance) throws Exception {
		this.greetServiceDiscovery.updateService(instance);
	}
	
	public void registerGreetService(ServiceInstance<GreetService> instance) throws Exception{
		this.greetServiceDiscovery.registerService(instance);
	}
	
	public void unregisterGreetService(ServiceInstance<GreetService> instance) throws Exception{
		this.greetServiceDiscovery.unregisterService(instance);
	} 
	
	public Collection<ServiceInstance<GreetService>> queryGreetServiceInstances(String name) throws Exception{
		return this.greetServiceDiscovery.queryForInstances(name);
	}
	
	public ServiceInstance<GreetService> queryGreetServiceInstance(String name, String id) throws Exception{
		return this.greetServiceDiscovery.queryForInstance(name, id);
	}
	
	public ServiceInstance<GreetService> getGreetServiceInstanceViaProvider(String name) throws Exception{
		ServiceProvider<GreetService> provider;
		if(!greetServiceProviderMap.containsKey(name)){
			provider = this.greetServiceDiscovery.serviceProviderBuilder()
					.serviceName(name)
					.providerStrategy(new RandomStrategy<GreetService>())
					.build();
			greetServiceProviderMap.put(name, provider);
			provider.start();
		}
		provider = greetServiceProviderMap.get(name);
		return provider.getInstance(); 
	}
	
	public void start() throws Exception{
		this.greetServiceDiscovery.start();
	}
	
	public void close() throws Exception{
		this.greetServiceProviderMap.values().stream().forEach(provider->{
			try {
				provider.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		this.greetServiceDiscovery.close();
	}

}
