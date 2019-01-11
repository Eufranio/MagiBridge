package com.magitechserver.magibridge;

import com.magitechserver.magibridge.config.categories.Messages;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.ReplacerUtil;
import flavor.pie.boop.BoopableChannel;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import net.dv8tion.jda.core.entities.Message;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Frani on 24/08/2017.
 */
public class NucleusHandler {

    public static void handle(boolean isStaffChannel, FormatType format, Map<String, String> placeholders, boolean hasAttachment, List<Message.Attachment> attachments) {
        MessageChannel messageChannel;
        if (!isStaffChannel) {
            if (Sponge.getPluginManager().getPlugin("boop").isPresent() && MagiBridge.getConfig().CORE.USE_BOOP) {
                messageChannel = new BoopableChannel(Sponge.getServer().getBroadcastChannel());
            } else {
                messageChannel = Sponge.getServer().getBroadcastChannel();
            }
        } else {
            messageChannel = NucleusAPI.getStaffChatService().get().getStaffChat();
            format = FormatType.DISCORD_TO_SERVER_STAFF_FORMAT;
        }

        String msg = ReplacerUtil.replaceEach(format.get(), placeholders);

        if (messageChannel != null) {
            Text messageAsText = ReplacerUtil.toText(msg);
            Text prefix = Text.of();

            // Prefix enabled
            if (MagiBridge.getConfig().MESSAGES.PREFIX.ENABLED) {
                Messages.PrefixCategory category = MagiBridge.getConfig().MESSAGES.PREFIX;

                URL url;
                try {
                    url = new URL(category.LINK);
                } catch (MalformedURLException e) {
                    MagiBridge.getLogger().error("Invalid prefix URL! Fix it on your config!");
                    return;
                }

                prefix = ReplacerUtil.toText(category.TEXT)
                        .toBuilder().onHover(TextActions.showText(ReplacerUtil.toText(category.HOVER)))
                        .onClick(TextActions.openUrl(url))
                        .build();
            }

            if (hasAttachment) {
                messageChannel.send(Text.of(prefix, messageAsText, attachmentBuilder(attachments)));
            } else {
                messageChannel.send(Text.of(prefix, messageAsText));
            }
        }
    }

    public static Text attachmentBuilder(List<Message.Attachment> attachments) {
        Text.Builder hover = Text.builder("Attachments: ")
                .append(Text.NEW_LINE);
        for (Message.Attachment attachment : attachments) {
            hover.append(Text.of(attachment.getFileName(), Text.NEW_LINE));
        }

        hover.append(Text.of(TextColors.AQUA, "Click to open this attachment!"));

        URL url = null;
        try {
            url = new URL(attachments.get(0).getUrl());
        } catch (MalformedURLException exception) {}

        return Text.builder()
                .append(ReplacerUtil.toText(MagiBridge.getConfig().MESSAGES.ATTACHMENT_NAME))
                .onHover(TextActions.showText(hover.build()))
                .onClick(url != null ? TextActions.openUrl(url) : null)
                .build();
    }

    public static String getNick(Player p) {
        if (!Sponge.getPluginManager().getPlugin("nucleus").isPresent()) return p.getName();
        return NucleusAPI.getNicknameService()
                .map(s -> s.getNickname(p).map(Text::toPlain).orElse(null))
                .orElse(p.getName());
    }

}
