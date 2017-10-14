package com.magitechserver.magibridge.api;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.Webhooking;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Created by Frani on 27/09/2017.
 */
public class MagiBridgeAPI {

    /**
     * Sends a message to the provided {@link TextChannel}
     *
     * @param channel The channel to send the message
     * @param message The message that should be sent
     */
    public void sendMessageToChannel(TextChannel channel, String message) {
        DiscordHandler.sendMessageToChannel(channel.getId(), message);
    }

    /**
     * Sends a message to the provided {@link TextChannel} using a {@link WebhookContent}
     *
     * @param channel The channel to send the message
     * @param content The {@link WebhookContent} containing the avatar URL, webhook name and message
     */
    public void sendMessageToChannel(TextChannel channel, WebhookContent content) {
        Webhooking.sendWebhook(channel.getId(), content);
    }

    /**
     * Replies to a {@link Message}, usefull when interacting with private
     * message channels without having to open a new channel every time
     *
     * @param message The message you want to reply
     * @param reply The reply for that message
     */
    public void replyTo(Message message, String reply) {
        message.getChannel().sendMessage(reply).queue();
    }

    /**
     * Updates the Bot's playing status
     *
     * @param status The game (you lost)
     */
    public void setBotStatus(String status) {
        getJDA().getPresence().setGame(Game.of(status));
    }

    /**
     * Gets the {@link JDA} instance being used by MagiBridge
     *
     * @return the JDA instance
     */
    public JDA getJDA() {
        return MagiBridge.jda;
    }

}
