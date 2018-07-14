package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.GroupUtil;
import com.magitechserver.magibridge.util.ReplacerUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 10/07/2017.
 */
public class SpongeLoginListener {

    @Listener
    public void onLogin(ClientConnectionEvent.Join event, @First Player p) {
        if (!p.hasPlayedBefore()) {
            DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL, FormatType.NEW_PLAYERS_MESSAGE.get().replace("%player%", p.getName()));
            return;
        }

        if (p.hasPermission("magibridge.silentjoin")) {
            MagiBridge.getLogger().warn("The player " + p.getName() + " has the magibridge.silentjoin permission, not sending quit message!");
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", p.getName());
        placeholders.put("%nick%", NucleusHandler.getNick(p));
        placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
        placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));

        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL,
                ReplacerUtil.replaceEach(FormatType.JOIN_MESSAGE.get(), placeholders));
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event, @First Player p) {
        if (p.hasPermission("magibridge.silentquit")) {
            MagiBridge.getLogger().warn("The player " + p.getName() + " has the magibridge.silentquit permission, not sending quit message!");
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", p.getName());
        placeholders.put("%nick%", NucleusHandler.getNick(p));
        placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
        placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));

        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL,
                ReplacerUtil.replaceEach(FormatType.QUIT_MESSAGE.get(), placeholders));
    }

}
