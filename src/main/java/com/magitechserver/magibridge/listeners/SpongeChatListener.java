package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.Config;
import com.magitechserver.magibridge.util.GroupUtil;
import com.magitechserver.magibridge.util.Webhooking;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import nl.riebie.mcclans.channels.AllyMessageChannelImpl;
import nl.riebie.mcclans.channels.ClanMessageChannelImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Frani on 09/07/2017.
 */
public class SpongeChatListener {

    @Listener(order = Order.LAST)
    public void onSpongeMessage(MessageChannelEvent.Chat e, @Root Player p) {
        if(!Sponge.getServer().getOnlinePlayers().contains(p)) return;
        if(e.getChannel().isPresent()) {
            MessageChannel staffChannel = NucleusAPI.getStaffChatService().get().getStaffChat();

            if(Sponge.getPluginManager().isLoaded("mcclans")) {
                if(e.getChannel().get() instanceof AllyMessageChannelImpl || e.getChannel().get() instanceof ClanMessageChannelImpl) return;
            }

            boolean isStaffMessage = e.getChannel().get().getClass().equals(staffChannel.getClass());

            String[] nick = new String[1];
            NucleusAPI.getNicknameService().ifPresent(s -> s.getNickname(p).ifPresent(n -> nick[0] = n.toPlain()));

            String message = e.getFormatter().getBody().toText().toPlain();
            String channel = isStaffMessage ? MagiBridge.getConfig().getString("channel", "nucleus", "staff-discord-channel") : MagiBridge.getConfig().getString("channel", "nucleus", "global-discord-channel");
            String format = e.getChannel().get().getClass().equals(staffChannel.getClass()) ? "server-to-discord-staff-format" : "server-to-discord-format";
            Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
                placeholders.put("%player%", p.getName());
                placeholders.put("%message%", message);
                placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));
                placeholders.put("%nick%", nick[0] != null ? nick[0] : "");
            boolean removeEveryone = !p.hasPermission("magibridge.everyone");

            DiscordHandler.sendMessageToDiscord(message, channel, format, placeholders, removeEveryone, 0);
        }
    }

}
