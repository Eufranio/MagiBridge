package com.magitechserver.magibridge.listeners;

import com.arckenver.nations.channel.NationMessageChannel;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.MessageBuilder;
import com.magitechserver.magibridge.util.Utils;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
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
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.type.FixedMessageChannel;

/**
 * Created by Frani on 17/04/2019.
 */
public class VanillaListeners {

    @Listener
    public void onAdvancement(AdvancementEvent event, @Root Player p) {
        if (!p.hasPermission("magibridge.chat")) return;
        if (!MagiBridge.getConfig().CORE.ADVANCEMENT_MESSAGES_ENABLED) return;

        MessageBuilder.forChannel(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL)
                .placeholder("player", p.getName())
                .placeholder("nick", Utils.getNick(p))
                .placeholder("advancement", event.getAdvancement().getName())
                .placeholder("prefix", p.getOption("prefix").orElse(""))
                .placeholder("topgroup", Utils.getHighestGroup(p))
                .useWebhook(false)
                .format(FormatType.ADVANCEMENT_MESSAGE)
                .send();
    }

    @Listener
    public void onDeath(DestructEntityEvent.Death event) {
        if (event.getTargetEntity() instanceof Player && MagiBridge.getConfig().CORE.DEATH_MESSAGES_ENABLED) {
            if (event.getMessage().toPlain().isEmpty()) return;

            Player p = (Player) event.getTargetEntity();
            if (!p.hasPermission("magibridge.chat")) return;

            MessageBuilder.forChannel(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL)
                    .placeholder("player", p.getName())
                    .placeholder("nick", Utils.getNick(p))
                    .placeholder("deathmessage", event.getMessage().toPlain())
                    .placeholder("prefix", p.getOption("prefix").orElse(null))
                    .placeholder("topgroup", Utils.getHighestGroup(p))
                    .useWebhook(false)
                    .format(FormatType.DEATH_MESSAGE)
                    .send();
        }
    }

    @Listener(order = Order.LAST)
    public void onSpongeMessage(MessageChannelEvent.Chat e, @First Player p) {
        if (!MagiBridge.useVanillaChat) return;
        if (!p.hasPermission("magibridge.chat")) return;
        if (!Sponge.getServer().getOnlinePlayers().contains(p) || e.isMessageCancelled()) return;
        if (MagiBridge.getConfig().CORE.HIDE_VANISHED_CHAT && p.get(Keys.VANISH).orElse(false)) return;

        FormatType format = FormatType.SERVER_TO_DISCORD_FORMAT;
        String channel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;

        if (e.getChannel().isPresent()) {
            MessageChannel messageChannel = e.getChannel().get();

            if (Sponge.getPluginManager().isLoaded("nations") && messageChannel instanceof NationMessageChannel) {
                return; // don't want to send private nation messages to Discord
            }

            if (Sponge.getPluginManager().isLoaded("mcclans") &&
                    (messageChannel instanceof AllyMessageChannelImpl ||
                            messageChannel instanceof ClanMessageChannelImpl)) {
                return; // don't want to send clan messages to Discord
            }
        }

        if (MagiBridge.getConfig().CHANNELS.USE_NUCLEUS && Sponge.getPluginManager().isLoaded("nucleus") && e.getChannel().isPresent()) {
            MessageChannel messageChannel = e.getChannel().get();

            if (!NucleusAPI.getStaffChatService().isPresent()) {
                MagiBridge.getLogger().error("The staff chat module is disabled in the Nucleus config! Please enable it!");
                return;
            }

            boolean isStaffMessage = messageChannel instanceof NucleusChatChannel.StaffChat;
            if (!isStaffMessage && messageChannel instanceof FixedMessageChannel) {
                if (!messageChannel.getMembers().containsAll(Sponge.getServer().getBroadcastChannel().getMembers())) {
                    return; // probably a non-global channel, griefprevention uses this on it's mute sytem
                }
            }

            channel = isStaffMessage ?
                    MagiBridge.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL :
                    MagiBridge.getConfig().CHANNELS.NUCLEUS.GLOBAL_CHANNEL;
            if (channel.isEmpty()) return;

            boolean ignoreRoot = false;
            if (MagiBridge.getConfig().CORE.SEND_HELPOP && messageChannel instanceof NucleusChatChannel.HelpOp) {
                ignoreRoot = true;
                channel = MagiBridge.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL.isEmpty() ?
                        channel :
                        MagiBridge.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL;
            }

            if (!ignoreRoot && !(e.getSource() instanceof Player)) {
                return; // if it's not a helpop message, we don't care about other sources
            }

            format = isStaffMessage ?
                    FormatType.SERVER_TO_DISCORD_STAFF_FORMAT :
                    FormatType.SERVER_TO_DISCORD_FORMAT;

        } else {
            if (e.getChannel().isPresent() && Sponge.getPluginManager().isLoaded("nucleus") && MagiBridge.getConfig().CORE.SEND_HELPOP) {
                if (e.getChannel().get().getClass().getName().equals("io.github.nucleuspowered.util.PermissionMessageChannel")) {
                    channel = MagiBridge.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL.isEmpty() ?
                            MagiBridge.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL :
                            MagiBridge.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL;
                }
            }
        }

        MessageBuilder.forChannel(channel)
                .placeholder("prefix", p.getOption("prefix").orElse(""))
                .placeholder("player", p.getName())
                .placeholder("message", e.getFormatter().getBody().toText().toPlain())
                .placeholder("topgroup", Utils.getHighestGroup(p))
                .placeholder("nick", Utils.getNick(p))
                .format(format)
                .allowEveryone(p.hasPermission("magibridge.everyone"))
                .allowMentions(p.hasPermission("magibridge.mention"))
                .send();

    }

    @Listener
    public void onLogin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player p) {
        if (!p.hasPermission("magibridge.chat")) return;

        String mainChannel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;
        if (!p.hasPlayedBefore()) {
            MessageBuilder.forChannel(mainChannel)
                    .placeholder("player", p.getName())
                    .useWebhook(false)
                    .format(FormatType.NEW_PLAYERS_MESSAGE)
                    .send();
            return;
        }

        if (p.hasPermission("magibridge.silentjoin")) {
            MagiBridge.getLogger().warn("The player " + p.getName() + " has the magibridge.silentjoin permission, not sending join message!");
            return;
        }

        MessageBuilder.forChannel(mainChannel)
                .placeholder("player", p.getName())
                .placeholder("nick", Utils.getNick(p))
                .placeholder("prefix", p.getOption("prefix").orElse(""))
                .placeholder("topgroup", Utils.getHighestGroup(p))
                .useWebhook(false)
                .format(FormatType.JOIN_MESSAGE)
                .send();
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player p) {
        if (!p.hasPermission("magibridge.chat")) return;

        String mainChannel = MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL;

        if (p.hasPermission("magibridge.silentquit")) {
            MagiBridge.getLogger().warn("The player " + p.getName() + " has the magibridge.silentjoin permission, not sending join message!");
            return;
        }

        MessageBuilder.forChannel(mainChannel)
                .placeholder("player", p.getName())
                .placeholder("nick", Utils.getNick(p))
                .placeholder("prefix", p.getOption("prefix").orElse(""))
                .placeholder("topgroup", Utils.getHighestGroup(p))
                .useWebhook(false)
                .format(FormatType.QUIT_MESSAGE)
                .send();
    }

}
