package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.FormatType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 13/01/2018.
 */
public class VanillaChatListener {

    @Listener
    public void onMessageChannelEvent(MessageChannelEvent.Chat e, @Root Player p) {
        if (!Sponge.getServer().getOnlinePlayers().contains(p) || e.isMessageCancelled()) return;
        FormatType format = FormatType.SERVER_TO_DISCORD_FORMAT;
        String channel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
        placeholders.put("%player%", p.getName());
        placeholders.put("%message%", e.getFormatter().getBody().toText().toPlain());

        boolean removeEveryone = !p.hasPermission("magibridge.everyone");
        DiscordHandler.sendMessageToDiscord(channel, format, placeholders, removeEveryone, 0);
    }

}