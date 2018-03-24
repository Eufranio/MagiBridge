package com.magitechserver.magibridge;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import com.magitechserver.magibridge.config.categories.Messages;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.ReplacerUtil;
import net.dv8tion.jda.core.entities.Message;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Frani on 05/07/2017.
 */
public class UCHandler {

    // Channels can be upper/lower case
    public static UCChannel getChannelByCaseInsensitiveName(String name) {
        for (UCChannel channel : UChat.get().getAPI().getChannels())
            if (channel.getName().equalsIgnoreCase(name)) {
                return channel;
            }
        return null;
    }

    public static void handle(String channel, FormatType format, Map<String, String> placeholders, boolean hasAttachment, List<Message.Attachment> attachments) {
        String rawFormat = format.get();
        if (MagiBridge.getConfig().CHANNELS.UCHAT.UCHAT_OVERRIDES.get(channel) != null) {
            rawFormat = MagiBridge.getConfig().CHANNELS.UCHAT.UCHAT_OVERRIDES.get(channel);
        }

        UCChannel uc = UCHandler.getChannelByCaseInsensitiveName(channel);

        if (uc != null) {

            Text message = TextSerializers.FORMATTING_CODE.deserialize(ReplacerUtil.replaceEach(rawFormat, placeholders));
            Text attachment = Text.of();

            // Prefix enabled
            Text prefix = Text.of();
            if (MagiBridge.getConfig().MESSAGES.PREFIX.ENABLED) {
                Messages.PrefixCategory category = MagiBridge.getConfig().MESSAGES.PREFIX;
                Text.Builder text = TextSerializers.FORMATTING_CODE.deserialize(category.TEXT).toBuilder();
                Text hover = TextSerializers.FORMATTING_CODE.deserialize(category.HOVER);

                URL url;
                try {
                    url = new URL(category.LINK);
                } catch (MalformedURLException e) {
                    MagiBridge.logger.error("Invalid prefix URL!");
                    return;
                }

                prefix = text.onHover(TextActions.showText(hover))
                        .onClick(TextActions.openUrl(url))
                        .build();
            }

            if (hasAttachment) {
                attachment = NucleusHandler.attachmentBuilder(attachments);
            }

            uc.sendMessage(Sponge.getServer().getConsole(), prefix.concat(message).concat(attachment), true);
        }
    }

}
