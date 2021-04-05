package com.magitechserver.magibridge.discord;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.chat.ServerMessageBuilder;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import com.magitechserver.magibridge.util.Utils;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.spongepowered.api.scheduler.Task;

import java.util.Map;

/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Task.builder().execute(task -> process(e)).submit(MagiBridge.getInstance());
    }

    private void process(MessageReceivedEvent e) {
        ConfigCategory config = MagiBridge.getInstance().getConfig();
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId())) {
            return;
        }
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) ||
                e.getAuthor().isFake() ||
                e.getAuthor().isBot()) {
            if (config.CHANNELS.IGNORE_BOTS)
                return;
        }

        String messageStripped = e.getMessage().getContentStripped();
        if (messageStripped.isEmpty()) return;

        if (config.CORE.CUT_MESSAGES) {
            if (messageStripped.length() > 120) {
                messageStripped = messageStripped.substring(0, 120);
            }
        }
        if (messageStripped.startsWith("```")) {
            messageStripped = messageStripped.substring(0, messageStripped.length() - 3).substring(3);
        }
        if (messageStripped.startsWith("`")) {
            messageStripped = messageStripped.substring(0, messageStripped.length() - 1).substring(1);
        }

        String message = Utils.replaceEach(EmojiParser.parseToAliases(messageStripped), config.REPLACER.REPLACER);

        if (e.getMessage().getAttachments().isEmpty() && message.trim().isEmpty())
            return;

        String channel = e.getChannel().getId();

        if (!MagiBridge.getInstance().getListeningChannels().contains(channel))
            return;

        // Handle console command
        if (config.CHANNELS.CONSOLE_CHANNEL.equals(channel) || message.startsWith(config.CHANNELS.CONSOLE_COMMAND)) {
            Utils.dispatchCommand(e);
            return;
        }

        // Handle player list command
        if (message.equalsIgnoreCase(config.CHANNELS.LIST_COMMAND)) {
            Utils.dispatchList(e.getChannel());
            return;
        }

        Member member = e.getMember();
        boolean isMember = member != null;

        // check if the allowed role is everyone or if the sender has the role
        String colorAllowedRole = config.CHANNELS.COLOR_REQUIRED_ROLE;
        boolean canUseColors = colorAllowedRole.equalsIgnoreCase("everyone") ||
                (isMember && member.getRoles().stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(colorAllowedRole)));

        String name = isMember ? member.getEffectiveName() : "Unknown";

        String noRolePlaceholder = config.MESSAGES.NO_ROLE_PLACEHOLDER;
        String toprole = isMember ? (!member.getRoles().isEmpty() ? member.getRoles().get(0).getName() : noRolePlaceholder) : noRolePlaceholder;

        Map<String, String> colors = config.COLORS.COLORS;
        String toprolecolor = colors.getOrDefault("99AAB5", "&f");

        if (isMember && !member.getRoles().isEmpty()) {
            Role firstRole = member.getRoles().get(0);
            if (firstRole.getColor() != null) {
                String hex = Integer.toHexString(firstRole.getColor().getRGB()).toUpperCase();
                if (hex.length() == 8) hex = hex.substring(2);
                if (colors.containsKey(hex)) {
                    toprolecolor = colors.get(hex);
                }
            }
        }

        ServerMessageBuilder builder = ServerMessageBuilder.create()
                .placeholder("user", name)
                .placeholder("message", message)
                .placeholder("toprole", toprole)
                .placeholder("toprolecolor", toprolecolor)
                .attachments(e.getMessage().getAttachments())
                .colors(canUseColors)
                .format(FormatType.DISCORD_TO_SERVER_FORMAT);

        // Hooks active
        if (config.CHANNELS.USE_NUCLEUS) {
            builder.staff(channel.equals(config.CHANNELS.NUCLEUS.STAFF_CHANNEL)).send();
        } else if (config.CHANNELS.USE_UCHAT) {
            String chatChannel = config.CHANNELS.UCHAT.UCHAT_CHANNELS.get(channel);
            if (chatChannel != null) {
                builder.channel(chatChannel).send();
            }
        } else {
            builder.staff(channel.equals(config.CHANNELS.NUCLEUS.STAFF_CHANNEL)).send();
        }
    }
}
