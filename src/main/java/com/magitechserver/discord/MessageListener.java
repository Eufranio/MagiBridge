package com.magitechserver.discord;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import com.magitechserver.DiscordHandler;
import com.magitechserver.MagiBridge;
import com.magitechserver.UCHandler;
import com.magitechserver.util.BridgeCommandSource;
import com.magitechserver.util.ReplacerUtil;
import flavor.pie.boop.BoopableChannel;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

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
        String toprole = e.getMember().getRoles().size() >= 1 ? e.getMember().getRoles().get(0).getName() : MagiBridge.getConfig().getString("messages", "no-role-prefix");
        if (message == null && e.getMessage().getAttachments().size() == 0 || message.trim().isEmpty() && e.getMessage().getAttachments().size() == 0) return;
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
        boolean hasAttachment = e.getMessage().getAttachments().size() >= 1;

        // Handle console command
        if(message.startsWith(MagiBridge.getConfig().getString("channel", "console-command")) && isListenableChannel(channelID)) {
            if (e.getMember().getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "console-command-required-role")))) {
                DiscordHandler.sendMessageToChannel(e.getChannel().getId(), "**You don't have permission to use the console command!**");
                return;
            }
            String cmd = message.replace(MagiBridge.getConfig().getString("channel", "console-command") + " ", "");
            Sponge.getCommandManager().process(new BridgeCommandSource(e.getChannel().getId(), Sponge.getServer().getConsole()), cmd);
        }

        // Handle player list command
        if(message.equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "player-list-command")) && isListenableChannel(channelID)) {
            String players = null;
            Collection<Player> cplayers = Sponge.getServer().getOnlinePlayers();
            if(cplayers.size() == 0) {
                msg = "**There are no players online!**";
            } else {
                String listformat = MagiBridge.getConfig().getString("messages", "player-list-name");
                if(cplayers.size() >= 1) {
                    for (Player player : cplayers) {
                        players = players + listformat
                                .replace("%player%", player.getName())
                                .replace("%prefix%", player.getOption("prefix")
                                        .orElse("")) +
                                ", ";
                    }
                    players = players.substring(0, players.length() - 2);
                }
                msg = "**Players online (" + Sponge.getServer().getOnlinePlayers().size() + "/" + Sponge.getServer().getMaxPlayers() + "):** "
                        + "```" + players + "```";
            }
            DiscordHandler.sendMessageToChannel(e.getChannel().getId(), msg);
        }

        if(message.toLowerCase().contains(MagiBridge.getConfig().getString("channel", "console-command").toLowerCase())
                || message.toLowerCase().contains(MagiBridge.getConfig().getString("channel", "player-list-command").toLowerCase())) return;

        // UltimateChat hook active
        if (MagiBridge.getConfig().getBool("channel", "use-ultimatechat") && !MagiBridge.getConfig().getBool("channel", "use-nucleus")) {
            String chatChannel = MagiBridge.getConfig().getMap("channel", "ultimatechat").get(channelID);
            if (chatChannel != null) {

                if(MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(chatChannel)  != null) {
                    msg = ReplacerUtil.replaceEach(MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(chatChannel)
                            .replace("%user%", name)
                            .replace("%msg%", message)
                            .replace("%toprole%", toprole),
                            MagiBridge.getConfig().getMap("discord-to-mc-replacer"));
                }

                UCChannel channel = UCHandler.getChannelByCaseInsensitiveName(chatChannel);

                UCHandler.sendMessageToChannel(channel, msg);
            }
        }

        // Nucleus hook active
        if(MagiBridge.getConfig().getBool("channel", "use-nucleus") && !MagiBridge.getConfig().getBool("channels", "use-ultimatechat")) {
            MessageChannel chatChannel = null;
            if(channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "global-discord-channel"))) {
                if(Sponge.getPluginManager().getPlugin("boop").isPresent() && MagiBridge.getConfig().getBool("misc", "use-boop")) {
                    chatChannel = new BoopableChannel(Sponge.getServer().getBroadcastChannel().getMembers());
                } else {
                    chatChannel = Sponge.getServer().getBroadcastChannel();
                }
            } else if(channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel"))) {
                chatChannel = StaffChatMessageChannel.getInstance();
            }

            if (chatChannel != null) {
                if(channelID.equals(MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel"))) {
                    msg = ReplacerUtil.replaceEach(MagiBridge.getConfig().getString("messages", "discord-to-server-staff-format")
                            .replace("%user%", name)
                            .replace("%msg%", message)
                            .replace("%toprole%", toprole)
                            .replace("&", "§"),
                            MagiBridge.getConfig().getMap("discord-to-mc-replacer"));
                }

                if(hasAttachment) {
                    Text attachment = attachmentBuilder(e);
                    Text textMsg = Text.of(msg);
                    if(!message.trim().isEmpty()) {
                        chatChannel.getMembers().forEach(player -> player.sendMessage(Text.of(textMsg)));
                    }
                    chatChannel.getMembers().forEach(player -> player.sendMessage(Text.of(
                            ReplacerUtil.replaceEach(MagiBridge.getConfig().getString("messages", "discord-to-server-global-format")
                                    .replace("%user%", name)
                                    .replace("%msg%", "")
                                    .replace("%toprole%", toprole)
                                    .replace("&", "§"),
                            MagiBridge.getConfig().getMap("discord-to-mc-replacer"))).concat(attachment)
                    ));
                } else {
                    Text plainMsg = Text.of(msg);
                    chatChannel.getMembers().forEach(player -> player.sendMessage(plainMsg));
                }

                // Can't do this yet. Waiting for Nucleus update
                // chatChannel.send(name, msg);
            }
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

    private Text attachmentBuilder(MessageReceivedEvent e) {
        Text text = Text.of();
        Text.Builder builder = Text.builder();
        Text hover = TextSerializers.FORMATTING_CODE.deserialize("&bAttachment: ").concat(Text.NEW_LINE);
        for(Message.Attachment attachment : e.getMessage().getAttachments()) {
            hover = hover.concat(Text.of(attachment.getFileName())).concat(Text.NEW_LINE);
        }
        hover = hover.concat(TextSerializers.FORMATTING_CODE.deserialize("&bClick to open the attachment!"));
        URL url = null;
        try {
            url = new URL(e.getMessage().getAttachments().get(0).getUrl());
        } catch (MalformedURLException exception) {}
        text = Text.builder(MagiBridge.getConfig().getString("messages", "attachment-name"))
                .color(TextColors.AQUA)
                .onHover(TextActions.showText(hover))
                .onClick(TextActions.openUrl(url))
                .build();
        return text;
    }

}
