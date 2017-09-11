package com.magitechserver.magibridge.discord;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.UCHandler;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        // Basics
        String channelID = e.getChannel().getId();
        String message = processMessage(e);

        if(!isValidMessage(e)) return;
        if(message.isEmpty()) return;
        if(!isListenableChannel(channelID)) return;

        boolean canUseColors = MagiBridge.getConfig().getString("channel", "color-allowed-role").equalsIgnoreCase("everyone")
                || e.getMember().getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "color-allowed-role")));

        String name = e.getMember().getEffectiveName();
        String toprole = e.getMember().getRoles().size() >= 1 ? e.getMember().getRoles().get(0).getName() : MagiBridge.getConfig().getString("messages", "no-role-prefix");
        Map<String, String> colors = MagiBridge.getConfig().getMap("colors");
        String toprolecolor = MagiBridge.getConfig().getString("colors", "default");

        if(e.getMember().getRoles().size() >= 1) {
            String hex = Integer.toHexString(e.getMember().getRoles().get(0).getColor().getRGB()).toUpperCase();
            if(colors.containsKey(hex)) {
                toprolecolor = colors.get(hex);
            }
        }

        Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%user%", name);
            placeholders.put("%message%", canUseColors ? message : message.replaceAll("&([0-9a-fA-FlLkKrR])", "").replaceAll("§([0-9a-fA-FlLkKrR])", ""));
            placeholders.put("%toprole%", toprole);
            placeholders.put("%toprolecolor%", toprolecolor);
            placeholders.putAll(MagiBridge.getConfig().getMap("discord-to-mc-replacer"));

        boolean hasAttachment = e.getMessage().getAttachments().size() >= 1;

        // Handle console command
        if(message.startsWith(MagiBridge.getConfig().getString("channel", "console-command")) && isListenableChannel(channelID)) {
            DiscordHandler.dispatchCommand(e);
            return;
        }

        // Handle player list command
        if(message.equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "player-list-command")) && isListenableChannel(channelID)) {
            DiscordHandler.dispatchList(e.getMessage(), e.getChannel());
            return;
        }

        // UltimateChat hook active
        if (MagiBridge.getConfig().getBool("channel", "use-ultimatechat") && !MagiBridge.getConfig().getBool("channel", "use-nucleus")) {
            String chatChannel = MagiBridge.getConfig().getMap("channel", "ultimatechat").get(channelID);
            String format = MagiBridge.getConfig().getString("messages", "discord-to-server-global-format");
            if (chatChannel != null) {
                UCHandler.handle(chatChannel, format, placeholders);
            }
        }

        // Nucleus hook active
        if(MagiBridge.getConfig().getBool("channel", "use-nucleus") && !MagiBridge.getConfig().getBool("channels", "use-ultimatechat")) {
            String format = MagiBridge.getConfig().getString("messages", "discord-to-server-global-format");
            boolean isStaffChannel = channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel"));
            NucleusHandler.handle(isStaffChannel, format, placeholders, hasAttachment, e.getMessage().getAttachments());
        }
    }

    private boolean isListenableChannel(String channel) {
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

    private String processMessage(MessageReceivedEvent e) {
        String message = e.getMessage().getContent();
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getAuthor().isFake()) return "";
        if (message == null && e.getMessage().getAttachments().size() == 0 || message.trim().isEmpty() && e.getMessage().getAttachments().size() == 0) return "";
        if (MagiBridge.getConfig().getBool("misc", "cut-messages")) {
            if (message.length() > 120) {
                message = message.substring(0, 120);
            }
        }
        if (message.startsWith("```")) {
            message = message.substring(0, message.length() - 3).substring(3);
        }
        if (message.startsWith("`")) {
            message = message.substring(0, message.length() - 1).substring(1);
        }
        return message;
    }

    private boolean isValidMessage(MessageReceivedEvent e) {
        String message = e.getMessage().getContent();
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getAuthor().isFake()) return false;
        if (message == null && e.getMessage().getAttachments().size() == 0 || message.trim().isEmpty() && e.getMessage().getAttachments().size() == 0) return false;
        return true;
    }
}
