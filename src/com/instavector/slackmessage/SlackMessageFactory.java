package com.instavector.slackmessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class SlackMessageFactory {

	private SlackMessageFactory() {}

	public static SlackMessage CreateSlackMessageObject(String messageText) {
		Gson gson = new Gson();
		SlackMessage msgObj = null;

		try {
			msgObj = gson.fromJson(messageText, SlackMessage.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return null;
		}
		return msgObj;
	}
}
