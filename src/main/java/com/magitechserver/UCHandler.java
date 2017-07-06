package com.magitechserver;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.UChat;
import org.spongepowered.api.Sponge;

/**
 * Created by Frani on 05/07/2017.
 */
public class UCHandler {

    // Obvious?
    public static void sendMessageToChannel(String channel, String message) {

        UCChannel chatChannel = getChannelByCaseInsensitiveName(channel);

        if(!(chatChannel == null)) {
            chatChannel.sendMessage(Sponge.getServer().getConsole(), message);
        }
    }

    // Channels can be upper/lower case
    private static UCChannel getChannelByCaseInsensitiveName(String name) {
        for (UCChannel channel : UChat.get().getConfig().getChannels())
            if (channel.getName().equalsIgnoreCase(name)) {
                return channel;
            }
        return null;
    }

}
