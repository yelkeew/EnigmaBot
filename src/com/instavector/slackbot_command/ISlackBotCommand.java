package com.instavector.slackbot_command;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.instavector.slackmessage.SlackMessage;

public interface ISlackBotCommand {

	public abstract String getCommandName();

	public abstract String getCommandDescription();

	public abstract String getCommandPattern();

	public abstract boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message);

	public static boolean SendResponse(Slack slackInstance, String apiToken, String channel, String response) {
		try {
			ChatPostMessageResponse postMessage = slackInstance.methods().chatPostMessage(ChatPostMessageRequest.builder()
					.token(apiToken)
					.channel(channel)
					.asUser(true)
					.text(response)
					.build());
			if (false == postMessage.isOk()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
