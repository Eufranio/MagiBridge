package com.magitechserver.magibridge.listeners;

import com.arckenver.nations.channel.NationMessageChannel;
import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.FormatType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 13/01/2018.
 */
public class VanillaChatListener {

    @Listener
    public void onMessageChannelEvent(MessageChannelEvent.Chat e, @First Player p) {
        if (!Sponge.getServer().getOnlinePlayers().contains(p) || e.isMessageCancelled()) return;
        if (e.getChannel().isPresent() && Sponge.getPluginManager().isLoaded("nations")) {
            if (e.getChannel().get() instanceof NationMessageChannel) {
                return; // don't want to send private nation messages to Discord
            }
        }

        FormatType format = FormatType.SERVER_TO_DISCORD_FORMAT;
        String channel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;

        if (e.getChannel().isPresent() && Sponge.getPluginManager().isLoaded("nucleus") && MagiBridge.getConfig().CORE.SEND_HELPOP) {
            if (e.getChannel().get().getClass().getName().equals("io.github.nucleuspowered.util.PermissionMessageChannel")) {
                channel = MagiBridge.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL.isEmpty() ?
                        MagiBridge.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL :
                        MagiBridge.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL;
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
        placeholders.put("%player%", p.getName());
        placeholders.put("%message%", e.getFormatter().getBody().toText().toPlain());

        boolean removeEveryone = !p.hasPermission("magibridge.everyone");
        DiscordHandler.sendMessageToDiscord(channel, format, placeholders, removeEveryone, 0);
    }

}
