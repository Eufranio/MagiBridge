package com.magitechserver.magibridge;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.API.uChatAPI;
import org.spongepowered.api.Sponge;

/**
 * Created by Frani on 05/07/2017.
 */
public class UCHandler {

    // Obvious?
    public static void sendMessageToChannel(UCChannel channel, String message) {
        channel.sendMessage(Sponge.getServer().getConsole(), message);
    }

    // Channels can be upper/lower case
    public static UCChannel getChannelByCaseInsensitiveName(String name) {
        for (UCChannel channel : uChatAPI.getChannels())
            if (channel.getName().equalsIgnoreCase(name)) {
                return channel;
            }
        return null;
    }
}
