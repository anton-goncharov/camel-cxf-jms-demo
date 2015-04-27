package com.windmilled.app;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * REST service implementation
 *
 */
public class QueueServiceImpl implements QueueService {
	
	@GET
	@Path("/ping")
	public Response ping(String request) {
		Response response = Response.status(200).entity("Ping").build();
		return response;		
	}

    @GET
    @Path("/{store}")
	public Response getMessage(@PathParam("store") String store) {
		Response response = Response.status(200).entity("").build();
		return response;		
	}

    @POST
    @Path("/{store}")
	public Response uploadMessage(@PathParam("store") String store, String payload) {
        Response response = Response.status(200).entity("").build();
        return response;
	}	

}
