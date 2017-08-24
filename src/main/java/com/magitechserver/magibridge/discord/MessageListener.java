package com.magitechserver.magibridge.discord;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.UCHandler;
import com.magitechserver.magibridge.util.BridgeCommandSource;
import com.magitechserver.magibridge.util.GroupUtil;
import com.magitechserver.magibridge.util.ReplacerUtil;
import flavor.pie.boop.BoopableChannel;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        // Basics
        String channelID = e.getChannel().getId();
        String message = proccessMessage(e);

        if(message.isEmpty()) return;

        String name = e.getMember().getEffectiveName();
        String toprole = e.getMember().getRoles().size() >= 1 ? e.getMember().getRoles().get(0).getName() : MagiBridge.getConfig().getString("messages", "no-role-prefix");

        Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%user%", name);
            placeholders.put("%msg%", message);
            placeholders.putAll(MagiBridge.getConfig().getMap("discord-to-mc-replacer"));

        /*
        String msg = ReplacerUtil.replaceEach(MagiBridge.getConfig().getString("messages", "discord-to-server-global-format")
                .replace("%user%", name)
                .replace("%msg%", message)
                .replace("&", "§"),
                MagiBridge.getConfig().getMap("discord-to-mc-replacer"));
        */
        boolean hasAttachment = e.getMessage().getAttachments().size() >= 1;

        // Handle console command
        if(message.startsWith(MagiBridge.getConfig().getString("channel", "console-command")) && isListenableChannel(channelID)) {
            DiscordHandler.dispatchCommand(e);
        }

        // Handle player list command
        if(message.equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "player-list-command")) && isListenableChannel(channelID)) {
            DiscordHandler.dispatchList(e.getMessage(), e.getChannel());
        }

        // Check if the user can use colors
        if(!MagiBridge.getConfig().getString("channel", "color-allowed-role").equalsIgnoreCase("everyone") || e.getMember().getRoles().stream().noneMatch(r ->
                r.getName().equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "color-allowed-role")))) {
            message = message.replaceAll("&([0-9a-fA-FlLkKrR])", "").replaceAll("§([0-9a-fA-FlLkKrR])", "");
        }

        // UltimateChat hook active
        if (MagiBridge.getConfig().getBool("channel", "use-ultimatechat") && !MagiBridge.getConfig().getBool("channel", "use-nucleus")) {
            String chatChannel = MagiBridge.getConfig().getMap("channel", "ultimatechat").get(channelID);
            String format = MagiBridge.getConfig().getString("messages", "server-to-discord-format");
            if (chatChannel != null) {
                UCHandler.handle(chatChannel, format, placeholders);
            }
        }

        // Nucleus hook active
        if(MagiBridge.getConfig().getBool("channel", "use-nucleus") && !MagiBridge.getConfig().getBool("channels", "use-ultimatechat")) {
            String format = MagiBridge.getConfig().getString("messages", "discord-to-server-staff-format");
            boolean isStaffChannel = channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel"));

            NucleusHandler.handle(isStaffChannel, format, placeholders, hasAttachment, e.getMessage().getAttachments());

        }
    }

    private static Boolean isListenableChannel(String channel) {
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

    private String proccessMessage(MessageReceivedEvent e) {
        String message = e.getMessage().getContent();
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getAuthor().isFake()) return "";
        if (message == null && e.getMessage().getAttachments().size() == 0 || message.trim().isEmpty() && e.getMessage().getAttachments().size() == 0) return "";
        if (message.length() > 120) {
            message = message.substring(0, 120);
        }
        if (message.startsWith("```")) {
            message = message.substring(0, message.length() - 3).substring(3);
        }
        if (message.startsWith("`")) {
            message = message.substring(0, message.length() - 1).substring(1);
        }
        if(message.toLowerCase().contains(MagiBridge.getConfig().getString("channel", "console-command").toLowerCase())
                || message.toLowerCase().contains(MagiBridge.getConfig().getString("channel", "player-list-command").toLowerCase())) return "";

        return message;
    }

}
