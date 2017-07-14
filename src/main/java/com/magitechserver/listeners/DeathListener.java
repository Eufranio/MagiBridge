package com.magitechserver.listeners;

import com.magitechserver.DiscordHandler;
import com.magitechserver.MagiBridge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;

/**
 * Created by Frani on 14/07/2017.
 */
public class DeathListener {

    @Listener
    public void onDeath(DestructEntityEvent.Death event) {
        if(event.getTargetEntity() instanceof Player) {
            String deathMessage = event.getMessage().toPlain();
            String msg = MagiBridge.getConfig().getString("messages", "death-message")
                    .replace("%player%", ((Player) event.getTargetEntity()).getName())
                    .replace("%deathmessage%", deathMessage);
            DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"), msg);
        }
    }
}
