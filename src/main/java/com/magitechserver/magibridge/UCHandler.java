package com.magitechserver.magibridge;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
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

    public static void handle(String channel, String format, Map<String, String> placeholders, boolean hasAttachment, List<Message.Attachment> attachments) {
        if(MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(channel) != null) {
            format = MagiBridge.getConfig().getMap("channel", "ultimatechat", "format-overrides").get(channel);
        }

        UCChannel uc = UCHandler.getChannelByCaseInsensitiveName(channel);

        if(uc != null) {

            Text message = TextSerializers.FORMATTING_CODE.deserialize(ReplacerUtil.replaceEach(format, placeholders));
            Text attachment = Text.of();

            // Prefix enabled
            Text prefix = Text.of();
            if(MagiBridge.getConfig().getBool("messages", "prefix", "enabled")) {
                Map<String, String> map = MagiBridge.getConfig().getMap("messages", "prefix");
                Text.Builder text = TextSerializers.FORMATTING_CODE.deserialize(map.get("text")).toBuilder();
                Text hover = TextSerializers.FORMATTING_CODE.deserialize(map.get("hover"));

                URL url;
                try {
                    url = new URL(map.get("link"));
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
