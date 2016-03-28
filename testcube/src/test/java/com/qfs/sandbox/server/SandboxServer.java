/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.server;


import com.qfs.pivot.websocket.impl.StreamingEndPoint;
import com.qfs.sandbox.cfg.WebAppInitializer;
import com.quartetfs.biz.pivot.spring.ActivePivotRemotingServicesConfig;
import com.quartetfs.biz.xmla.servlet.impl.XmlaServlet;
import com.quartetfs.biz.xmla.servlet.pivot.impl.ActivePivotXmlaServlet;
import com.quartetfs.fwk.QuartetRuntimeException;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ClassInheritanceHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.*;
import javax.websocket.DeploymentException;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>JettyServer</b>
 *
 * Launches a server on port 9090<br/>
 * For client testing, point client to:<br/>
 * <code>http://localhost:9090/xmla</code><br/>
 *
 * <p>
 * The actual configuration of the ActivePivot Sandbox
 * web application is contained in the WEB-INF/web.xml resource file.
 * <p>
 * The Sandbox application is pre-configured to run in distributed
 * mode, all you have to do is launch the Jetty Server several times
 * with each time a different listening port.
 *
 * @author Quartet Financial Systems
 *
 */
public class SandboxServer {

	/** Jetty server default port (9090) */
	public static final int DEFAULT_PORT = 9090;

	/**
	 * Configure and launch the standalone server.
	 * @param args only one optional argument is supported: the server port
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {

		int port = DEFAULT_PORT;
		if(args != null && args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}

		final Server server = createServer(port);

		// Launch the server
		server.start();
		server.join();
	}


	public static Server createServer(int port) {

        final WebAppContext root = new WebAppContext();
        root.setConfigurations(new Configuration[] { new JettyAnnotationConfiguration() });
        root.setContextPath("/");
        root.setParentLoaderPriority(true);

		// Enable GZIP compression
        final FilterHolder gzipFilter = new FilterHolder(org.eclipse.jetty.servlets.GzipFilter.class);
		gzipFilter.setInitParameter("mimeTypes", "text/xml,application/x-java-serialized-object");
		root.addFilter(gzipFilter, "/*", EnumSet.of(DispatcherType.REQUEST));

		// Create server and configure it
		final Server server = new Server(port);
		server.setHandler(root);	
		
		// Initialize javax.websocket layer
		final ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(root);

        // Add WebSocket endpoint to javax.websocket layer
        try {
			wscontainer.addEndpoint(StreamingEndPoint.class);
		} catch (DeploymentException ex) {
			throw new QuartetRuntimeException(ex);
		}

		return server;
	}


	/**
	 *
	 * When the Jetty servlet-3.0 annotation parser is used, it only
	 * scans the jar files in the classpath. This small override will
	 * allow Jetty to also see the Sandbox web application initializer
	 * in the classpath of the IDE (Eclipse for instance).
	 *
	 * @author Quartet FS
	 *
	 */
	public static class JettyAnnotationConfiguration extends AnnotationConfiguration {

		@Override
		public void preConfigure(WebAppContext context) throws Exception {
			ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
			ConcurrentHashMap<String, ConcurrentHashSet<String>> map = new ClassInheritanceMap();
			set.add(WebAppInitializerTest.class.getName());
			map.put(WebApplicationInitializer.class.getName(), set);
			context.setAttribute(CLASS_INHERITANCE_MAP, map);
			_classInheritanceHandler = new ClassInheritanceHandler(map);
		}

	}

	public static class WebAppInitializerTest extends WebAppInitializer  {
		public WebAppInitializerTest() {
		}

		@Override
		public void onStartup(ServletContext servletContext) throws ServletException {


			// ActivePivot XMLA Servlet
			ActivePivotXmlaServlet xmlaServlet = new ActivePivotXmlaServlet();
			ServletRegistration.Dynamic xmla = servletContext.addServlet("xmla", xmlaServlet);
			xmla.setInitParameter(XmlaServlet.BINARY_ENABLED, "true");
			xmla.setInitParameter(XmlaServlet.COMPRESSION_ENABLED, "true");
			xmla.addMapping("/xmla");
			xmla.setLoadOnStartup(2);

			// Spring Context Bootstrapping
			WebAppInitializer.configureServletConfig(servletContext);

			// Spring HTTP Web Services
			ActivePivotRemotingServicesConfig.addServletsTo(servletContext);

			//ServletRegistration.Dynamic springDataService = servletContext.addServlet("springDataService", new HttpRequestHandlerServlet());
			//springDataService.addMapping("/remoting/Data");

			// Apache CXF Web Services
			ServletRegistration.Dynamic cxf = servletContext.addServlet("CXFServlet", new CXFServlet());
			cxf.addMapping("/webservices/*");
			cxf.setLoadOnStartup(3);

			// ADDED here for Access-Control-Allow-Origin because dependant of container (JETTY here)
			// http://stackoverflow.com/questions/16037558/how-to-add-access-control-allow-origin-to-jetty-server
			FilterRegistration.Dynamic crossOrigin = servletContext.addFilter("crossOrigin", new CrossOriginFilter());
			crossOrigin.setInitParameter("allowedOrigins", "*");
			crossOrigin.setInitParameter("allowedMethods", "GET,POST,HEAD,OPTIONS");
			crossOrigin.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,Authorization");
			crossOrigin.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");

			// Spring Security Filter
			FilterRegistration.Dynamic springSecurity = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy());
			springSecurity.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");// isMatchAfter set to false
			// to make WS authentication work through handshake process.

		}
	}


}