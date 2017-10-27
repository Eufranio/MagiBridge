package com.magitechserver.magibridge.discord;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.UCHandler;
import com.magitechserver.magibridge.events.MBMessageEvent;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.ReplacerUtil;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Task.builder().execute(task -> proccess(e)).submit(MagiBridge.getInstance());
    }

    private void proccess(MessageReceivedEvent e) {
        MBMessageEvent messageEvent = new MBMessageEvent(e.getGuild(), Sponge.getCauseStackManager().getCurrentCause(), e.getMessage());
        Sponge.getEventManager().post(messageEvent);
        if (messageEvent.isCancelled()) return;

        // Basics
        String channelID = e.getChannel().getId();
        String message = processMessage(e);

        if(!isValidMessage(e)) return;
        if(message.isEmpty() && e.getMessage().getAttachments().isEmpty()) return;
        if(!isListenableChannel(channelID)) return;

        boolean canUseColors = MagiBridge.getConfig().CHANNELS.COLOR_REQUIRED_ROLE.equalsIgnoreCase("everyone")
                || e.getMember().getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase(MagiBridge.getConfig().CHANNELS.COLOR_REQUIRED_ROLE));

        String name = e.getMember().getEffectiveName();
        String toprole = e.getMember().getRoles().size() >= 1 ? e.getMember().getRoles().get(0).getName() : MagiBridge.getConfig().MESSAGES.NO_ROLE_PLACEHOLDER;
        Map<String, String> colors = MagiBridge.getConfig().COLORS.COLORS;
        String toprolecolor = "99AAB5";

        if(e.getMember().getRoles().size() >= 1) {
            Role firstRole = e.getMember().getRoles().get(0);
            if (firstRole.getColor() != null) {
                String hex = Integer.toHexString(firstRole.getColor().getRGB()).toUpperCase();
                if (hex.length() == 8) hex = hex.substring(2);
                if (colors.containsKey(hex)) {
                    toprolecolor = colors.get(hex);
                }
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%user%", name);
        placeholders.put("%message%", canUseColors ? message : message.replaceAll("&([0-9a-fA-FlLkKrR])", "").replaceAll("§([0-9a-fA-FlLkKrR])", ""));
        placeholders.put("%toprole%", toprole);
        placeholders.put("%toprolecolor%", toprolecolor);

        boolean hasAttachment = e.getMessage().getAttachments().size() >= 1;

        // Handle console command
        if(message.startsWith(MagiBridge.getConfig().CHANNELS.CONSOLE_COMMAND) && isListenableChannel(channelID)) {
            DiscordHandler.dispatchCommand(e);
            return;
        }

        // Handle player list command
        if(message.equalsIgnoreCase(MagiBridge.getConfig().CHANNELS.LIST_COMMAND) && isListenableChannel(channelID)) {
            DiscordHandler.dispatchList(e.getMessage(), e.getChannel());
            return;
        }

        // UltimateChat hook active
        if (MagiBridge.getConfig().CHANNELS.USE_UCHAT && !MagiBridge.getConfig().CHANNELS.USE_NUCLEUS) {
            String chatChannel = MagiBridge.getConfig().CHANNELS.UCHAT.UCHAT_CHANNELS.get(channelID);
            FormatType format = FormatType.DISCORD_TO_SERVER_FORMAT;
            if (chatChannel != null) {
                UCHandler.handle(chatChannel, format, placeholders, hasAttachment, e.getMessage().getAttachments());
            }
        }

        // Nucleus hook active
        if(MagiBridge.getConfig().CHANNELS.USE_NUCLEUS && !MagiBridge.getConfig().CHANNELS.USE_UCHAT) {
            FormatType format = FormatType.DISCORD_TO_SERVER_FORMAT;
            boolean isStaffChannel = channelID.equals(MagiBridge.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL);
            NucleusHandler.handle(isStaffChannel, format, placeholders, hasAttachment, e.getMessage().getAttachments());
        }
    }

    private boolean isListenableChannel(String channel) {
        if(MagiBridge.getConfig().CHANNELS.USE_UCHAT && MagiBridge.getConfig().CHANNELS.UCHAT.UCHAT_CHANNELS.containsKey(channel)) {
            return true;
        }

        if(MagiBridge.getConfig().CHANNELS.USE_NUCLEUS && (
                MagiBridge.getConfig().CHANNELS.NUCLEUS.GLOBAL_CHANNEL.equals(channel)
             || MagiBridge.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL.equals(channel))) {
            return true;
        }

        if(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL.equals(channel)) {
            return true;
        }

        return false;
    }

    private String processMessage(MessageReceivedEvent e) {
        String message = e.getMessage().getStrippedContent();
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getAuthor().isFake()) return "";
        if (message == null && e.getMessage().getAttachments().size() == 0 || message.trim().isEmpty() && e.getMessage().getAttachments().size() == 0) return "";
        if (MagiBridge.getConfig().CORE.CUT_MESSAGES) {
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
        return ReplacerUtil.replaceEach(EmojiParser.parseToAliases(message), MagiBridge.getConfig().REPLACER.REPLACER);
    }

    private boolean isValidMessage(MessageReceivedEvent e) {
        String message = e.getMessage().getStrippedContent();
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getAuthor().isFake()) return false;
        if (message == null && e.getMessage().getAttachments().size() == 0 || message.trim().isEmpty() && e.getMessage().getAttachments().size() == 0) return false;
        return true;
    }
}
