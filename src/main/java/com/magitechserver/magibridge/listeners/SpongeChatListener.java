package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.GroupUtil;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import nl.riebie.mcclans.channels.AllyMessageChannelImpl;
import nl.riebie.mcclans.channels.ClanMessageChannelImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
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
        if (!Sponge.getServer().getOnlinePlayers().contains(p) || e.isMessageCancelled()) return;
        if (e.getChannel().isPresent()) {
            if (MagiBridge.getConfig().CORE.HIDE_VANISHED_CHAT && p.get(Keys.VANISH).orElse(false)) return;
            if (!NucleusAPI.getStaffChatService().isPresent()) {
                MagiBridge.logger.error("The staff chat module is disabled in the Nucleus config! Please enable it!");
            }
            MessageChannel staffChannel = NucleusAPI.getStaffChatService().get().getStaffChat();

            if (Sponge.getPluginManager().isLoaded("mcclans") && e.getChannel().get() instanceof AllyMessageChannelImpl || e.getChannel().get() instanceof ClanMessageChannelImpl) {
                return;
            }

            boolean isStaffMessage = e.getChannel().get().getClass().equals(staffChannel.getClass());

            String channel = isStaffMessage ? MagiBridge.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL : MagiBridge.getConfig().CHANNELS.NUCLEUS.GLOBAL_CHANNEL;
            FormatType format = e.getChannel().get().getClass().equals(staffChannel.getClass()) ? FormatType.SERVER_TO_DISCORD_STAFF_FORMAT : FormatType.SERVER_TO_DISCORD_FORMAT;
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
            placeholders.put("%player%", p.getName());
            placeholders.put("%message%", e.getFormatter().getBody().toText().toPlain());
            placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));
            placeholders.put("%nick%", NucleusHandler.getNick(p));
            boolean removeEveryone = !p.hasPermission("magibridge.everyone");

            DiscordHandler.sendMessageToDiscord(channel, format, placeholders, removeEveryone, 0);
        }
    }

    @Listener
    public void onAfk(NucleusAFKEvent e) {
        if (MagiBridge.getConfig().MESSAGES.AFK.AFK_ENABLED) {
            FormatType format = e instanceof NucleusAFKEvent.GoingAFK ? FormatType.GOING_AFK : e instanceof NucleusAFKEvent.ReturningFromAFK ? FormatType.RETURNING_AFK : null;
            if (format != null) {
                Player p = e.getTargetEntity();
                String channel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
                placeholders.put("%player%", p.getName());
                placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));
                placeholders.put("%nick%", NucleusHandler.getNick(p));
                DiscordHandler.sendMessageToDiscord(channel, format, placeholders, false, 0, false);
            }
        }
    }

}
