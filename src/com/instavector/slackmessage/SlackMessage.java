package com.instavector.slackmessage;

import com.google.gson.annotations.SerializedName;

public class SlackMessage {

	private String type;
	private String presence;
	private String title;
	private String subtitle;
	private String msg;
	private String content;
	private String channel;
	private String user;
	@SerializedName("bot_id")
	private String botId;
	private String text;
	private String launchUri;
	private String avatarImage;
	private String ssbFilename;
	private String imageUri;
	@SerializedName("is_shared")
	private String isShared;
	@SerializedName("event_ts")
	private String eventTs;

	public static final String MSG_TYPE_HELLO = "hello";
	public static final String MSG_TYPE_RECONNECT_URL = "reconnect_url";
	public static final String MSG_TYPE_PRESENCE_CHANGE = "presence_change";
	public static final String MSG_TYPE_USER_TYPING = "typing";
	public static final String MSG_TYPE_MESSAGE = "message";

	public static final String MSG_PRESENCE_ACTIVE = "active";
	public static final String MSG_PRESENCE_AWAY = "away";


	public SlackMessage() {
	}

	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getPresence() {
		return presence;
	}

	public void setPresence(String presence) {
		this.presence = presence;
	}

	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getSubtitle() {
		return subtitle;
	}


	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public String getChannel() {
		return channel;
	}


	public void setChannel(String channel) {
		this.channel = channel;
	}


	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotId(String botId) {
		this.botId = botId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLaunchUri() {
		return launchUri;
	}


	public void setLaunchUri(String launchUri) {
		this.launchUri = launchUri;
	}


	public String getAvatarImage() {
		return avatarImage;
	}


	public void setAvatarImage(String avatarImage) {
		this.avatarImage = avatarImage;
	}


	public String getSsbFilename() {
		return ssbFilename;
	}


	public void setSsbFilename(String ssbFilename) {
		this.ssbFilename = ssbFilename;
	}


	public String getImageUri() {
		return imageUri;
	}


	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}


	public String getIsShared() {
		return isShared;
	}


	public void setIsShared(String isShared) {
		this.isShared = isShared;
	}


	public String getEventTs() {
		return eventTs;
	}


	public void setEventTs(String eventTs) {
		this.eventTs = eventTs;
	}

}
