/**
 * EnigmaBot - simple Slack Bot example
 *
 * @author Dan Weekley <weekley@pobox.com>
 */
package com.instavector.slackbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.websocket.DeploymentException;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.RTMMessageHandler;


/**
 * A simple Slack Bot that implements a few commands.
 */
public class EnigmaBot {

	private static final String API_PROPERTIES_FILE = ".api-token";

	private boolean initComplete = false;

	private String apiToken = null;

	private RTMClient rtmClient = null;


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
			apiToken = props.getProperty("apiToken");
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

	public boolean start() {

		try {
			rtmClient = new Slack().rtm(apiToken);

			rtmClient.addMessageHandler(new RTMMessageHandler() {

				@Override
				public void handle(String message) {
					System.out.println("Got message: " + message);

					// TODO: convert string to JSON
					// TODO: examine message type
					// TODO: process 'type:message' and look up command
				}
			});

			rtmClient.connect();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (DeploymentException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	public void test() {
	}


	public void stop() {
		try {
			rtmClient.close();  // calls disconnect()
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		System.out.println("OK");

		EnigmaBot bot = new EnigmaBot();
		if (false == bot.start()) {
			System.err.println("ERROR: couldn't start bot");
		}

		for (int i = 0; i < 60; i ++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO: some stuff

		bot.stop();

	}
}
