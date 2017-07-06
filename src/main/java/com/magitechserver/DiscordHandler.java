package com.magitechserver;

import com.magitechserver.util.Config;

/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public void sendMessageToChannel(String channel, String message) {
        if(MagiShat.getInstance().getJDA().getTextChannelById(channel) == null) {
            MagiShat.getInstance().logger.error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiShat.getInstance().logger.error("Replace it with a valid one then reload the plugin!");
            return;
        }
        MagiShat.getInstance().getJDA().getTextChannelById(channel).sendMessage(message).queue();
    }

}
