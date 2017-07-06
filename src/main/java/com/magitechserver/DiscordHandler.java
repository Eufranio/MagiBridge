package com.magitechserver;


/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public static void sendMessageToChannel(String channel, String message) {
        if(MagiShat.jda.getTextChannelById(channel) == null) {
            MagiShat.logger.error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiShat.logger.error("Replace it with a valid one then reload the plugin!");
            return;
        }
        MagiShat.jda.getTextChannelById(channel).sendMessage(message).queue();
    }

}
