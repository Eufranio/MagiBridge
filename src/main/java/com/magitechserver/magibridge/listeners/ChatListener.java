package com.magitechserver.magibridge.listeners;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.GroupUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 05/07/2017.
 */
public class ChatListener {

    @Listener
    public void onMessage(SendChannelMessageEvent e) {
        if (!e.getSender().hasPermission("magibridge.chat")) return;
        // Tell
        if (e.getChannel() == null) return;
        String channel = getKey(e.getChannel().getName().toLowerCase());
        if (channel == null) return;

        Player p = e.getSender() instanceof Player ? (Player) e.getSender() : null;

        if (p != null &&
                MagiBridge.getConfig().CORE.HIDE_VANISHED_CHAT &&
                p.get(Keys.VANISH).orElse(false))
            return;
        FormatType format = FormatType.SERVER_TO_DISCORD_FORMAT;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%prefix%", e.getSender().getOption("prefix").orElse(""));
        placeholders.put("%player%", e.getSender().getName());
        placeholders.put("%message%", e.getMessage().toPlain());
        placeholders.put("%topgroup%", p != null ? GroupUtil.getHighestGroup(p) : "");

        if (p != null && Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
            placeholders.put("%nick%", NucleusHandler.getNick(p));
        }

        boolean removeEveryone = !e.getSender().hasPermission("magibridge.everyone");

        DiscordHandler.sendMessageToDiscord(channel, format, placeholders, removeEveryone, 0, e.getSender().hasPermission("magibridge.mention"));
    }

    private String getKey(String value) {
        for (Map.Entry<String, String> values : MagiBridge.getConfig().CHANNELS.UCHAT.UCHAT_CHANNELS.entrySet()) {
            if (value.equals(values.getValue().toLowerCase())) {
                return values.getKey();
            }
        }
        return null;
    }
}

