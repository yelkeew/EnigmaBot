# EnigmaBot
Slack Bot that responds to a few simple commands

## Requirements
1. Integrate with Slack
1. Provide a list of commands
1. Retrieve answers to at least four commands
1. Have a name and avatar
1. Provide a README file for the project
1. Implement unit tests
1. Integrate tests of service layer
1. Integrate Travis CI to build and test the project

## Design
EnigmaBot uses the [jSlack](https://github.com/seratch/jslack) library for Slack integration.

Supported commands implement the `ISlackBotCommand` interface and are contained within the `com.instavector.slackmessage` package.  The main `EnigmaBot` class uses the [Reflections](https://github.com/ronmamo/reflections) library to locate commands at runtime.

## Build
EnigmaBot uses Maven.  Run `mvn package` to build the project.   

## Test
 1. Populate ".*-token" properties files in the project directory
    * .api-token: [Slack API token](https://api.slack.com/bot-users), `apiToken = xxxxxxxxxx`
    * .weather-token: [Open Weather Map token](http://openweathermap.org/appid), `appId = xxxxxxxxxx`
    * .flickr-token: [Flickr API token](https://www.flickr.com/services/api/misc.api_keys.html), `key = xxxxxxxxxx, secret = xxxxxxxxxx`
 1. Add a bot to your Slack channel
 1. Upload the `robot.jpg` file as the bot's avatar
 1. Run the bot: `java -cp "target\deps\*;target\enigmabot-0.0.1.jar" com.instavector.slackbot.EnigmaBotMain`
 1. Create Slack channel for testing, e.g., "bot-testing" or direct-message the bot
 1. Invite bot to channel, `/invite @enigma-bot`
 1. Use `list` to see a list of commands supported by the bot

 
## TODO
1. Better regular expression handling for command arguments
1. Detect missing/incorrect command tokens on initialization, warn user, and exclude command from supported list