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
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import javax.websocket.DeploymentException;

import org.reflections.Reflections;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.RTMMessageHandler;
import com.instavector.slackbot_command.ISlackBotCommand;
import com.instavector.slackmessage.SlackMessage;
import com.instavector.slackmessage.SlackMessageFactory;


/**
 * A simple Slack Bot that implements a few commands.
 */
public class EnigmaBot implements ISlackBotCommand {

	private static final String API_PROPERTIES_FILE = ".api-token";

	private static final String COMMANDS_PACKAGE = "com.instavector.slackbot_command";

	private boolean initComplete = false;

	private String apiToken = null;

	private Slack slack = null;

	private RTMClient rtmClient = null;

	private ArrayList<ISlackBotCommand> slackCommands = null;

	private String botUserId = null;


	// Attributes for listing Slack Bot commands
	private static final String LIST_CMD_NAME = "list";

	private static final String LIST_CMD_DESCRIPTION = "list supported commands";

	private static final String LIST_CMD_PATTERN = "[lL]ist.*";

	private static EnigmaBot instance = null;

	public static EnigmaBot getInstance() {
		if (null == instance) {
			instance = new EnigmaBot();
		}
		return instance;
	}

	private EnigmaBot() {
		if (!loadProperties()) {
			return;
		}

		if (!loadCommands()) {
			return;
		}

		initComplete = true;
	}

	// Load parameters from Java properties file
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

	// Load supported commands from internal package - allows adding commands without requring bot class
	// to be dependent on specific commands
	private boolean loadCommands() {

		Reflections reflections = new Reflections(COMMANDS_PACKAGE);
		slackCommands = new ArrayList<ISlackBotCommand>();

		Set<Class<? extends ISlackBotCommand>> cmdClasses = reflections.getSubTypesOf(ISlackBotCommand.class);
		for (Class c: cmdClasses) {
			ISlackBotCommand cmd;
			try {
				cmd = (ISlackBotCommand) c.newInstance();
				slackCommands.add(cmd);
			} catch (InstantiationException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return false;
			}
		}

		slackCommands.add(this);

		return true;
	}

	boolean isInitComplete() {
		return initComplete;
	}

	// Start up the bot
	public boolean start() {

		if (false == initComplete) {
			System.err.println("ERROR: couldn't initialize bot, not starting");
			return false;
		}

		try {
			slack = new Slack();
			rtmClient = slack.rtm(apiToken);

			rtmClient.addMessageHandler(new RTMMessageHandler() {
				@Override
				public void handle(String messageContents) {
					System.out.println(" -> MSG: " + messageContents);
					SlackMessage msg = SlackMessageFactory.CreateSlackMessageObject(messageContents);
					if (null == msg) {
						System.err.println("ERROR: couldn't deserialize Slack message");
						return;
					}

					// Presence change message appears to be bot coming/going; collect user ID to avoid
					// reacting to our own responses
					if (SlackMessage.MSG_TYPE_PRESENCE_CHANGE.equals(msg.getType())) {
						botUserId = msg.getUser();
						return;
					}

					// Only examine actual user messages, not hello, presence change, etc.
					if (!SlackMessage.MSG_TYPE_MESSAGE.equals(msg.getType())) {
						return;
					}

					// Try to match message text against supported commands
					for (ISlackBotCommand c: slackCommands) {
						String text = msg.getText();
						if (null == text) {
							return;
						}
						// Ignore our own messages, in case a response contains text that matches a command pattern
						if (botUserId.equals(msg.getUser())) {
							return;
						}
						if (text.matches(c.getCommandPattern())) {
							if (false == c.executeCommand(slack, apiToken, msg)) {
								System.err.println("ERROR: failed to execute \"" + c.getCommandName() + "\" command");
							}
							break;
						}
					}
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

		System.out.println(this.getClass().getSimpleName() + " is running");

		return true;

	}

	/*
	 * Methods implementing "list" command - list supported commands
	 *
	 * (non-Javadoc)
	 * @see com.instavector.slackbot_command.ISlackBotCommand#getCommandName()
	 */
	@Override
	public String getCommandName() {
		return LIST_CMD_NAME;
	}

	@Override
	public String getCommandPattern() {
		return LIST_CMD_PATTERN;
	}

	@Override
	public String getCommandDescription() {
		return LIST_CMD_DESCRIPTION;
	}

	@Override
	public boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message) {
		StringBuilder sb = new StringBuilder("Here are the commands I support:\n");

		slackCommands.forEach(c -> {
			sb.append(c.getCommandName() + " - " + c.getCommandDescription() + "\n");
		});
		ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), sb.toString().trim());
		return true;
	}


	// Stop the bot
	public void stop() {
		try {
			System.out.println("Stopping...");
			rtmClient.close();  // calls disconnect()
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
