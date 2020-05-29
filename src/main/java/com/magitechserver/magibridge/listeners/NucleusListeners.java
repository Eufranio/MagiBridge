package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import com.magitechserver.magibridge.util.Utils;
import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;

/**
 * Created by Frani on 17/04/2019.
 */
public class NucleusListeners {

    MagiBridge plugin;
    public NucleusListeners(MagiBridge plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onAfk(NucleusAFKEvent e) {
        if (!e.getTargetEntity().hasPermission("magibridge.chat")) return;
        if (plugin.getConfig().MESSAGES.AFK.AFK_ENABLED) {
            FormatType format = (e instanceof NucleusAFKEvent.GoingAFK) ? FormatType.GOING_AFK :
                    (e instanceof NucleusAFKEvent.ReturningFromAFK) ? FormatType.RETURNING_AFK :
                            null;
            if (format != null) {
                Player p = e.getTargetEntity();
                String channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

                DiscordMessageBuilder.forChannel(channel)
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

    @Listener
    public void helpOpHandler(SendCommandEvent event, @Root Player player) {
        if (plugin.getConfig().CORE.SEND_HELPOP && event.getCommand().toLowerCase().startsWith("helpop")) {
            String channel = plugin.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL.isEmpty() ?
                    plugin.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL :
                    plugin.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL;

            FormatType format = FormatType.HELP_OP_MESSAGE;

            DiscordMessageBuilder.forChannel(channel)
                    .placeholder("prefix", player.getOption("prefix").orElse(""))
                    .placeholder("player", player.getName())
                    .placeholder("message", event.getArguments())
                    .placeholder("topgroup", Utils.getHighestGroup(player))
                    .placeholder("nick", Utils.getNick(player))
                    .format(format)
                    .allowEveryone(player.hasPermission("magibridge.everyone"))
                    .allowMentions(player.hasPermission("magibridge.mention"))
                    .useWebhook(false)
                    .send();
        }
    }

}
