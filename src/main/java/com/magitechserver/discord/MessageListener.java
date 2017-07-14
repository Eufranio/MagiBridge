package com.magitechserver.discord;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import com.magitechserver.DiscordHandler;
import com.magitechserver.MagiBridge;
import com.magitechserver.UCHandler;
import com.magitechserver.util.BridgeCommandSource;
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
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getAuthor().isFake()) return;
        String name = e.getMember().getEffectiveName();
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

        // Handle console command
        if(message.startsWith(MagiBridge.getConfig().getString("channel", "console-command")) && isListenableChannel(channelID)) {
            if (e.getMember().getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "console-command-requiered-role")))) {
                DiscordHandler.sendMessageToChannel(e.getChannel().getId(), "**You don't have permission to use the console command!**");
                return;
            }
            String cmd = message.replace(MagiBridge.getConfig().getString("channel", "console-command") + " ", "");
            Sponge.getCommandManager().process(new BridgeCommandSource(e.getChannel().getId(), Sponge.getServer().getConsole()), cmd);
        }

        // UltimateChat hook active
        if (MagiBridge.getConfig().getBool("channel", "use-ultimatechat") && !MagiBridge.getConfig().getBool("channel", "use-nucleus")) {
            String chatChannel = MagiBridge.getConfig().getMap("channel", "ultimatechat").get(channelID);
            if (chatChannel != null) {

                if(MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(chatChannel)  != null) {
                    msg = MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(chatChannel).replace("%user%", name).replace("%msg%", message).replace("&", "§");
                }

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
        if(message.equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "player-list-command")) && isListenableChannel(channelID)) {
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

    public Boolean isListenableChannel(String channel) {
        if(MagiBridge.getConfig().getBool("channel", "use-ultimatechat") && MagiBridge.getConfig().getMap("channel", "ultimatechat").containsKey(channel)) {
            return true;
        }

        if(MagiBridge.getConfig().getBool("channel", "use-nucleus") && (
                MagiBridge.getConfig().getString("channel", "nucleus", "global-discord-channel").equals(channel)
             || MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel").equals(channel))) {
            return true;
        }

        if(MagiBridge.getConfig().getString("channel", "main-discord-channel").equals(channel)) {
            return true;
        }

        return false;
    }

}
