package com.magitechserver.magibridge.api;

import net.dv8tion.jda.core.entities.*;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

import java.util.List;

/**
 * Created by Frani on 27/09/2017.
 */

public interface DiscordEvent extends Cancellable, Event {

    /**
     * Gets the {@link Guild} that this event was fired
     *
     * @return The guild of the event, if available
     */
    Guild getGuild();

    interface MessageEvent extends DiscordEvent {
        /**
         * Gets the {@link Message} of this event
         *
         * @return The message object
         */
        Message getMessage();

        /**
         * Gets the raw message of this event
         *
         * @return The raw message
         */
        String getRawMessage();

        /**
         * Gets the {@link User} that sent the message
         *
         * @return The user that sent the message
         */
        User getUser();

        /**
         * Gets the {@link Member} that sent the message
         *
         * @return The member that sent the message. Members are per guild, Users are global
         */
        Member getMember();

        /**
         * Gets an List of {@link net.dv8tion.jda.core.entities.Message.Attachment} that this message contains
         *
         * @return The list of attachments, empty if there is no attachment
         */
        List<Message.Attachment> getAttachments();

        /**
         * Gets the {@link MessageChannel} that the message was sent
         *
         * @return The channel, if available
         */
        MessageChannel getChannel();

    }
}