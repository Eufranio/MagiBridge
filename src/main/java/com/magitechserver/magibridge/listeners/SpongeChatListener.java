package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.Config;
import com.magitechserver.magibridge.util.GroupUtil;
import com.magitechserver.magibridge.util.Webhooking;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import nl.riebie.mcclans.channels.AllyMessageChannelImpl;
import nl.riebie.mcclans.channels.ClanMessageChannelImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;


/**
 * Created by Frani on 09/07/2017.
 */
public class SpongeChatListener {

    @Listener(order = Order.LAST)
    public void onSpongeMessage(MessageChannelEvent.Chat e, @Root Player p) {
        if(!Sponge.getServer().getOnlinePlayers().contains(p)) return;
        if(e.getChannel().isPresent()) {
            String[] nick = new String[1];
            String content = e.getFormatter().getBody().toText().toPlain()
                    .replace("@everyone", p.hasPermission("magibridge.everyone") ? "@everyone" : "")
                    .replace("@here", p.hasPermission("magibridge.everyone") ? "@here" : "");
            String prefix = p.getOption("prefix").orElse("");
            NucleusAPI.getNicknameService().ifPresent(s -> s.getNickname(p).ifPresent(n -> nick[0] = n.toPlain()));
            String format = e.getChannel().get() instanceof StaffChatMessageChannel ? MagiBridge.getConfig().getString("messages", "server-to-discord-staff-format") : MagiBridge.getConfig().getString("messages", "server-to-discord-format");
            String message = format
                    .replace("%player%", p.getName())
                    .replace("%prefix%", prefix)
                    .replace("%message%", content)
                    .replace("%nick%", nick[0] != null ? nick[0] : "")
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
                        .replace("%prefix%", p.getOption("prefix").orElse(""))
                        .replace("%nick%", nick[0] != null ? nick[0] : "")
                        .replace("%player%", p.getName())
                        .replace("%topgroup%", GroupUtil.getHighestGroup(p)),
                        p.getName(),
                        content,
                        discordChannel);
                return;
            }
            DiscordHandler.sendMessageToChannel(discordChannel, message);
        }
    }

}
