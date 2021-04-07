package com.magitechserver.magibridge.config.categories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class CoreCategory {

    @Setting(value = "bot-token", comment = "Bot Token of the Bot User that you want to be used when sending messages to Discord")
    public String BOT_TOKEN = "";
    @Setting(value = "death-messages-enabled", comment = "Should MagiBridge send death messages to Discord?")
    public boolean DEATH_MESSAGES_ENABLED = true;
    @Setting(value = "advancement-messages-enabled", comment = "Should MagiBridge send advancement messages to Discord?")
    public boolean ADVANCEMENT_MESSAGES_ENABLED = true;
    @Setting(value = "use-boop", comment = "Should MagiBridge enable Boop support? Will only work if Boop is installed and updated")
    public boolean USE_BOOP = false;
    @Setting(value = "hide-vanished-chat", comment = "Don't send messages of a player to Discord if he is vanished")
    public boolean HIDE_VANISHED_CHAT = false;
    @Setting(value = "cut-messages", comment = "Set to false if MagiBridge should NOT cut messages coming from Discord with more than\n" +
            "120 characters. This can turn the chat ugly if someone sends a big message")
    public boolean CUT_MESSAGES = false;
    @Setting(value = "topic-updater-interval", comment = "Topic Updater interval in minutes, minimum is 5. If you're having rate limit errors, set this to 10 or higher!")
    public int UPDATER_INTERVAL = 10;
    @Setting(value = "enable-topic-updater", comment = "Should MagiBridge enable the Topic Updater, updating the topic of the main Discord channel?")
    public boolean ENABLE_UPDATER = true;
    @Setting(value = "enable-console-logging", comment = "Should MagiBridge send console messages to Discord? You must set the console-discord-channel for this to work!")
    public boolean ENABLE_CONSOLE_LOGGING = false;

    @Setting(value = "send-helpop", comment = "Should MagiBridge send Nucleus' HelpOp messages to Discord? The channel that the messages will be sent\n" +
            "SHOULD be defined in the Channels section if this is enabled. \n" +
            "NOTE: The HelpOp Discord channel is READ ONLY, it will forward any user messages to the in-game staff channel.")
    public boolean SEND_HELPOP = false;

    @Setting(value = "send-broadcast", comment = "Should MagiBridge relay broadcast messages? If the broadcast-channel is not set in the channels \n" +
            "section, the broadcasts will be sent to the main channel. ")
    public boolean sendBroadcasts = true;

}
