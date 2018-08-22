package com.noah.with_curator;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.gson.Gson;

@Path("/")
public class GreetService {
	
	private static final Gson gson = new Gson();

	private Integer port;
	private String ip;
	
	public GreetService() {
	}

	public GreetService(Integer port, String ip) {
		this.port = port;
		this.ip = ip;
	}
	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Path("/greet")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@JsonIgnore
	public Response getResult() {
		Map<String, String> result = new HashMap<>();
		result.put("greet", "Hello from " + ip + ":" + port);
		return Response.ok(gson.toJson(result)).build();
	}

}
