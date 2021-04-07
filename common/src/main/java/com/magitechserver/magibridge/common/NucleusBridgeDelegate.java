package com.magitechserver.magibridge.common;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.UUID;

public interface NucleusBridgeDelegate {

    Text getNickname(UUID player);

    boolean isStaffChatEnabled();

    boolean isDirectedToStaffChannel(MessageChannelEvent.Chat event);

    MessageChannel getStaffChannel();

    Text replacePlaceholders(String string, CommandSource commandSource);

}
