/*
 * Class to segregate the main() method from the logic in the primary EngimaBot class
 */
package com.instavector.slackbot;

public class EnigmaBotMain {

	public static void main(String[] args) {

		// Make sure the bot shuts down properly when the application exits
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				EnigmaBot bot = EnigmaBot.getInstance();
				if (true == bot.isRunning()) {
					bot.stop();
				}
			}
		});

		EnigmaBot bot = null;
		try {
			bot = EnigmaBot.getInstance();
			if (false == bot.isInitComplete()) {
				System.err.println("ERROR: couldn't intialize bot");
				System.exit(1);
			}

			if (false == bot.start()) {
				System.err.println("ERROR: couldn't start bot");
			}

			// stay alive while the bot is listening for commands
			while (true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
