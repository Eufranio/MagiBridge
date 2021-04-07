package com.magitechserver.magibridge.listeners;

import com.arckenver.nations.channel.NationMessageChannel;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.common.NucleusBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import com.magitechserver.magibridge.util.Utils;
import io.github.aquerr.eaglefactions.api.messaging.chat.AllianceMessageChannel;
import io.github.aquerr.eaglefactions.api.messaging.chat.FactionMessageChannel;
import nl.riebie.mcclans.channels.AllyMessageChannelImpl;
import nl.riebie.mcclans.channels.ClanMessageChannelImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.type.FixedMessageChannel;

/**
 * Created by Frani on 17/04/2019.
 */
public class VanillaListeners {

    MagiBridge plugin;
    public VanillaListeners(MagiBridge plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onAdvancement(AdvancementEvent.Grant event, @Getter("getTargetEntity") Player p) {
        if (!p.hasPermission("magibridge.chat")) return;
        if (!plugin.getConfig().CORE.ADVANCEMENT_MESSAGES_ENABLED) return;
        if (event.getAdvancement().getName().startsWith("recipes")) return;

        String channel = plugin.getConfig().CHANNELS.ADVANCEMENT_MESSAGES_CHANNEL;
        if (channel.isEmpty())
            channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

        DiscordMessageBuilder.forChannel(channel)
                .placeholders(Utils.playerPlaceholders(p))
                .placeholder("advancement", event.getAdvancement().getName())
                .useWebhook(false)
                .format(FormatType.ADVANCEMENT_MESSAGE)
                .send();
    }

    @Listener
    public void onDeath(DestructEntityEvent.Death event) {
        if (event.getTargetEntity() instanceof Player && plugin.getConfig().CORE.DEATH_MESSAGES_ENABLED) {
            if (event.getMessage().toPlain().isEmpty()) return;

            Player p = (Player) event.getTargetEntity();
            if (!p.hasPermission("magibridge.chat")) return;

            String channel = plugin.getConfig().CHANNELS.DEATH_MESSAGES_CHANNEL;
            if (channel.isEmpty())
                channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

            DiscordMessageBuilder.forChannel(channel)
                    .placeholders(Utils.playerPlaceholders(p))
                    .placeholder("deathmessage", event.getMessage().toPlain())
                    .useWebhook(false)
                    .format(FormatType.DEATH_MESSAGE)
                    .send();
        }
    }

    @Listener(order = Order.LAST)
    public void onSpongeMessage(MessageChannelEvent.Chat e, @Root Player p) {
        if (!plugin.enableVanillaChat()) return;
        if (!p.hasPermission("magibridge.chat")) return;
        if (!Sponge.getServer().getOnlinePlayers().contains(p) || e.isMessageCancelled()) return;

        // workaround for preventing sending helpop messages to the normal channels.
        // for some reason Nucleus triggers a new chat event for helpop messages.
        if (e.getCause().first(PluginContainer.class)
                .filter(container -> container.getId().contains("nucleus"))
                .isPresent() &&
            !NucleusBridge.getInstance().isDirectedToStaffChannel(e))
            return;

        if (plugin.getConfig().CORE.HIDE_VANISHED_CHAT && p.get(Keys.VANISH).orElse(false)) return;

        FormatType format = FormatType.SERVER_TO_DISCORD_FORMAT;
        String channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

        if (e.getChannel().isPresent()) {
            MessageChannel messageChannel = e.getChannel().get();

            boolean nationsLoaded = Sponge.getPluginManager().isLoaded("nations") ||
                    Sponge.getPluginManager().isLoaded("nations-updated");
            if (nationsLoaded && messageChannel instanceof NationMessageChannel) {
                return; // don't want to send private nation messages to Discord
            }

            if (Sponge.getPluginManager().isLoaded("eaglefactions")) {
                try {
                    // EagleFactions is loaded with the new version, and we don't want to send private faction messages to Discord
                    Class.forName("io.github.aquerr.eaglefactions.api.messaging.chat.AllianceMessageChannel");
                    if (messageChannel instanceof AllianceMessageChannel || messageChannel instanceof FactionMessageChannel)
                        return;
                } catch (Exception ex) {}
            }
            
            if (Sponge.getPluginManager().isLoaded("mcclans") &&
                    (messageChannel instanceof AllyMessageChannelImpl ||
                            messageChannel instanceof ClanMessageChannelImpl)) {
                return; // don't want to send clan messages to Discord
            }
        }

        if (plugin.getConfig().CHANNELS.USE_NUCLEUS && Sponge.getPluginManager().isLoaded("nucleus") && e.getChannel().isPresent()) {
            MessageChannel messageChannel = e.getChannel().get();

            if (!NucleusBridge.getInstance().isStaffChatEnabled()) {
                MagiBridge.getLogger().error("The staff chat module is disabled in the Nucleus config! Please enable it!");
                return;
            }

            boolean isStaffMessage = NucleusBridge.getInstance().isDirectedToStaffChannel(e);
            if (!isStaffMessage && messageChannel instanceof FixedMessageChannel) {
                if (!messageChannel.getMembers().containsAll(Sponge.getServer().getBroadcastChannel().getMembers())) {
                    return; // probably a non-global channel, griefprevention uses this on it's mute sytem
                }
            }

            channel = isStaffMessage ?
                    plugin.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL :
                    plugin.getConfig().CHANNELS.NUCLEUS.GLOBAL_CHANNEL;
            if (channel.isEmpty()) return;

            /*if (!ignoreRoot && !(e.getSource() instanceof Player)) {
                return; // if it's not a helpop message, we don't care about other sources
            }*/

            format = isStaffMessage ?
                    FormatType.SERVER_TO_DISCORD_STAFF_FORMAT :
                    FormatType.SERVER_TO_DISCORD_FORMAT;

        }

        DiscordMessageBuilder.forChannel(channel)
                .placeholders(Utils.playerPlaceholders(p))
                .placeholder("message", e.getFormatter().getBody().toText().toPlain())
                .format(format)
                .allowEveryone(p.hasPermission("magibridge.everyone"))
                .allowHere(p.hasPermission("magibridge.here"))
                .allowMentions(p.hasPermission("magibridge.mention"))
                .send();

    }

    @Listener
    public void onLogin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player p) {
        if (!p.hasPermission("magibridge.chat")) return;

        if (!p.hasPlayedBefore()) {
            String channel = plugin.getConfig().CHANNELS.WELCOME_MESSAGES_CHANNEL;
            if (channel.isEmpty())
                channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

            DiscordMessageBuilder.forChannel(channel)
                    .placeholders(Utils.playerPlaceholders(p))
                    .useWebhook(false)
                    .format(FormatType.NEW_PLAYERS_MESSAGE)
                    .send();
            return;
        }

        if (p.hasPermission("magibridge.silentjoin")) {
            MagiBridge.getLogger().warn("The player " + p.getName() + " has the magibridge.silentjoin permission, not sending join message!");
            return;
        }

        String channel = plugin.getConfig().CHANNELS.JOIN_MESSAGES_CHANNEL;
        if (channel.isEmpty())
            channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

        DiscordMessageBuilder.forChannel(channel)
                .placeholders(Utils.playerPlaceholders(p))
                .useWebhook(false)
                .format(FormatType.JOIN_MESSAGE)
                .send();
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player p) {
        if (!p.hasPermission("magibridge.chat")) return;

        String channel = plugin.getConfig().CHANNELS.JOIN_MESSAGES_CHANNEL;
        if (channel.isEmpty())
            channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

        if (p.hasPermission("magibridge.silentquit")) {
            MagiBridge.getLogger().warn("The player " + p.getName() + " has the magibridge.silentjoin permission, not sending join message!");
            return;
        }

        DiscordMessageBuilder.forChannel(channel)
                .placeholders(Utils.playerPlaceholders(p))
                .useWebhook(false)
                .format(FormatType.QUIT_MESSAGE)
                .send();

    }

}
