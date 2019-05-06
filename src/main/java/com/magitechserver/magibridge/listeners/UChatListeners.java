package com.magitechserver.magibridge.listeners;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.MessageBuilder;
import com.magitechserver.magibridge.util.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import java.util.Map;

/**
 * Created by Frani on 05/07/2017.
 */
public class UChatListeners {

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
        MessageBuilder.forChannel(channel)
                .placeholder("prefix", e.getSender().getOption("prefix").orElse(""))
                .placeholder("player", e.getSender().getName())
                .placeholder("message", e.getMessage().toPlain())
                .placeholder("topgroup", p != null ? Utils.getHighestGroup(p) : "")
                .placeholder("nick", p != null ? Utils.getNick(p) : "")
                .format(format)
                .allowEveryone(e.getSender().hasPermission("magibridge.everyone"))
                .allowMentions(e.getSender().hasPermission("magibridge.mention"))
                .send();
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

