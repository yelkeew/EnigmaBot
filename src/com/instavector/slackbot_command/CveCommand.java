package com.instavector.slackbot_command;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.seratch.jslack.Slack;
import com.instavector.slackmessage.SlackMessage;

public class CveCommand implements ISlackBotCommand {

	private static final String CMD_NAME = "cve";

	private static final String CMD_DESCRIPTION = "search recent CVEs";

	private static final String CMD_PATTERN = ".*[Cc][Vv][Ee].*";

	private static final String CVE_ZIP_URI = "https://static.nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-recent.json.zip";

	public CveCommand() {

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

	private boolean downloadCveZip() {
		Client cveClient = ClientBuilder.newClient();
		WebTarget target = cveClient.target(CVE_ZIP_URI);

		try {
			Response response = target.request(MediaType.APPLICATION_JSON).get();
			if (response.getStatus() != Status.OK.getStatusCode()) {
				return false;
			}

		} catch (Exception e) {

		}

		return true;
	}

	@Override
	public boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message) {

		return false;
	}
}
