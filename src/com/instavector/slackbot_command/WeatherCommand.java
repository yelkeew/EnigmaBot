package com.instavector.slackbot_command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.seratch.jslack.Slack;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.instavector.slackmessage.SlackMessage;

public class WeatherCommand implements ISlackBotCommand {

	private static final String CMD_NAME = "weather";

	private static final String CMD_DESCRIPTION = "get the weather forecaset for zip code or city name";

	private static final String CMD_PATTERN = "[Ww]eather.*";

	private static final String WEATHER_URI_CITY = "http://api.openweathermap.org/data/2.5/weather?q=";

	private static final String WEATHER_URI_ZIP = "http://api.openweathermap.org/data/2.5/weather?zip=";

	private static final String WEATHER_PROPERTIES_FILE = ".weather-token";

	private static String appId = null;

	private boolean initComplete = false;

	public WeatherCommand() {
		try {
			FileInputStream fis = new FileInputStream(new File(WEATHER_PROPERTIES_FILE));
			Properties props = new Properties();
			props.load(fis);
			appId = props.getProperty("appId");

			initComplete = true;
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: properties file '" + WEATHER_PROPERTIES_FILE + "' doesn't exist");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR: couldn't read properties file '" + WEATHER_PROPERTIES_FILE + "'");
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

	@Override
	public boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message) {

		if (false == initComplete) {
			return false;
		}

		String cmdSubstr = "eather ";  // remove initial char to resolve potential case sensitivity problem
		String text = message.getText();
		String arg = null;

		// Find the argument after the command
		int pos = text.indexOf(cmdSubstr);
		if (-1 == pos) {
			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(),
					"Tell me what city (e.g., Baltimore) or ZIP code (e.g., 21224) you want weather for, OK?");
			return true;
		} else {
			arg = text.substring(pos + cmdSubstr.length());
		}

		String uri = WEATHER_URI_CITY;

		String zipCodeRegex = "[0-9]{5}";
		if (arg.matches(zipCodeRegex)) {
			uri = WEATHER_URI_ZIP;
		}

		uri += arg.replace(" ", "%20");  // escape the spaces, e.g. "Los Angeles"
		uri += "&appid=" + appId;
		uri += "&units=Imperial";

		Client weatherClient = ClientBuilder.newClient();
		WebTarget target = weatherClient.target(uri);

		try {
			Response response = target.request(MediaType.APPLICATION_JSON).get();

			if (response.getStatus() != Status.OK.getStatusCode()) {
				ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(),
						"Sorry, I couldn't get your weather forecast.");
				return false;
			}

			/*
			   Typical response from Open Weather Map
			{
			"coord":{"lon":-76.61,"lat":39.29},
			"weather":[
				{
					"id":500,
					"main":"Rain",
					"description":"light rain",
					"icon":"10d"
				}
			],
			"base":"stations",
			"main":{
				"temp":286.71,
				"pressure":1025,
				"humidity":71,
				"temp_min":285.15,
				"temp_max":288.15
			},
			"visibility":16093,
			"wind":{"speed":2.1,"deg":80},
			"clouds":{"all":40},
			"dt":1509829560,
			"sys":{
				"type":1,
				"id":1328,
				"message":0.1691,
				"country":"US",
				"sunrise":1509795533,
				"sunset":1509832838},
				"id":4347778,
				"name":"Baltimore",
				"cod":200
			}
			 */

			String weatherResponseStr = response.readEntity(String.class);
			JsonParser parser = new JsonParser();
			JsonObject weatherObject = parser.parse(weatherResponseStr).getAsJsonObject();
			String conditions = weatherObject.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("main").getAsString();
			String description = weatherObject.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
			String temp = weatherObject.get("main").getAsJsonObject().get("temp").getAsString();
			String temp_min = weatherObject.get("main").getAsJsonObject().get("temp_min").getAsString();
			String temp_max = weatherObject.get("main").getAsJsonObject().get("temp_max").getAsString();

			// Response uses unicode char for degree symbol
			String responseStr = "Today's weather for *" + arg + "* is " + conditions + " (" + description + "), Temp: " +
					temp + " \u00b0F, Min Temp: " + temp_min + " \u00b0F, Max Temp: " + temp_max + " \u00b0F";


			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), responseStr);
		} catch (Exception e) {
			e.printStackTrace();
			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), "Sorry, I couldn't get your weather forecast.");
		}

		return true;
	}

}
