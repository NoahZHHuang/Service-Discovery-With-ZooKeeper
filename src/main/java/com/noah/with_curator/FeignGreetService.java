package com.noah.with_curator;

import java.util.Map;

import feign.Headers;
import feign.RequestLine;

public interface FeignGreetService {

	 @Headers({"Content-Type: application/json"})
	 @RequestLine("GET /greet")
	 Map<String, String> get();
	
}
