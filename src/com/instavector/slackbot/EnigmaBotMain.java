package com.instavector.slackbot;

public class EnigmaBotMain {

	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				EnigmaBot bot = EnigmaBot.getInstance();
				if (true == bot.isInitComplete()) {
					bot.stop();
				}
			}
		});

		EnigmaBot bot = null;
		try {

			bot = EnigmaBot.getInstance();

			if (false == bot.start()) {
				System.err.println("ERROR: couldn't start bot");
			}

			// stay alive while the bot is listening for commands
			while (true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bot.stop();
		}

	}

}
