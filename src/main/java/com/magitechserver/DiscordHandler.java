package com.magitechserver;

/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public void sendMessageToChannel(String channel, String message) {
        MagiShat.getInstance().getJDA().getTextChannelById(channel).sendMessage(message).queue();
    }

}
