package com.magitechserver.magibridge.discord;

import com.google.common.collect.Maps;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.chat.MessageBuilder;
import com.magitechserver.magibridge.chat.ServerMessageBuilder;
import com.magitechserver.magibridge.common.NucleusBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.events.DiscordMessageEvent;
import com.magitechserver.magibridge.util.Utils;
import me.rojo8399.placeholderapi.PlaceholderService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Frani on 17/04/2019.
 */
public class DiscordMessageBuilder implements MessageBuilder {

    private String channel;
    private FormatType formatType;
    private boolean useWebhook = true;
    private boolean allowMentions = true;
    private boolean allowEveryone = false;
    private boolean allowHere = false;
    private boolean queue = true;
    private int deleteTime = -1;
    private Map<String, String> placeholders = Maps.newHashMap();
    private char[] bannedCharacters = {(char) 0x202E, (char) 0x202D, (char) 0x202A, (char) 0x202B, (char) 0x202C};

    public static DiscordMessageBuilder forChannel(String channel) {
        return new DiscordMessageBuilder(channel);
    }

    private DiscordMessageBuilder(String channel) {
        this.channel = channel;
    }

    public DiscordMessageBuilder format(FormatType format) {
        this.formatType = format;
        return this;
    }

    public DiscordMessageBuilder placeholder(String name, String value) {
        this.placeholders.put("%" + name + "%", value);
        return this;
    }

    public DiscordMessageBuilder placeholders(Map<String, String> placeholders) {
        placeholders.forEach(this::placeholder);
        return this;
    }

    public DiscordMessageBuilder useWebhook(boolean use) {
        this.useWebhook = use;
        return this;
    }

    public DiscordMessageBuilder allowMentions(boolean allow) {
        this.allowMentions = allow;
        return this;
    }

    public DiscordMessageBuilder allowEveryone(boolean allow) {
        this.allowEveryone = allow;
        return this;
    }

    public DiscordMessageBuilder allowHere(boolean allow) {
        this.allowHere = allow;
        return this;
    }

    public DiscordMessageBuilder queue(boolean queue) {
        this.queue = queue;
        return this;
    }

    @Override
    public Type getType() {
        return Type.SERVER_TO_DISCORD;
    }

    public String get() {
        JDA jda = MagiBridge.getInstance().getJDA();
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) return null;

        TextChannel textChannel = jda.getTextChannelById(this.channel.replace("#", ""));
        if (textChannel == null) {
            MagiBridge.getLogger().error("The channel " + channel + " defined in the config isn't a valid Discord Channel ID!");
            MagiBridge.getLogger().error("Replace it with a valid one and then reload the plugin!");
            return null;
        }

        if (Sponge.getEventManager().post(new DiscordMessageEvent(this)))
            return null;

        // replace special chars only on the main message content, allowing formats to use them if needed
        placeholders.computeIfPresent("%message%", (placeholder, value) -> {
            for (char ch : bannedCharacters) { // Remove special chars that discord removes anyway, preventing unwanted @everyone and @here
                value = value.replace(Character.toString(ch), "");
            }

            // also replace the words in the mc-to-discord-replacer
            return Utils.replaceEach(value, MagiBridge.getInstance().getConfig().REPLACER.mcToDiscordReplacer);
        });

        CommandSource source = Sponge.getServer().getConsole();
        if (placeholders.get("%player%") != null) {
            source = Sponge.getServer().getPlayer(placeholders.get("%player%")).get();
        }

        // apply placeholders
        String message = this.parsePlaceholders(this.formatType, source);

        // don't apply placeholders for the raw message
        if (this.useWebhook && MagiBridge.getInstance().getConfig().CHANNELS.USE_WEBHOOKS) {
            message = placeholders.get("%message%")
                    .replaceAll(ServerMessageBuilder.STRIP_COLOR_PATTERN.pattern(), "");
        } // the whole message should be the raw message if we're gonna send this via webhooks

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
                    if (user.trim().isEmpty()) continue;

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

        if(!this.allowEveryone)
            message = message.replace("@everyone", "everyone");

        if(!this.allowHere)
            message = message.replace("@here", "here");

        if (message.isEmpty()) return null;

        return message;
    }

    public void send() {
        String message = this.get();
        if (message == null)
            return;

        TextChannel textChannel = MagiBridge.getInstance()
                .getJDA()
                .getTextChannelById(this.channel.replace("#", ""));

        if (this.deleteTime != -1) {
            if (this.queue)
                textChannel.sendMessage(message).queue(m -> m.delete().queueAfter(this.deleteTime, TimeUnit.SECONDS));
            else
                textChannel.sendMessage(message).complete().delete().queueAfter(this.deleteTime, TimeUnit.SECONDS);
        } else if (this.useWebhook && MagiBridge.getInstance().getConfig().CHANNELS.USE_WEBHOOKS) {
            Player player = Sponge.getServer().getPlayer(placeholders.get("%player%")).get();
            String placeholderName = this.parsePlaceholders(FormatType.WEBHOOK_NAME, player);

            WebhookManager.sendWebhookMessage(
                    placeholderName,
                    player.getName(),
                    message,
                    channel
            );
        } else {
            if (this.queue)
                textChannel.sendMessage(message).queue();
            else
                textChannel.sendMessage(message).complete();
        }
    }

    String parsePlaceholders(FormatType format, CommandSource source) {
        // apply magibridge placeholders
        String rawMessage = this.placeholders.remove("%message%");
        if (rawMessage != null)
            rawMessage = rawMessage.replace("%message%", "");

        String message = format.format(this.placeholders);

        // apply PlaceholderAPI placeholders
        if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
            PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
            message = service.replacePlaceholders(message, source, null).toPlain();
        }

        // apply Nucleus placeholders
        if (Sponge.getPluginManager().isLoaded("nucleus")) {
            message = NucleusBridge.getInstance().replacePlaceholders(message, source).toPlain();
        }

        // PlaceholderAPI adds {} around unresolved placeholders
        if (rawMessage != null) {
            message = message.replace("{message}", rawMessage)
                    .replace("%message%", rawMessage);
        }

        this.placeholder("message", rawMessage);
        message = message.replaceAll(ServerMessageBuilder.STRIP_COLOR_PATTERN.pattern(), "");
        return message;
    }

}
