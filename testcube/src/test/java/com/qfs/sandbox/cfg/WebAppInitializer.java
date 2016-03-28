/*
 * (C) Quartet FS 2013-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.quartetfs.biz.pivot.spring.ActivePivotRemotingServicesConfig;
import com.quartetfs.biz.xmla.servlet.impl.XmlaServlet;
import com.quartetfs.biz.xmla.servlet.pivot.impl.ActivePivotXmlaServlet;

/**
 *
 * Initializer of the Web Application.
 * <p>
 * When bootstrapped by a servlet-3.0 application container, the Spring
 * Framework will automatically create an instance of this class and call its
 * startup callback method.
 * <p>
 * The content of this class replaces the old web.xml file in previous versions
 * of the servlet specification.
 *
 * @author Quartet FS
 *
 */
public class WebAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		// ActivePivot XMLA Servlet
		ActivePivotXmlaServlet xmlaServlet = new ActivePivotXmlaServlet();
		Dynamic xmla = servletContext.addServlet("xmla", xmlaServlet);
		xmla.setInitParameter(XmlaServlet.BINARY_ENABLED, "true");
		xmla.setInitParameter(XmlaServlet.COMPRESSION_ENABLED, "true");
		xmla.addMapping("/xmla");
		xmla.setLoadOnStartup(2);

		configureServletConfig(servletContext);

		// Spring HTTP Web Services
		ActivePivotRemotingServicesConfig.addServletsTo(servletContext);

		//Dynamic springDataService = servletContext.addServlet("springDataService", new HttpRequestHandlerServlet());
		//springDataService.addMapping("/remoting/Data");

		// Apache CXF Web Services
		Dynamic cxf = servletContext.addServlet("CXFServlet", new CXFServlet());
		cxf.addMapping("/webservices/*");
		cxf.setLoadOnStartup(3);

		// Spring Security Filter
		FilterRegistration.Dynamic springSecurity = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy());
		springSecurity.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");// isMatchAfter set to false
		// to make WS authentication work through handshake process.
	}

	public static void configureServletConfig(ServletContext servletContext) {
		// Spring Context Bootstrapping
		servletContext.setInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, AnnotationConfigWebApplicationContext.class.getName());
		//servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, SandboxConfig.class.getName());
		servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, DynSandboxConfig.class.getName());
		servletContext.addListener(new ContextLoaderListener());
	}
}
