package com.magitechserver.listeners;

import com.magitechserver.DiscordHandler;
import com.magitechserver.MagiBridge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * Created by Frani on 10/07/2017.
 */
public class SpongeLoginListener {

    @Listener
    public void onLogin(ClientConnectionEvent.Join event, @First Player p) {
        String joinMsg = MagiBridge.getConfig().getString("messages", "player-join-message").replace("%player%", p.getName());
        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"), joinMsg);
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event, @First Player p) {
        String quitMsg = MagiBridge.getConfig().getString("messages", "player-quit-message").replace("%player%", p.getName());
        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"), quitMsg);
    }

}
