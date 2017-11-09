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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.websocket.DeploymentException;

import org.reflections.Reflections;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.users.UsersListRequest;
import com.github.seratch.jslack.api.methods.response.users.UsersListResponse;
import com.github.seratch.jslack.api.model.User;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.RTMMessageHandler;
import com.instavector.slackbot_command.ISlackBotCommand;
import com.instavector.slackmessage.SlackMessage;
import com.instavector.slackmessage.SlackMessageFactory;

/**
 * A simple Slack Bot that implements a few commands.
 */
public class EnigmaBot implements ISlackBotCommand {

	public static final String API_PROPERTIES_FILE = ".api-token";

	private static final String COMMANDS_PACKAGE = "com.instavector.slackbot_command";

	// This has to agree with the name you give the bot in the Slack app directory
	private static final String BOT_NAME = "enigmabot";

	private boolean initComplete = false;

	private boolean running = false;

	// Slack API token
	private String apiToken = null;

	// Slack object instance - our main connection for integrating with Slack
	private Slack slack = null;

	// Slack Real Time Messaging (RTM) client
	private RTMClient rtmClient = null;

	private ArrayList<ISlackBotCommand> slackCommands = null;

	private String botUserId = null;

	// Attributes for listing Slack Bot commands
	private static final String LIST_CMD_NAME = "list";

	private static final String LIST_CMD_DESCRIPTION = "list supported commands";

	private static final String LIST_CMD_PATTERN = "[lL]ist.*";

	List<Command> cmdList  = null;

	private static EnigmaBot instance = null;

	public static EnigmaBot getInstance() {
		if (null == instance) {
			instance = new EnigmaBot();
		}
		return instance;
	}

	private EnigmaBot() {
		cmdList = new ArrayList<Command>();

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

	private class Command {
		String name;
		String description;

		public Command(String name, String description) {
			this.name = name;
			this.description = description;
		}
	}

	// Load supported commands from internal package - allows adding commands without requiring bot class
	// to be dependent on specific commands
	private boolean loadCommands() {

		slackCommands = new ArrayList<ISlackBotCommand>();


		for (Class<? extends ISlackBotCommand> c: (new Reflections(COMMANDS_PACKAGE)).getSubTypesOf(ISlackBotCommand.class)) {
			try {
				ISlackBotCommand cmd = c.newInstance();
				if (true == cmd.isInitComplete()) {
					slackCommands.add(cmd);
				} else {
					System.err.println("ERROR: failed to initialize " + c.getSimpleName());
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		// Add this class to the list to support the "list" command
		slackCommands.add(this);

		// Build a sorted list of commands the bot supports
		slackCommands.forEach(c -> {cmdList.add(new Command(c.getCommandName(), c.getCommandDescription()));});
		Collections.sort(cmdList, new Comparator<Command>() {
			@Override
			public int compare(Command cmd0, Command cmd1) {
				return cmd0.name.compareTo(cmd1.name);
			}});

		return true;
	}

	public List<ISlackBotCommand> getCommands() {
		return slackCommands;
	}

	// Parse command by checking against command reg-ex (public to support unit testing)
	public boolean matchCommand(ISlackBotCommand command, String text) {
		return text.matches(command.getCommandPattern());
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

			// Determine the bot's user ID to avoid reacting to our own responses
			UsersListRequest req = UsersListRequest.builder().token(apiToken).presence(0).build();
			try {
				UsersListResponse response = slack.methods().usersList(req);
				if (response.isOk()) {
					List<User> members = response.getMembers();
					for (User m: members) {
						if (BOT_NAME.equals(m.getName())) {
							botUserId = m.getId();
							break;
						}
					}
					if (null == botUserId) {
						System.err.println("ERROR: couldn't get bot user ID");
						return false;
					}
				} else {
					System.err.println("ERROR: couldn't get list of Slack users");
					return false;
				}

			} catch (SlackApiException e) {
				e.printStackTrace();
				return false;
			}

			// The main processing loop for the bot - receives a message from Slack, parses, and executes
			rtmClient.addMessageHandler(new RTMMessageHandler() {
				@Override
				public void handle(String messageContents) {
					System.out.println(" -> MSG: " + messageContents);
					SlackMessage msg = SlackMessageFactory.CreateSlackMessageObject(messageContents);
					if (null == msg) {
						System.err.println("ERROR: couldn't deserialize Slack message");
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
						if (null != botUserId) {
							if (botUserId.equals(msg.getUser())) {
								return;
							}
						}

						if (matchCommand(c, text)) {
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

		running = true;
		System.out.println(this.getClass().getSimpleName() + " is running");

		return true;

	}

	// Stop the bot
	public void stop() {
		try {
			System.out.println("Stopping...");
			rtmClient.close();  // calls disconnect()
			running = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public String getBotUserId() {
		return botUserId;
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
	public boolean isInitComplete() {
		return initComplete;
	}

	@Override
	public boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message) {
		StringBuilder sb = new StringBuilder("Here are the commands I support:\n");

		cmdList.forEach(c -> { sb.append(c.name + " - " + c.description + "\n");});
		ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), sb.toString().trim());

		return true;
	}
}
