package com.magitechserver.listeners;

import com.magitechserver.DiscordHandler;
import com.magitechserver.MagiBridge;
import com.magitechserver.util.Config;
import com.magitechserver.util.GroupUtil;
import com.magitechserver.util.Webhooking;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import nl.riebie.mcclans.channels.AllyMessageChannelImpl;
import nl.riebie.mcclans.channels.ClanMessageChannelImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by Frani on 09/07/2017.
 */
public class SpongeChatListener {

    @Listener(order = Order.LAST)
    public void onSpongeMessage(MessageChannelEvent.Chat e, @Root Player p) {
        if(!Sponge.getServer().getOnlinePlayers().contains(p)) return;
        if(e.getChannel().isPresent()) {
            MagiBridge.logger.error("MessageChannel: " + e.getChannel().get().getClass());
            String content = e.getChannel().get() instanceof StaffChatMessageChannel ? e.getFormatter().getBody().toText().toPlain() : e.getMessage().toPlain();
            String prefix = p.getOption("prefix").orElse("");
            String format = e.getChannel().get() instanceof StaffChatMessageChannel ? MagiBridge.getConfig().getString("messages", "server-to-discord-staff-format") : MagiBridge.getConfig().getString("messages", "server-to-discord-format");
            String message = format
                    .replace("%player%", p.getName())
                    .replace("%prefix%", prefix)
                    .replace("%message%", content)
                    .replace("%topgroup%", GroupUtil.getHighestGroup(p));
            String discordChannel = MagiBridge.getConfig().getString("channel", "nucleus", "global-discord-channel");

            if(Sponge.getPluginManager().isLoaded("mcclans")) {
                if(e.getChannel().get() instanceof AllyMessageChannelImpl || e.getChannel().get() instanceof ClanMessageChannelImpl) return;
            }
            if(e.getChannel().get() instanceof StaffChatMessageChannel) {
                discordChannel = MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel");
            }
            if(Config.useWebhooks()) {
                Webhooking.sendWebhookMessage(MagiBridge.getConfig().getString("messages", "webhook-name")
                        .replace("%prefix%", p.getOption("prefix").isPresent() ? p.getOption("prefix").orElse(null) : "")
                        .replace("%player%", p.getName())
                        .replace("%topgroup%", GroupUtil.getHighestGroup(p)),
                        p.getName(),
                        e.getMessage().toPlain(),
                        discordChannel);
                return;
            }
            DiscordHandler.sendMessageToChannel(discordChannel, message);
        }
    }

}
