package com.magitechserver.magibridge;

import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public static void sendMessageToChannel(String channel, String message) {
        if(!isValidChannel(channel)) return;
        MagiBridge.jda.getTextChannelById(channel).sendMessage(message.replaceAll("&([0-9a-fA-FlLkKrR])", "")).queue();
    }

    public static void sendMessageToChannel(String channel, String message, long deleteTime) {
        if(!isValidChannel(channel)) return;
        MagiBridge.jda.getTextChannelById(channel).sendMessage(message.replaceAll("&([0-9a-fA-FlLkKrR])", ""))
                .queue(m -> m.delete().queueAfter(deleteTime, TimeUnit.SECONDS));
    }

    private static boolean isValidChannel(String channel) {
        if(MagiBridge.jda == null) return false;
        if(MagiBridge.jda.getTextChannelById(channel) == null) {
            MagiBridge.logger.error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiBridge.logger.error("Replace it with a valid one then reload the plugin!");
            return false;
        }
        return true;
    }

}
