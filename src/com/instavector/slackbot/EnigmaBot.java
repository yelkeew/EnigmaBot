package com.instavector.slackbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * A simple Slack Bot that implements a few commands.
 * 
 * @author Dan Weekley <weekley@pobox.com>
 */
public class EnigmaBot {

	private static final String API_PROPERTIES_FILE = ".api-token";
	
	private boolean initComplete = false;
	
	private String apiToken = null;
	
	
	public EnigmaBot() {
		
		if (!loadProperties()) {
			return;
		}
		
		initComplete = true;
		
	}
	
	private boolean loadProperties() {
		try {
			FileInputStream fis = new FileInputStream(new File(API_PROPERTIES_FILE));
			Properties props = new Properties();
			props.load(fis);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: properties file '" + API_PROPERTIES_FILE + "' doesn't exist");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("ERROR: couldn't read properties file '" + API_PROPERTIES_FILE + "'");
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
}
