package com.windmilled.app;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * REST service interface
 *
 */
@Path("/")
public interface QueueService {	   
	
	@GET
	@Path("/ping")
	Response ping(String request);
	
	@GET	
	@Path("/{store}")
	Response getMessage(@PathParam("store") String resource);
	
	@POST
	@Path("/{store}")
	Response uploadMessage(@PathParam("store") String resource, String payload);
	
}
