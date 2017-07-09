package com.magitechserver.discord;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import com.magitechserver.UCHandler;
import com.magitechserver.util.Config;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String channelID = e.getChannel().getId();
        String chatChannel = Config.CHANNELS.get(channelID);
        if(chatChannel != null) {
            String message = e.getMessage().getContent();
            String name = e.getMember().getEffectiveName();
            if(UCHandler.getChannelByCaseInsensitiveName(chatChannel) == null) return;
            UCChannel channel = UCHandler.getChannelByCaseInsensitiveName(chatChannel);

            if(e.getAuthor().getId() == e.getJDA().getSelfUser().getId() || e.getAuthor().isFake()) return;
            if(message == null || message.trim().isEmpty()) return;
            if(message.length() > 120) {
                message = message.substring(0, message.length() - 120);
            }
            if(message.startsWith("```")) {
                message = message.substring(0, message.length() - 3).substring(3);
            }
            if(message.startsWith("`")) {
                message = message.substring(0, message.length() - 1).substring(1);
            }
            String msg = Config.DISCORD_TO_SERVER_FORMAT.replace("%user%", name).replace("%msg%", message);

            UCHandler.sendMessageToChannel(channel, msg);
        }
    }

}
