package com.magitechserver.magibridge.chat;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import com.google.common.collect.Maps;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.common.NucleusBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import com.magitechserver.magibridge.config.categories.Messages;
import com.magitechserver.magibridge.events.DiscordMessageEvent;
import com.magitechserver.magibridge.util.TextHelper;
import com.magitechserver.magibridge.util.Utils;
import flavor.pie.boop.BoopableChannel;
import net.dv8tion.jda.api.entities.Message;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Frani on 26/04/2019.
 */
public class ServerMessageBuilder implements MessageBuilder {

    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)[§&][0-9A-FK-OR]");

    private boolean staff = false;
    private boolean colors = true;
    private FormatType format;
    private Map<String, String> placeholders = Maps.newHashMap();
    private List<Message.Attachment> attachments;
    private String channel;

    public static ServerMessageBuilder create() {
        return new ServerMessageBuilder();
    }

    public ServerMessageBuilder staff(boolean staff) {
        this.staff = staff;
        return this;
    }

    public ServerMessageBuilder format(FormatType format) {
        this.format = format;
        return this;
    }

    public ServerMessageBuilder placeholder(String name, String value) {
        this.placeholders.put("%" + name + "%", value);
        return this;
    }

    public ServerMessageBuilder attachments(List<Message.Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public ServerMessageBuilder channel(String channel) {
        this.channel = channel;
        return this;
    }

    public ServerMessageBuilder colors(boolean colors) {
        this.colors = colors;
        return this;
    }

    public Type getType() {
        return Type.DISCORD_TO_SERVER;
    }

    public void send() {
        if (Sponge.getEventManager().post(new DiscordMessageEvent(this)))
            return;

        if (!this.colors) {
            this.placeholders.compute("%message%", (k, v) -> v != null ? STRIP_COLOR_PATTERN.matcher(v).replaceAll("") : v);
        }

        boolean isUchat = this.channel != null;
        ConfigCategory config = MagiBridge.getInstance().getConfig();

        Text prefix = Text.of();
        // Prefix enabled
        if (config.MESSAGES.PREFIX.ENABLED) {
            Messages.PrefixCategory category = config.MESSAGES.PREFIX;
            try {
                prefix = Utils.toText(category.TEXT)
                        .toBuilder()
                        .onHover(TextActions.showText(Utils.toText(category.HOVER)))
                        .onClick(TextActions.openUrl(new URL(category.LINK)))
                        .build();
            } catch (MalformedURLException e) {
                MagiBridge.getLogger().error(category.LINK + " is an invalid prefix URL! Fix it on your config!");
                return;
            }
        }

        Text attachment = Text.of();
        // Message contains attachments
        if (this.attachments != null && !this.attachments.isEmpty()) {
            Text.Builder hover = Text.builder("Attachments: ").append(Text.NEW_LINE);
            for (Message.Attachment att : this.attachments) {
                hover.append(Text.of(att.getFileName(), Text.NEW_LINE));
            }

            hover.append(Text.of(TextColors.AQUA, "Click to open this attachment!"));

            URL url = null;
            try {
                url = new URL(attachments.get(0).getUrl());
            } catch (MalformedURLException exception) {}

            attachment = Text.builder()
                    .append(Utils.toText(config.MESSAGES.ATTACHMENT_NAME))
                    .onHover(TextActions.showText(hover.build()))
                    .onClick(url != null ? TextActions.openUrl(url) : null)
                    .build();
        }

        // implementation specific sending code

        if (isUchat && config.CHANNELS.USE_UCHAT && Sponge.getPluginManager().isLoaded("ultimatechat")) {
            String rawFormat = config.CHANNELS.UCHAT.UCHAT_OVERRIDES.getOrDefault(channel, this.format.get());

            UCChannel chatChannel = UChat.get().getAPI().getChannels().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(channel))
                    .findFirst().orElse(null);
            if (chatChannel == null) {
                MagiBridge.getLogger().error("The channel " + channel + " specified in the config doesn't exist in-game!");
                return;
            }

            Text text = Text.of(prefix, Utils.toText(Utils.replaceEach(rawFormat, this.placeholders)), attachment);
            text = TextHelper.replaceLink(text);
            chatChannel.sendMessage(Sponge.getServer().getConsole(), text, true);
        } else if (config.CHANNELS.USE_NUCLEUS && Sponge.getPluginManager().isLoaded("nucleus")) {
            MessageChannel messageChannel;
            if (!this.staff) {
                if (Sponge.getPluginManager().getPlugin("boop").isPresent() && config.CORE.USE_BOOP) {
                    messageChannel = new BoopableChannel(MessageChannel.TO_ALL);
                } else {
                    messageChannel = MessageChannel.TO_ALL;
                }
            } else {
                messageChannel = NucleusBridge.getInstance().getStaffChannel();
                this.format = FormatType.DISCORD_TO_SERVER_STAFF_FORMAT;
            }

            Text toSend = Text.of(prefix, Utils.toText(this.format.format(this.placeholders)), attachment);
            toSend = TextHelper.replaceLink(toSend);
            messageChannel.send(toSend);
        } else {
            MessageChannel messageChannel = Sponge.getPluginManager().getPlugin("boop").isPresent() ?
                    new BoopableChannel(MessageChannel.TO_ALL) :
                    MessageChannel.TO_ALL;

            Text toSend = Text.of(prefix, Utils.toText(this.format.format(this.placeholders)), attachment);
            toSend = TextHelper.replaceLink(toSend);
            messageChannel.send(toSend);
        }
    }

}
