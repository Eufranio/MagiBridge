package com.magitechserver.magibridge.discord;

import com.google.common.collect.Maps;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.util.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Frani on 17/04/2019.
 */
public class MessageBuilder {

    private String channel;
    private FormatType formatType;
    private boolean useWebhook = true;
    private boolean allowMentions = true;
    private boolean allowEveryone = false;
    private boolean allowHere = false;
    private int deleteTime = -1;
    private Map<String, String> placeholders = Maps.newHashMap();

    public static MessageBuilder forChannel(String channel) {
        return new MessageBuilder(channel);
    }

    private MessageBuilder(String channel) {
        this.channel = channel;
    }

    public MessageBuilder format(FormatType format) {
        this.formatType = format;
        return this;
    }

    public MessageBuilder placeholder(String name, String value) {
        this.placeholders.put("%" + name + "%", value);
        return this;
    }

    public MessageBuilder useWebhook(boolean use) {
        this.useWebhook = use;
        return this;
    }

    public MessageBuilder allowMentions(boolean allow) {
        this.allowMentions = allow;
        return this;
    }

    public MessageBuilder allowEveryone(boolean allow) {
        this.allowEveryone = allow;
        return this;
    }

    public MessageBuilder allowHere(boolean allow) {
        this.allowHere = allow;
        return this;
    }

    public void send() {
        JDA jda = MagiBridge.getInstance().getJDA();
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) return;

        TextChannel textChannel = jda.getTextChannelById(this.channel.replace("#", ""));
        if (textChannel == null) {
            MagiBridge.getLogger().error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiBridge.getLogger().error("Replace it with a valid one then reload the plugin!");
            return;
        }

        String message = this.formatType.format(this.placeholders).replaceAll("&([0-9a-fA-FlLkKrR])", "");
        if (this.useWebhook && MagiBridge.getConfig().CHANNELS.USE_WEBHOOKS) {
            message = Utils.replaceEach(placeholders.get("%message%"), this.placeholders);
        } // the whole message should be the exact player message if we're gonna send this via webhooks

        // replace message emotes with the ones from the guild, if they exist
        Guild guild = textChannel.getGuild();
        for (Emote emote : guild.getEmotes()) {
            message = message.replace(":" + emote.getName() + ":", emote.getAsMention());
        }

        if (this.allowMentions) { // Mention discord users if they're mentioned in the message
            List<String> usersMentioned = Arrays.stream(message.split(" "))
                    .filter(word -> word.startsWith("@"))
                    .map(s -> s.substring(1)) // remove the @ from the start of the string
                    .collect(Collectors.toList());

            if (!usersMentioned.isEmpty()) {
                for (String user : usersMentioned) {
                    List<Member> users = textChannel.getGuild().getMembers().stream()
                            .filter(m -> m.getEffectiveName().equalsIgnoreCase(user))
                            .collect(Collectors.toList());

                    List<Role> roles = textChannel.getGuild().getRolesByName(user, true);
                    if (!users.isEmpty()) {
                        message = message.replace("@" + user, users.get(0).getAsMention().replace("!", ""));
                    }
                    if (!roles.isEmpty()) {
                        message = message.replace("@" + user, roles.get(0).getAsMention());
                    }
                }
            }
        }

        if (!this.allowEveryone) {
            message = message.replace("@everyone", "");
        }
        if (!this.allowHere) {
            message = message.replace("@here", "");
        }

        if (message.isEmpty()) return;

        if (this.deleteTime != -1) {
            textChannel.sendMessage(message).queue(m -> m.delete().queueAfter(this.deleteTime, TimeUnit.SECONDS));
        } else if (this.useWebhook && MagiBridge.getConfig().CHANNELS.USE_WEBHOOKS) {
            Webhooking.sendWebhookMessage(
                    FormatType.WEBHOOK_NAME.format(this.placeholders),
                    placeholders.get("%player%"),
                    message,
                    channel);
        } else {
            textChannel.sendMessage(message).queue();
        }
    }

}
