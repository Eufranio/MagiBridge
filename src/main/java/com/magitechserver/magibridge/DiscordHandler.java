package com.magitechserver.magibridge;

import com.magitechserver.magibridge.util.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public static void sendMessageToChannel(String channel, String message) {
        if (!isValidChannel(channel)) return;
        message = translateEmojis(message, MagiBridge.jda.getTextChannelById(channel).getGuild());
        List<String> usersMentioned = new ArrayList<>();
        Arrays.stream(message.split(" ")).filter(word ->
                word.startsWith("@")).forEach(mention ->
                usersMentioned.add(mention.substring(1)));

        if (!usersMentioned.isEmpty()) {
            for (String user : usersMentioned) {
                List<User> users = MagiBridge.jda.getUsersByName(user, true);
                if (!users.isEmpty()) {
                    message = message.replaceAll("@" + user, "<@" + users.get(0).getId() + ">");
                }
            }
        }
        MagiBridge.jda.getTextChannelById(channel).sendMessage(message.replaceAll("&([0-9a-fA-FlLkKrR])", "")).queue();
    }

    public static void sendMessageToChannel(String channel, String message, long deleteTime) {
        if (!isValidChannel(channel)) return;
        message = translateEmojis(message, MagiBridge.jda.getTextChannelById(channel).getGuild());
        MagiBridge.jda.getTextChannelById(channel).sendMessage(message.replaceAll("&([0-9a-fA-FlLkKrR])", ""))
                .queue(m -> m.delete().queueAfter(deleteTime, TimeUnit.SECONDS));
    }

    private static boolean isValidChannel(String channel) {
        if (MagiBridge.jda == null) return false;
        if (MagiBridge.jda.getTextChannelById(channel) == null) {
            MagiBridge.logger.error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiBridge.logger.error("Replace it with a valid one then reload the plugin!");
            return false;
        }
        return true;
    }

    public static void sendMessageToDiscord(String channel, FormatType format, Map<String, String> placeholders, boolean removeEveryone, long deleteTime, boolean withWebhook) {
        if (!isValidChannel(channel)) return;

        String rawFormat = format.get();

        // Applies placeholders
        String message = ReplacerUtil.replaceEach(rawFormat, placeholders);
        message = message.replaceAll("&([0-9a-fA-FlLkKrR])", "");
        message = translateEmojis(message, MagiBridge.jda.getTextChannelById(channel).getGuild());

        if (removeEveryone) {
            message = message.replace("@everyone", "");
            message = message.replace("@here", "");
        }

        // Mention discord users if they're mentioned in the message
        List<String> usersMentioned = new ArrayList<>();
        Arrays.stream(message.split(" ")).filter(word ->
                word.startsWith("@")).forEach(usersMentioned::add);

        if (!usersMentioned.isEmpty()) {
            for (String mention : usersMentioned) {
                List<Member> users = new ArrayList<>();
                MagiBridge.jda.getGuilds().forEach(guild ->
                        guild.getMembers().stream().filter(m ->
                                m.getEffectiveName().equalsIgnoreCase(mention))
                                .forEach(users::add));
                List<Role> roles = MagiBridge.jda.getRolesByName(mention, true);
                if (!users.isEmpty()) {
                    message = message.replace(mention, users.get(0).getAsMention().replace("!", ""));
                }
                if (!roles.isEmpty()) {
                    message = message.replace(mention, roles.get(0).getAsMention());
                }
            }
        }

        if (deleteTime > 0) {
            MagiBridge.jda.getTextChannelById(channel).sendMessage(message)
                    .queue(m -> m.delete().queueAfter(deleteTime, TimeUnit.SECONDS));
        } else if (MagiBridge.getConfig().CHANNELS.USE_WEBHOOKS && withWebhook) {
            message = translateEmojis(placeholders.get("%message%"), MagiBridge.jda.getTextChannelById(channel).getGuild());
            placeholders.replace("%message%", message);
            Webhooking.sendWebhookMessage(ReplacerUtil.replaceEach(MagiBridge.getConfig().MESSAGES.WEBHOOK_NAME, placeholders),
                    placeholders.get("%player%"),
                    placeholders.get("%message%"),
                    channel);
        } else {
            MagiBridge.jda.getTextChannelById(channel).sendMessage(message).queue();
        }
    }

    public static void sendMessageToDiscord(String channel, FormatType format, Map<String, String> placeholders, boolean removeEveryone, long deleteTime) {
        sendMessageToDiscord(channel, format, placeholders, removeEveryone, deleteTime, true);
    }

    public static void dispatchCommand(MessageReceivedEvent e) {
        String args[] = e.getMessage().getContentDisplay().replace(MagiBridge.getConfig().CHANNELS.CONSOLE_COMMAND + " ", "").split(" ");

        if (!canUseCommand(e.getMember(), args[0])) {
            DiscordHandler.sendMessageToChannel(e.getChannel().getId(), MagiBridge.getConfig().MESSAGES.CONSOLE_NO_PERMISSION);
            return;
        }

        String cmd = e.getMessage().getContentDisplay().replace(MagiBridge.getConfig().CHANNELS.CONSOLE_COMMAND + " ", "");
        Sponge.getCommandManager().process(new BridgeCommandSource(e.getChannel().getId(), Sponge.getServer().getConsole()), cmd);
    }

    public static void dispatchList(Message m, MessageChannel c) {
        String players = "";
        boolean shouldDelete = MagiBridge.getConfig().CHANNELS.DELETE_LIST;
        String msg;
        Collection<Player> cplayers = new ArrayList<>();
        Sponge.getServer().getOnlinePlayers().forEach(p -> {
            if (!p.get(Keys.VANISH).orElse(false)) {
                cplayers.add(p);
            }
        });
        if (cplayers.size() == 0) {
            msg = MagiBridge.getConfig().MESSAGES.NO_PLAYERS;
        } else {
            String listformat = MagiBridge.getConfig().MESSAGES.PLAYER_LIST_NAME;
            if (cplayers.size() >= 1) {
                for (Player player : cplayers) {
                    players = players + listformat
                            .replace("%player%", player.getName())
                            .replace("%topgroup%", GroupUtil.getHighestGroup(player))
                            .replace("%prefix%", player.getOption("prefix")
                                    .orElse("")) +
                            ", ";
                }
                players = players.substring(0, players.length() - 2);
            }
            msg = "**Players online (" + Sponge.getServer().getOnlinePlayers().size() + "/" + Sponge.getServer().getMaxPlayers() + "):** "
                    + "```" + players + "```";
        }
        if (shouldDelete) {
            m.delete().queueAfter(10, TimeUnit.SECONDS);
            sendMessageToChannel(c.getId(), msg, 10);
        } else {
            sendMessageToChannel(c.getId(), msg);
        }
    }

    private static boolean canUseCommand(Member m, String command) {
        if (MagiBridge.getConfig().CHANNELS.COMMANDS_ROLE_OVERRIDE == null) return false;
        if (MagiBridge.getConfig().CHANNELS.COMMANDS_ROLE_OVERRIDE.get(command) != null) {
            if (MagiBridge.getConfig().CHANNELS.COMMANDS_ROLE_OVERRIDE.get(command).equalsIgnoreCase("everyone")) {
                return true;
            }
        }
        if (m.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase(MagiBridge.getConfig().CHANNELS.CONSOLE_REQUIRED_ROLE))) {
            return true;
        }
        return MagiBridge.getConfig().CHANNELS.COMMANDS_ROLE_OVERRIDE.get(command) != null && m.getRoles().stream().anyMatch(role ->
                role.getName().equalsIgnoreCase(MagiBridge.getConfig().CHANNELS.COMMANDS_ROLE_OVERRIDE.get(command)));
    }

    private static String translateEmojis(String message, Guild guild) {
        for (Emote emote : guild.getEmotes()) {
            message = message.replace(":" + emote.getName() + ":", emote.getAsMention());
        }
        return message;
    }

}
