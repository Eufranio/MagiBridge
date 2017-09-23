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
        if(!isValidChannel(channel)) return;
        message = translateEmojis(message, MagiBridge.jda.getTextChannelById(channel).getGuild());
        List<String> usersMentioned = new ArrayList<>();
        Arrays.stream(message.split(" ")).filter(word ->
                word.startsWith("@")).forEach(mention ->
                usersMentioned.add(mention.substring(1)));

        if(!usersMentioned.isEmpty()) {
            for (String user : usersMentioned) {
                List<User> users = MagiBridge.jda.getUsersByName(user, true);
                if(!users.isEmpty()) {
                    message = message.replaceAll("@" + user, "<@" + users.get(0).getId() + ">");
                }
            }
        }
        MagiBridge.jda.getTextChannelById(channel).sendMessage(message.replaceAll("&([0-9a-fA-FlLkKrR])", "")).queue();
    }

    public static void sendMessageToChannel(String channel, String message, long deleteTime) {
        if(!isValidChannel(channel)) return;
        message = translateEmojis(message, MagiBridge.jda.getTextChannelById(channel).getGuild());
        MagiBridge.jda.getTextChannelById(channel).sendMessage(message.replaceAll("&([0-9a-fA-FlLkKrR])", ""))
                .queue(m -> m.delete().queueAfter(deleteTime, TimeUnit.SECONDS));
    }

    private static boolean isValidChannel(String channel) {
        if(MagiBridge.jda == null) return false;
        if(MagiBridge.jda.getTextChannelById(channel) == null) {
            MagiBridge.logger.error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiBridge.logger.error("Replace it with a valid one then reload the plugin!");
            return false;
        }
        return true;
    }

    public static void sendMessageToDiscord(String channel, String format, Map<String, String> placeholders, boolean removeEveryone, long deleteTime) {
        if(!isValidChannel(channel)) return;

        String rawFormat = MagiBridge.getConfig().getString("messages", format);

        // Applies placeholders
        String message = ReplacerUtil.replaceEach(rawFormat, placeholders);
        message = message.replaceAll("&([0-9a-fA-FlLkKrR])", "");
        message = translateEmojis(message, MagiBridge.jda.getTextChannelById(channel).getGuild());

        if(removeEveryone) {
            message = message.replace("@everyone", "");
            message = message.replace("@here", "");
        }

        // Mention discord users if they're mentioned in the message
        List<String> usersMentioned = new ArrayList<>();
        Arrays.stream(message.split(" ")).filter(word ->
                word.startsWith("@")).forEach(mention ->
                usersMentioned.add(mention.substring(1)));

        if(!usersMentioned.isEmpty()) {
            for (String mention : usersMentioned) {
                List<Member> users = new ArrayList<>();
                MagiBridge.jda.getGuilds().forEach(guild ->
                        guild.getMembers().stream().filter(m ->
                                m.getEffectiveName().equalsIgnoreCase(mention))
                                .forEach(m -> users.add(m)));
                List<Role> roles = MagiBridge.jda.getRolesByName(mention, true);
                if(!users.isEmpty()) {
                    message = message.replaceAll("@" + mention, users.get(0).getAsMention());
                }
                if (!roles.isEmpty()) {
                    message = message.replaceAll("@" + mention, roles.get(0).getAsMention());
                }
            }
        }

        if(deleteTime > 0) {
            MagiBridge.jda.getTextChannelById(channel).sendMessage(message)
                    .queue(m -> m.delete().queueAfter(deleteTime, TimeUnit.SECONDS));
        } else if(Config.useWebhooks()) {
            message = translateEmojis(placeholders.get("%message%"), MagiBridge.jda.getTextChannelById(channel).getGuild());
            placeholders.replace("%message%", message);
            Webhooking.sendWebhookMessage(ReplacerUtil.replaceEach(MagiBridge.getConfig().getString("messages", "webhook-name"), placeholders),
                    placeholders.get("%player%"),
                    placeholders.get("%message%"),
                    channel);
        } else {
            MagiBridge.jda.getTextChannelById(channel).sendMessage(message).queue();
        }

    }

    public static void dispatchCommand(MessageReceivedEvent e) {
        String args[] = e.getMessage().getContent().replace(MagiBridge.getConfig().getString("channel", "console-command") + " ", "").split(" ");

        if (!canUseCommand(e.getMember(), args[0])) {
            DiscordHandler.sendMessageToChannel(e.getChannel().getId(), MagiBridge.getConfig().getString("messages", "console-command-no-permission"));
            return;
        }

        String cmd = e.getMessage().getContent().replace(MagiBridge.getConfig().getString("channel", "console-command") + " ", "");
        Sponge.getCommandManager().process(new BridgeCommandSource(e.getChannel().getId(), Sponge.getServer().getConsole()), cmd);
    }

    public static void dispatchList(Message m, MessageChannel c) {
        String players = "";
        boolean shouldDelete = MagiBridge.getConfig().getBool("channel", "delete-list-message");
        String msg;
        Collection<Player> cplayers =  new ArrayList<>();
        Sponge.getServer().getOnlinePlayers().forEach(p -> {
            if(!p.get(Keys.VANISH).orElse(false)) {
                cplayers.add(p);
            }
        });
        if(cplayers.size() == 0) {
            msg = MagiBridge.getConfig().getString("messages", "no-players-message");
        } else {
            String listformat = MagiBridge.getConfig().getString("messages", "player-list-name");
            if(cplayers.size() >= 1) {
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
        if(shouldDelete) {
            m.delete().queueAfter(10, TimeUnit.SECONDS);
            sendMessageToChannel(c.getId(), msg, 10);
        } else {
            sendMessageToChannel(c.getId(), msg);
        }
    }

    private static boolean canUseCommand(Member m, String command) {
        if (MagiBridge.getConfig().getMap("channel", "commands-role-override") == null) return false;
        if(MagiBridge.getConfig().getMap("channel", "commands-role-override").get(command) != null) {
            if (MagiBridge.getConfig().getMap("channel", "commands-role-override").get(command).equalsIgnoreCase("everyone")) {
                return true;
            }
        }
        if (m.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase(MagiBridge.getConfig().getString("channel", "console-command-required-role")))) {
            return true;
        }
        return MagiBridge.getConfig().getMap("channel", "commands-role-override").get(command) != null && m.getRoles().stream().anyMatch(role ->
                role.getName().equalsIgnoreCase(MagiBridge.getConfig().getMap("channel", "commands-role-override").get(command)));
    }

    private static String translateEmojis(String message, Guild guild) {
        for (Emote emote : guild.getEmotes()) {
            message = message.replace(":" + emote.getName() + ":", emote.getAsMention());
        }
        return message;
    }

}
