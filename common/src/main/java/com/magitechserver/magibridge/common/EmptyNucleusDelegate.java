package com.magitechserver.magibridge.common;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

public class EmptyNucleusDelegate implements NucleusBridgeDelegate {

    @Override
    public Text getNickname(UUID player) {
        return Sponge.getServer().getPlayer(player)
                .map(p -> (Text) Text.of(p.getName()))
                .orElse(Text.EMPTY);
    }

    @Override
    public boolean isStaffChatEnabled() {
        return false;
    }

    @Override
    public boolean isDirectedToStaffChannel(MessageChannelEvent.Chat event) {
        return false;
    }

    @Override
    public MessageChannel getStaffChannel() {
        return MessageChannel.TO_ALL;
    }

    @Override
    public Text replacePlaceholders(String string, CommandSource commandSource) {
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }
}