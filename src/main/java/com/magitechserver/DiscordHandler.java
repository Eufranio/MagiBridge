package com.magitechserver;


/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public static void sendMessageToChannel(String channel, String message) {
        if(MagiBridge.jda.getTextChannelById(channel) == null) {
            MagiBridge.logger.error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiBridge.logger.error("Replace it with a valid one then reload the plugin!");
            return;
        }

        MagiBridge.jda.getTextChannelById(channel).sendMessage(message).queue();
    }

}
