package com.magitechserver.discord;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import com.magitechserver.UCHandler;
import com.magitechserver.util.Config;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String msg = null;
        if (Config.USE_UCHAT && !Config.USE_NUCLEUS) {
            String channelID = e.getChannel().getId();
            String chatChannel = Config.CHANNELS.get(channelID);
            if (chatChannel != null) {
                String message = e.getMessage().getContent();
                String name = e.getMember().getEffectiveName();

                // Basic checks
                if (UCHandler.getChannelByCaseInsensitiveName(chatChannel) == null) return;
                UCChannel channel = UCHandler.getChannelByCaseInsensitiveName(chatChannel);

                if (e.getAuthor().getId() == e.getJDA().getSelfUser().getId() || e.getAuthor().isFake()) return;
                if (message == null || message.trim().isEmpty()) return;
                if (message.length() > 120) {
                    message = message.substring(0, message.length() - 120);
                }
                if (message.startsWith("```")) {
                    message = message.substring(0, message.length() - 3).substring(3);
                }
                if (message.startsWith("`")) {
                    message = message.substring(0, message.length() - 1).substring(1);
                }

                // Apply placeholders
                msg = Config.DISCORD_TO_SERVER_FORMAT.replace("%user%", name).replace("%msg%", message);

                UCHandler.sendMessageToChannel(channel, msg);
            }
        }

        if(Config.USE_NUCLEUS && !Config.USE_UCHAT) {
            String channelID = e.getChannel().getId();
            MessageChannel chatChannel = null;
            if(channelID.equals(Config.NUCLEUS_GLOBAL_DISCORD_CHANNEL)) {
                chatChannel = Sponge.getServer().getBroadcastChannel();
            } else if(channelID.equals(Config.NUCLEUS_STAFF_DISCORD_CHANNEL)) {
                chatChannel = StaffChatMessageChannel.getInstance();
            }

            if (chatChannel != null) {
                String message = e.getMessage().getContent();
                String name = e.getMember().getEffectiveName();

                // Basic checks
                if (e.getAuthor().getId() == e.getJDA().getSelfUser().getId() || e.getAuthor().isFake()) return;
                if (message == null || message.trim().isEmpty()) return;
                if (message.length() > 120) {
                    message = message.substring(0, message.length() - 120);
                }
                if (message.startsWith("```")) {
                    message = message.substring(0, message.length() - 3).substring(3);
                }
                if (message.startsWith("`")) {
                    message = message.substring(0, message.length() - 1).substring(1);
                }

                // Apply placeholders
                msg = Config.DISCORD_TO_SERVER_FORMAT.replace("%user%", name).replace("%msg%", message).replace("&", "§");

                // Change message if on a staff channel
                if(channelID.equals(Config.NUCLEUS_STAFF_DISCORD_CHANNEL)) {
                    msg = Config.DISCORD_TO_SERVER_STAFF_FORMAT.replace("%user%", name).replace("%msg%", message).replace("&", "§");
                }

                // Finally sends the message
                Text text = Text.of(msg);
                chatChannel.getMembers().forEach(player -> player.sendMessage(text));

                // Can't do this yet. Waiting for Nucleus update
                // chatChannel.send(name, msg);
            }
        }
    }

}
