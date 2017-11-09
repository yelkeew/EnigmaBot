package com.instavector.slackbot_command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import com.github.seratch.jslack.Slack;
import com.instavector.slackmessage.SlackMessage;

public class JokeCommand implements ISlackBotCommand {

	public static final String JOKES_FILE = "jokes.txt";

	private static final String CMD_NAME = "joke";

	private static final String CMD_DESCRIPTION = "tell me a joke";

	private static final String CMD_PATTERN = "[Jj]oke.*";

	private static final String JOKE_PHRASE_REGEX = "\\|";

	private static final int JOKE_PART_DELAY_MS = 2000;

	private int numJokes = 0;

	private ArrayList<String[]> jokes;

	private boolean initComplete = true;

	public JokeCommand() {

		jokes = new ArrayList<String []>();

		// Read jokes file
		try {
			Stream<String> stream = Files.lines(Paths.get(JOKES_FILE));

			stream.forEach(line -> {
				//System.out.println(line);
				if (line.isEmpty()) {
					//System.out.println("  --> Empty");
				}
				else if (line.startsWith("#")) {
					//System.out.println("  --> Comment");
				}
				else {
					//System.out.println("  --> Joke");
					String []jokeParts = line.split(JOKE_PHRASE_REGEX);
					if (0 < jokeParts.length) {
						jokes.add(jokeParts);
						++ numJokes;
					}
				}
			});

			stream.close();

			initComplete = true;

		} catch (FileNotFoundException e) {
			System.err.println("ERROR: jokes file '" + JOKES_FILE + "' doesn't exist");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR: couldn't read jokes file '" + JOKES_FILE + "'");
			e.printStackTrace();
		}

	}

	@Override
	public String getCommandName() {
		return CMD_NAME;
	}

	@Override
	public String getCommandDescription() {
		return CMD_DESCRIPTION;
	}

	@Override
	public String getCommandPattern() {
		return CMD_PATTERN;
	}

	@Override
	public boolean isInitComplete() {
		return initComplete;
	}

	// Look up a joke - random index into list of jokes
	@Override
	public boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message) {

		if (false == initComplete) {
			return false;
		}

		int index = (new Random()).nextInt(numJokes);
		String []jokeParts = jokes.get(index);

		for (int i = 0; i < jokeParts.length; i ++) {
			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), jokeParts[i]);
			if (i < jokeParts.length - 1) {
				try {
					Thread.sleep(JOKE_PART_DELAY_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
		}

		return true;
	}
}
