package com.magitechserver.magibridge;

import com.magitechserver.magibridge.util.Config;
import com.magitechserver.magibridge.util.ReplacerUtil;
import com.magitechserver.magibridge.util.Webhooking;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    public static void sendMessageToChannel(String channel, String message) {
        if(!isValidChannel(channel)) return;
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

        // Mention discord users if they're mentioned in the message
        String message = placeholders.get("%message%");
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

        String raw = MagiBridge.getConfig().getString("messages", format);

        // Applies placeholders
        String finalMessage = ReplacerUtil.replaceEach(raw, placeholders);
        message = message.replaceAll("&([0-9a-fA-FlLkKrR])", "");

        if(removeEveryone) {
            message = message.replace("@everyone", "");
            message = message.replace("@here", "");
        }

        if(deleteTime > 0) {
            MagiBridge.jda.getTextChannelById(channel).sendMessage(message)
                    .queue(m -> m.delete().queueAfter(deleteTime, TimeUnit.SECONDS));
        } else if(Config.useWebhooks()) {
            Webhooking.sendWebhookMessage(ReplacerUtil.replaceEach(MagiBridge.getConfig().getString("messages", "webhook-name"), placeholders),
                    placeholders.get("%player%"),
                    placeholders.get("%message%"),
                    channel);
        } else {
            MagiBridge.jda.getTextChannelById(channel).sendMessage(message).queue();
        }

    }

}
