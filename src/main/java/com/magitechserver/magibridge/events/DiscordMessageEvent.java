package com.magitechserver.magibridge.events;

import com.magitechserver.magibridge.chat.MessageBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * An event that is thrown when a message is received or sent from Discord to Server
 * or vice-versa.
 */
public class DiscordMessageEvent extends AbstractEvent implements  Cancellable {

    private boolean cancelled = false;
    private MessageBuilder messageBuilder;

    public DiscordMessageEvent(MessageBuilder builder) {
        this.messageBuilder = builder;
    }

    /**
     * Gets the message type (server->discord or discord->server). Check this before
     * casting the MessageBuilder to the implementation one!
     *
     * @return the type of the MessageBuilder
     */
    public MessageBuilder.Type getMessageType() {
        return messageBuilder.getType();
    }

    /**
     * Gets the MessageBuilder implementation that is going to handle this
     * message. After checking the type of message, you can cast this to the
     * implementation object.
     *
     * (see {@link com.magitechserver.magibridge.chat.ServerMessageBuilder} and {@link com.magitechserver.magibridge.discord.DiscordMessageBuilder}
     *
     * @return the {@link MessageBuilder} that is going to handle this message
     */
    public MessageBuilder getMessageBuilder() {
        return this.messageBuilder;
    }

    @Override
    public Cause getCause() {
        return Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
