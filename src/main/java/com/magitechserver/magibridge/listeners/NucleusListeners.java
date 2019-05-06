package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.MessageBuilder;
import com.magitechserver.magibridge.util.Utils;
import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

/**
 * Created by Frani on 17/04/2019.
 */
public class NucleusListeners {

    @Listener
    public void onAfk(NucleusAFKEvent e) {
        if (!e.getTargetEntity().hasPermission("magibridge.chat")) return;
        if (MagiBridge.getConfig().MESSAGES.AFK.AFK_ENABLED) {
            FormatType format = (e instanceof NucleusAFKEvent.GoingAFK) ? FormatType.GOING_AFK :
                    (e instanceof NucleusAFKEvent.ReturningFromAFK) ? FormatType.RETURNING_AFK :
                            null;
            if (format != null) {
                Player p = e.getTargetEntity();
                String channel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;

                MessageBuilder.forChannel(channel)
                        .placeholder("prefix", p.getOption("prefix").orElse(""))
                        .placeholder("player", p.getName())
                        .placeholder("topgroup", Utils.getHighestGroup(p))
                        .placeholder("nick", Utils.getNick(p))
                        .useWebhook(false)
                        .format(format)
                        .send();
            }
        }
    }

}
