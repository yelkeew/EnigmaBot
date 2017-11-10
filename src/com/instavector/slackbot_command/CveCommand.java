package com.instavector.slackbot_command;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;

import com.github.seratch.jslack.Slack;
import com.instavector.slackmessage.SlackMessage;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class CveCommand implements ISlackBotCommand {

	private static final String CMD_NAME = "cve";

	private static final String CMD_DESCRIPTION = "search recent CVEs";

	private static final String CMD_PATTERN = "[Cc]ve.*";

	MongoClient client = null;
	MongoDatabase db = null;
	MongoCollection<Document> coll = null;

	private static final String CVE_URI_BASE = "http://cve.mitre.org/cgi-bin/cvename.cgi?name=";

	private static final String CVE_DB_NAME = "cvedb";
	private static final String CVE_COLL_NAME = "cves";

	private static final String ID_KEY = "id";
	private static final String PUBLISHED_KEY = "Published";
	private static final String SUMMARY_KEY = "summary";
	private static final String REFERENCES_KEY = "references";

	boolean initComplete = false;

	public CveCommand() {
		try {
			client = new MongoClient();
			if (null == client) {
				return;
			}
			db = client.getDatabase(CVE_DB_NAME);
			if (null == db) {
				return;
			}
			coll = db.getCollection(CVE_COLL_NAME);
			if (null == coll) {
				return;
			}
			// Put an index on ID for better searching; collection is too large
			// to search through with default RAM constraints otherwise.
			coll.createIndex(new Document(ID_KEY, 1));

			initComplete = true;
		} catch (IllegalArgumentException e) {
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

		String cmdSubstr = "ve ";  // remove initial char to resolve potential case sensitivity problem
		String text = message.getText();
		String arg = null;

		// Find the argument after the command
		int pos = text.indexOf(cmdSubstr);
		if (-1 == pos) {
			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(),
					"Tell me what CVE you want information for, OK?");
			return true;
		} else {
			arg = text.substring(pos + cmdSubstr.length());
		}

		Document query = new Document();

		// Decide if this is a CVE id or keyword to search for
		if (arg.startsWith("CVE-")) {
			query.append(ID_KEY, arg);
		} else {
			query.append(SUMMARY_KEY, Pattern.compile(".*" + arg + ".*"));
		}

		ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), "Searching...");

		StringBuilder sb = new StringBuilder();

		// Look up CVE ID in the database; sort by newest first using published date
		FindIterable<Document> iter = coll.find(query).sort(new Document(PUBLISHED_KEY, -1)).limit(10);
		if (null == iter.first()) {
			ISlackBotCommand.SendResponse(
					slackInstance,
					apiToken,
					message.getChannel(),
					"Sorry, I couldn't find any CVEs matching your query");
			return true;
		}

		// Iterate over the returned documents and format each for inclusion in a Slack message
		for (Document cveDoc: iter) {
			if (0 != sb.length()) {
				sb.append("--------------------\n");  // Separator between records
			}
			String id = (String) cveDoc.get(ID_KEY);
			sb.append("*ID*: <" + CVE_URI_BASE + id + "|" + id + ">\n");
			sb.append("*Published*: " + ((Date) cveDoc.get(PUBLISHED_KEY)).toString() + "\n");
			sb.append("*Summary*: " + (String) cveDoc.get(SUMMARY_KEY) + "\n");
			sb.append("*References*:\n");

			@SuppressWarnings("unchecked")
			List<String> references = (List<String>)cveDoc.get(REFERENCES_KEY);
			if (references.isEmpty()) {
				sb.append("\u2022 none\n");
			} else {
				for (String ref : references) {
					sb.append("\u2022 " + ref + "\n");
				}
			}
		}

		ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(), sb.toString());

		return true;
	}
}
