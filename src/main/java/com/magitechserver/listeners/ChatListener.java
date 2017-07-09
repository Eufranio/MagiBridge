package com.magitechserver.listeners;

import br.net.fabiozumbi12.UltimateChat.API.SendChannelMessageEvent;
import com.magitechserver.DiscordHandler;
import com.magitechserver.util.Config;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import java.util.Map;

/**
 * Created by Frani on 05/07/2017.
 */
public class ChatListener {

    @Listener
    public void onMessage(SendChannelMessageEvent e) {
        String discordChannel = getKey(e.getChannel().getName().toLowerCase());
        if(discordChannel != null && getUCMessage(e) != null) {
            DiscordHandler.sendMessageToChannel(discordChannel, getUCMessage(e));
        }
    }


    public String getUCMessage(SendChannelMessageEvent e) {
        if(e.getSender() instanceof Player) {
            String content = e.getMessage();
            String sender = e.getSender().getName();
            String message = "**" + sender + "**: " + content;
            return message;
        }
        return null;
    }

    private String getKey(String value) {
        if(!Config.CHANNELS.containsValue(value)) return null;
        for (Map.Entry<String, String> values : Config.CHANNELS.entrySet()) {
            if(value.equals(values.getValue())) {
                return values.getKey();
            }
        }
        return null;
    }
}

