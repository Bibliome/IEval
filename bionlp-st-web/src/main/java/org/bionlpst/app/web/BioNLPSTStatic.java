package org.bionlpst.app.web;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class BioNLPSTStatic {
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/{path:.*\\.html}")
	public static Object html(@PathParam("path") String path) {
		return loadResource(path);
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{path:.*\\.xml}")
	public static Object xml(@PathParam("path") String path) {
		return loadResource(path);
	}
	
	@GET
	@Produces("text/javascript")
	@Path("/{path:.*\\.js}")
	public static Object javascript(@PathParam("path") String path) {
		return loadResource(path);
	}
	
	@GET
	@Produces("text/css")
	@Path("/{path:.*\\.css}")
	public static Object css(@PathParam("path") String path) {
		return loadResource(path);
	}
	
	@GET
	@Produces("image/png")
	@Path("/{path:.*\\.png}")
	public static Object png(@PathParam("path") String path) {
		return loadResource(path);
	}
	
	@GET
	@Produces("image/gif")
	@Path("/{path:.*\\.gif}")
	public static Object gif(@PathParam("path") String path) {
		return loadResource(path);
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/")
	public static Object startPage() {
		return loadResource("index.html");
	}
	
	private static final String RESOURCE_PREFIX = "org/bionlpst/app/web/resources/";
	
	private static InputStream loadResource(ClassLoader cl, String path) {
		String resourceName = RESOURCE_PREFIX + path;
		return cl.getResourceAsStream(resourceName);
	}
	
	private static Object loadResource(String path) {
		ClassLoader cl = BioNLPSTStatic.class.getClassLoader();
		InputStream result = loadResource(cl, path);
		if (result == null) {
			return Response.status(404).build();
		}
		return result;
	}
}
