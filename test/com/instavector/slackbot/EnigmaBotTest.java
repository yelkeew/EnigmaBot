package com.instavector.slackbot;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.instavector.slackbot_command.ISlackBotCommand;
import com.instavector.slackbot_command.JokeCommand;
import com.instavector.slackbot_command.PictureCommand;
import com.instavector.slackbot_command.WeatherCommand;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnigmaBotTest {

	/*
	 * Test bot startup & shutdown
	 */
	@Test
	public void test_01_EnigmaBotStartStop() {
		EnigmaBot bot = EnigmaBot.getInstance();
		assertTrue("EnigmaBot initialized", bot.isInitComplete());
		bot.start();
		assertTrue("EnigmaBot running", bot.isRunning());
		bot.stop();
		assertTrue("EnigmaBot stopped", !bot.isRunning());
	}

	/*
	 * Test for existence of necessary properties files - used for connections to external services
	 */
	@Test
	public void test_02_PropertiesFiles() {
		assertTrue("Slack API key file", Files.exists(Paths.get(EnigmaBot.API_PROPERTIES_FILE)));
		assertTrue("Jokes text file", Files.exists(Paths.get(JokeCommand.JOKES_FILE)));
		assertTrue("Flickr API key file", Files.exists(Paths.get(PictureCommand.FLICKR_PROPERTIES_FILE)));
		assertTrue("OpenWeatherMap API key file", Files.exists(Paths.get(WeatherCommand.WEATHER_PROPERTIES_FILE)));
	}

	/*
	 * Test that commands load successfully and expected attributes are populated
	 */
	@Test
	public void test_03_Commands() {
		EnigmaBot bot = EnigmaBot.getInstance();
		List<ISlackBotCommand> commands = bot.getCommands();
		assertTrue("Slack commands exist", 0 != commands.size());
		for (ISlackBotCommand c: commands) {
			assertTrue("Command initialized: " + c.getClass().getSimpleName(), c.isInitComplete());
			assertTrue("Command name: " + c.getClass().getSimpleName(), !c.getCommandName().isEmpty());
			assertTrue("Command description: " + c.getClass().getSimpleName(), !c.getCommandDescription().isEmpty());
			assertTrue("Command pattern: " + c.getClass().getSimpleName(), !c.getCommandPattern().isEmpty());
		}
	}
}
