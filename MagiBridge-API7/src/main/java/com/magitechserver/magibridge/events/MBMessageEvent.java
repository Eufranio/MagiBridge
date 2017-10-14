package com.magitechserver.magibridge.events;

import com.magitechserver.magibridge.api.DiscordEvent;
import net.dv8tion.jda.core.entities.*;
import org.spongepowered.api.event.cause.Cause;

import java.util.List;

/**
 * Created by Frani on 27/09/2017.
 */
public class MBMessageEvent extends MBDiscordEvent implements DiscordEvent.MessageEvent {

    private Message message;

    public MBMessageEvent(Guild guild, Cause cause, Message message) {
        super(guild, cause);
        this.message = message;
    }

    @Override
    public Member getMember() {
        return message.getMember();
    }

    @Override
    public MessageChannel getChannel() {
        return message.getChannel();
    }

    @Override
    public List<Message.Attachment> getAttachments() {
        return message.getAttachments();
    }

    @Override
    public User getUser() {
        return message.getAuthor();
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public String getRawMessage() {
        return message.getRawContent();
    }

}
