package com.magitechserver.magibridge.common;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.UUID;

public abstract class NucleusBridge {

    protected static NucleusBridge instance;
    protected NucleusBridgeDelegate delegate;

    public void init(Object plugin, boolean nucleusAvailable) {
        if (nucleusAvailable) {
            try {
                Class.forName("io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent");
                delegate = (NucleusBridgeDelegate) Class.forName("com.magitechserver.magibridge.nucleusv1.NucleusV1Handlers").newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                try {
                    delegate = (NucleusBridgeDelegate) Class.forName("com.magitechserver.magibridge.nucleusv2.NucleusV2Handlers").newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                    delegate = new EmptyNucleusDelegate();
                }
            }
        } else {
            delegate = new EmptyNucleusDelegate();
        }

        Sponge.getEventManager().registerListeners(plugin, delegate);
    }

    public abstract Text getNick(UUID player);

    public abstract void onAfk(Player player, boolean goingAfk);

    public abstract void onHelpOp(Player player, Text message);

    public abstract void onBroadcast(Text prefix, Text suffix, Text message);

    public boolean isStaffChatEnabled() {
        return delegate.isStaffChatEnabled();
    }

    public boolean isDirectedToStaffChannel(MessageChannelEvent.Chat event) {
        return delegate.isDirectedToStaffChannel(event);
    }

    public MessageChannel getStaffChannel() {
        return delegate.getStaffChannel();
    }

    public Text replacePlaceholders(String string, CommandSource commandSource) {
        return delegate.replacePlaceholders(string, commandSource);
    }

    public static NucleusBridge getInstance() {
        return instance;
    }

}
