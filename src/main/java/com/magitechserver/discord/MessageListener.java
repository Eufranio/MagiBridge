package com.magitechserver.discord;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import com.magitechserver.DiscordHandler;
import com.magitechserver.MagiBridge;
import com.magitechserver.UCHandler;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        // Basics
        String channelID = e.getChannel().getId();
        String message = e.getMessage().getContent();
        String name = e.getMember().getEffectiveName();
        if (name == null) return;
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
        String msg = MagiBridge.getConfig().getString("messages", "discord-to-server-global-format").replace("%user%", name).replace("%msg%", message).replace("&", "§");

        // UltimateChat hook active
        if (MagiBridge.getConfig().getBool("channel", "use-ultimatechat") && !MagiBridge.getConfig().getBool("channel", "use-nucleus")) {
            String chatChannel = MagiBridge.getConfig().getMap("channel", "ultimatechat").get(channelID);
            if (chatChannel != null) {

                if (UCHandler.getChannelByCaseInsensitiveName(chatChannel) == null) return;
                UCChannel channel = UCHandler.getChannelByCaseInsensitiveName(chatChannel);

                UCHandler.sendMessageToChannel(channel, msg);
            }
        }

        // Nucleus hook active
        if(MagiBridge.getConfig().getBool("channel", "use-nucleus") && !MagiBridge.getConfig().getBool("channels", "use-ultimatechat")) {
            MessageChannel chatChannel = null;
            if(channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "global-discord-channel"))) {
                chatChannel = Sponge.getServer().getBroadcastChannel();
            } else if(channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel"))) {
                chatChannel = StaffChatMessageChannel.getInstance();
            }

            if (chatChannel != null) {
                if(channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel"))) {
                    msg = MagiBridge.getConfig().getString("messages", "discord-to-server-staff-format").replace("%user%", name).replace("%msg%", message).replace("&", "§");
                }

                Text text = Text.of(msg);
                chatChannel.getMembers().forEach(player -> player.sendMessage(text));

                // Can't do this yet. Waiting for Nucleus update
                // chatChannel.send(name, msg);
            }
        }

        // Handle player list command
        if(message.equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "player-list-command"))) {
            String players = null;
            if(Sponge.getServer().getOnlinePlayers().size() == 0) {
                msg = "**There are no players online!**";
            } else {
                for (Player player : Sponge.getServer().getOnlinePlayers()) {
                    players = players == null ? player.getName() : players + player.getName() + ", ";
                }
                msg = "**Players online (" + Sponge.getServer().getOnlinePlayers().size() + "/" + Sponge.getServer().getMaxPlayers() + "):** "
                        + "```" + players + "```";
            }
            DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"), msg);
        }


    }

}
