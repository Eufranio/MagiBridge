package com.magitechserver.magibridge.bridge.nucleus;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.common.NucleusBridge;
import com.magitechserver.magibridge.common.NucleusBridgeDelegate;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import com.magitechserver.magibridge.util.TextHelper;
import com.magitechserver.magibridge.util.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public class NucleusBridgeImpl extends NucleusBridge {

    protected MagiBridge plugin;

    public static void init(MagiBridge pl) {
        instance = new NucleusBridgeImpl() {{
            this.plugin = pl;
        }};
        instance.init(pl);
    }

    @Override
    public Text getNick(UUID player) {
        return delegate.getNickname(player);
    }

    @Override
    public void onAfk(Player player, boolean goingAfk) {
        if (!player.hasPermission("magibridge.chat")) return;
        if (plugin.getConfig().MESSAGES.AFK.AFK_ENABLED) {
            FormatType format = goingAfk ? FormatType.GOING_AFK : FormatType.RETURNING_AFK;
            if (format != null) {
                String channel = plugin.getConfig().CHANNELS.NUCLEUS.AFK_MESSAGES_CHANNEL;
                if (channel.isEmpty())
                    channel = plugin.getConfig().CHANNELS.MAIN_CHANNEL;

                DiscordMessageBuilder.forChannel(channel)
                        .placeholders(Utils.playerPlaceholders(player))
                        .useWebhook(false)
                        .format(format)
                        .send();
            }
        }
    }

    @Override
    public void onHelpOp(Player player, Text message) {
        if (!plugin.getConfig().CORE.SEND_HELPOP)
            return;

        String channel = plugin.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL.isEmpty() ?
                plugin.getConfig().CHANNELS.NUCLEUS.STAFF_CHANNEL :
                plugin.getConfig().CHANNELS.NUCLEUS.HELPOP_CHANNEL;

        FormatType format = FormatType.HELP_OP_MESSAGE;

        DiscordMessageBuilder.forChannel(channel)
                .placeholders(Utils.playerPlaceholders(player))
                .placeholder("message", message.toPlain())
                .format(format)
                .allowEveryone(player.hasPermission("magibridge.everyone"))
                .allowHere(player.hasPermission("magibridge.here"))
                .allowMentions(player.hasPermission("magibridge.mention"))
                .useWebhook(false)
                .send();
    }

    @Override
    public void onBroadcast(Text prefix, Text suffix, Text message) {
        if (prefix != null)
            message = TextHelper.replace(message, prefix, Text.EMPTY);
        if (suffix != null)
            message = TextHelper.replace(message, suffix, Text.EMPTY);

        ConfigCategory config = plugin.getConfig();

        String channel = config.CHANNELS.broadcastChannel;
        if (channel.isEmpty())
            channel = config.CHANNELS.MAIN_CHANNEL;

        DiscordMessageBuilder.forChannel(channel)
                .placeholder("message", message.toPlain())
                .format(FormatType.BROADCAST_MESSAGE)
                .allowEveryone(true)
                .allowHere(true)
                .allowMentions(true)
                .useWebhook(false)
                .send();
    }
}
