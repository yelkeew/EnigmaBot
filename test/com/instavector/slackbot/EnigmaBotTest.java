package com.instavector.slackbot;

import static org.junit.Assert.assertNotNull;
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
import com.instavector.slackmessage.SlackMessage;
import com.instavector.slackmessage.SlackMessageFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnigmaBotTest {

	private static void assertTrueVerbose(String message, boolean condition) {
		System.out.println("TEST: " + message);
		assertTrue(message, condition);
	}

	private static void assertNotNullVerbose(String message, Object object) {
		System.out.println("TEST: " + message);
		assertNotNull(message, object);
	}

	/*
	 * Test bot startup & shutdown
	 */
	@Test
	public void test_01_EnigmaBotStartStop() {
		System.out.println("\nMETHOD: " + new Object(){}.getClass().getEnclosingMethod().getName());
		EnigmaBot bot = EnigmaBot.getInstance();
		assertTrueVerbose("EnigmaBot initialized", bot.isInitComplete());
		bot.start();
		assertTrueVerbose("EnigmaBot running", bot.isRunning());
		assertNotNullVerbose("EnigmaBot User ID", bot.getBotUserId());
		bot.stop();
		assertTrueVerbose("EnigmaBot stopped", !bot.isRunning());
	}

	/*
	 * Test for existence of necessary properties files - used for connections to external services
	 */
	@Test
	public void test_02_PropertiesFiles() {
		System.out.println("\nMETHOD: " + new Object(){}.getClass().getEnclosingMethod().getName());
		assertTrueVerbose("Slack API key file", Files.exists(Paths.get(EnigmaBot.API_PROPERTIES_FILE)));
		assertTrueVerbose("Jokes text file", Files.exists(Paths.get(JokeCommand.JOKES_FILE)));
		assertTrueVerbose("Flickr API key file", Files.exists(Paths.get(PictureCommand.FLICKR_PROPERTIES_FILE)));
		assertTrueVerbose("OpenWeatherMap API key file", Files.exists(Paths.get(WeatherCommand.WEATHER_PROPERTIES_FILE)));
	}

	/*
	 * Test that commands load successfully and expected attributes are populated
	 */
	@Test
	public void test_03_Commands() {
		System.out.println("\nMETHOD: " + new Object(){}.getClass().getEnclosingMethod().getName());
		EnigmaBot bot = EnigmaBot.getInstance();
		List<ISlackBotCommand> commands = bot.getCommands();
		assertTrueVerbose("Slack commands exist", 0 != commands.size());
		for (ISlackBotCommand c: commands) {
			assertTrueVerbose("Command initialized: " + c.getClass().getSimpleName(), c.isInitComplete());
			assertTrueVerbose("Command name: " + c.getClass().getSimpleName(), !c.getCommandName().isEmpty());
			assertTrueVerbose("Command description: " + c.getClass().getSimpleName(), !c.getCommandDescription().isEmpty());
			assertTrueVerbose("Command pattern: " + c.getClass().getSimpleName(), !c.getCommandPattern().isEmpty());
		}
	}

	/*
	 * Test that expected command message content can be successfully parsed
	 */
	@Test
	public void test_04_CommandParsing() {
		System.out.println("\nMETHOD: " + new Object(){}.getClass().getEnclosingMethod().getName());
		final String contentToken = "__CONTENT__";
		String slackMsgTemplate = "{" +
				"\"type\":\"desktop_notification\"," +
				"\"title\":\"SlackGroupName\"," +
				"\"subtitle\":\"user\"," +
				"\"msg\":\"1510192378.000092\"," +
				"\"content\":\"__CONTENT__\"," +
				"\"channel\":\"D7RSDBC9K\"," +
				"\"launchUri\":\"slack://channel?id=D7RSDBC9K&message=1510192378000092&team=T26DG1MND\"," +
				"\"avatarImage\":\"https://avatars.slack-edge.com/2016-08-30/74477277969_52a00ef5845530a0d0a0_192.jpg\"," +
				"\"ssbFilename\":\"knock_brush.mp3\"," +
				"\"imageUri\":null," +
				"\"is_shared\":false," +
				"\"event_ts\":\"1510192378.000199\"" +
				"}";

		String[] commandContent = {"list", "cve CVE-1999-0001", "picture robot", "weather baltimore"};

		EnigmaBot bot = EnigmaBot.getInstance();

		// Match desired message content
		for (String cmdMsg: commandContent) {
			// Populate Slack message JSON and convert to SlackMessage object
			String slackMsgText = slackMsgTemplate.replace(contentToken, cmdMsg);
			SlackMessage slackMsg = SlackMessageFactory.CreateSlackMessageObject(slackMsgText);
			boolean matched = false;

			for (ISlackBotCommand cmd: bot.getCommands()) {
				matched = bot.matchCommand(cmd, slackMsg.getContent());
				if (matched) {
					break;
				}
			}
			assertTrueVerbose("Match command: " + cmdMsg, matched);
		}

		// Reject bad message content
		for (String cmdMsg: commandContent) {
			// Populate Slack message JSON and convert to SlackMessage object
			String slackMsgText = slackMsgTemplate.replace(contentToken, "x" + cmdMsg);
			SlackMessage slackMsg = SlackMessageFactory.CreateSlackMessageObject(slackMsgText);
			boolean matched = false;

			for (ISlackBotCommand cmd: bot.getCommands()) {
				matched = bot.matchCommand(cmd, slackMsg.getContent());
				if (matched) {
					break;
				}
			}
			assertTrueVerbose("Unrecognized command: x" + cmdMsg, !matched);
		}
	}
}
