/*
 * (C) Quartet FS 2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.logging;

import static java.util.logging.Level.INFO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * java.util.logging configuration class with the following features:
 * <ul>
 * <li>Define independently the log levels of the main ActivePivot components
 * <li>Define both a console handler and a file handler
 * <li>Apply the QFS formatter to enrich log records with the current thread
 * and the current user.
 * </ul>
 * <p>
 * To load this class at the JVM startup time, add the following VM option:
 * <br>-Djava.util.logging.config.class=com.qfs.logging.LoggingConfiguration
 *
 * <p>
 * This configuration class is designed to be used programmatically, when
 * deploying in the Jetty server. When deploying in Apache Tomcat, use
 * the logging.properties based configuration.
 *
 * @author Quartet FS
 *
 */
public class LoggingConfiguration {

	public LoggingConfiguration() throws IOException {

		// Configure Levels
		Map<String, Level> levels = new HashMap<>();

		// Composer loggers
		levels.put("com.quartetfs.fwk", INFO);
		levels.put("com.quartetfs.fwk.serialization", INFO);

		// Streaming loggers
		levels.put("com.quartetfs.tech.streaming", INFO);

		// CSV Source loggers
		levels.put("com.qfs.msg.csv", INFO);

		// Direct memory chunk loggers
		levels.put("com.qfs.chunk.direct.impl", INFO);

		// ActivePivot Loggers
		levels.put("com.quartetfs.biz.pivot", INFO);
		levels.put("com.quartetfs.biz.pivot.impl", INFO);

		// Sandbox Application loggers
		levels.put("com.qfs.sandbox", INFO);
		levels.put("com.qfs.sandbox.source", INFO);

		// Apache CXF loggers
		levels.put("org.apache.cxf", INFO);

		// Inject log levels in the log manager
		LogManager.getLogManager().readConfiguration(asStream(levels));

		// Console Handler
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		consoleHandler.setFormatter(new QFSFormatter());

		// File Handler
		Handler fileHandler = new FileHandler("D:\\dev\\tmp\\activepivot5-sandbox.log");
		fileHandler.setLevel(Level.ALL);
		fileHandler.setFormatter(new QFSFormatter());

		Logger root = Logger.getLogger("");
		root.addHandler(consoleHandler);
		root.addHandler(fileHandler);
	}

	/**
	 * Present the log levels as a configuration input stream.
	 *
	 * @param levels the levels of logging per logger
	 * @return input stream
	 */
	public static InputStream asStream(Map<String, Level> levels) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Level> entry : levels.entrySet()) {
			sb.append(entry.getKey()).append(".level=").append(entry.getValue()).append('\n');
		}
		return new ByteArrayInputStream(sb.toString().getBytes());
	}

}