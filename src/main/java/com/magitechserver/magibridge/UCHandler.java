package com.magitechserver.magibridge;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.API.uChatAPI;
import com.magitechserver.magibridge.util.ReplacerUtil;
import org.spongepowered.api.Sponge;

import java.util.Map;

/**
 * Created by Frani on 05/07/2017.
 */
public class UCHandler {

    // Channels can be upper/lower case
    public static UCChannel getChannelByCaseInsensitiveName(String name) {
        for (UCChannel channel : uChatAPI.getChannels())
            if (channel.getName().equalsIgnoreCase(name)) {
                return channel;
            }
        return null;
    }

    public static void handle(String channel, String format, Map<String, String> placeholders) {
        if(MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(channel) != null) {
            format = MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(channel);
        }
        UCChannel uc = UCHandler.getChannelByCaseInsensitiveName(channel);
        if(uc != null) {
            String message = ReplacerUtil.replaceEach(format, placeholders);
            uc.sendMessage(Sponge.getServer().getConsole(), message);
        }
    }

}
