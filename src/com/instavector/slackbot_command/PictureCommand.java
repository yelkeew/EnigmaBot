package com.instavector.slackbot_command;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.files.FilesUploadRequest;
import com.github.seratch.jslack.api.methods.response.files.FilesUploadResponse;
import com.instavector.slackmessage.SlackMessage;

public class PictureCommand implements ISlackBotCommand {

	private static final String CMD_NAME = "picture";

	private static final String CMD_DESCRIPTION = "get a picture from Flickr";

	private static final String CMD_PATTERN = ".*[Pp]icture.*";

	private static final String FLICKR_PROPERTIES_FILE = ".flickr-token";

	private static String key = null;
	private static String secret = null;

	public PictureCommand() {
		try {
			FileInputStream fis = new FileInputStream(new File(FLICKR_PROPERTIES_FILE));
			Properties props = new Properties();
			props.load(fis);
			key = props.getProperty("key");
			secret = props.getProperty("secret");
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: properties file '" + FLICKR_PROPERTIES_FILE + "' doesn't exist");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR: couldn't read properties file '" + FLICKR_PROPERTIES_FILE + "'");
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
	public boolean executeCommand(Slack slackInstance, String apiToken, SlackMessage message) {
		String cmdSubstr = "icture ";  // remove initial char to resolve potential case sensitivity problem
		String text = message.getText();
		String arg = null;

		// Find the argument after the command
		int pos = text.indexOf(cmdSubstr);
		if (-1 == pos) {
			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(),
					"Tell me what subject you want to see in a picture (e.g., robot), OK?");
			return true;
		} else {
			arg = text.substring(pos + cmdSubstr.length());
		}

		// Search flickr for pictures with the specified string as a tag
		Flickr flickr = new Flickr(key, secret, new REST());
		SearchParameters searchParameters = new SearchParameters();
		String[] argParts = arg.split(" ");
		searchParameters.setTags(argParts);
		searchParameters.setSafeSearch(Flickr.SAFETYLEVEL_SAFE);  // No naughty pictures

		File photoFile = null;
		try {
			PhotoList<Photo> photoList = flickr.getPhotosInterface().search(searchParameters, 0, 0);
			if (0 == photoList.size()) {
				ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(),
						"Sorry, I couldn't get a picture for your request.");
				return false;
			}

			// Provide some user feedback in case the image download & file upload takes a few seconds
			ISlackBotCommand.SendResponse(slackInstance, apiToken, message.getChannel(),
					"Getting a picture for your request...");

			// Pick a random picture from the returned list
			Photo photo = photoList.get((new Random()).nextInt(photoList.size()));

			// Download and save off the image
			BufferedImage img = flickr.getPhotosInterface().getImage(photo, Size.MEDIUM);
			photoFile = new File("photo.jpg");
			System.out.println("Writing to " + photoFile.getAbsolutePath());
			if (false == ImageIO.write(img, "jpg", photoFile)) {
				System.err.println("ERROR: failed to write image file");
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// Upload the image to the slack channel
		List<String> chList = new ArrayList<String>();
		chList.add(message.getChannel());
		try {
			FilesUploadRequest req = FilesUploadRequest.builder()
					.token(apiToken)
					.file(photoFile)
					.filename(arg + ".jpg")
					.title(arg)
					.channels(chList)
					.build();
			FilesUploadResponse r = slackInstance.methods().filesUpload(req);
			if (!r.isOk()) {
				System.err.println("ERROR: upload failed - " + r.getError());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
