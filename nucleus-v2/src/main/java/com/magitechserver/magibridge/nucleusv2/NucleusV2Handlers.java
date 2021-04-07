package com.magitechserver.magibridge.nucleusv2;

import com.magitechserver.magibridge.common.NucleusBridge;
import com.magitechserver.magibridge.common.NucleusBridgeDelegate;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.module.afk.event.NucleusAFKEvent;
import io.github.nucleuspowered.nucleus.api.module.staffchat.NucleusStaffChatService;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.api.text.event.NucleusTextTemplateEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.UUID;

public class NucleusV2Handlers implements NucleusBridgeDelegate {

    @Listener
    @Include({NucleusAFKEvent.GoingAFK.class, NucleusAFKEvent.ReturningFromAFK.class})
    public void onAfkEvent(NucleusAFKEvent event) {
        NucleusBridge.getInstance().onAfk(event.getTargetEntity(), event instanceof NucleusAFKEvent.GoingAFK);
    }

    @Listener
    public void helpOpHandler(SendCommandEvent event, @Root Player player) {
        if (event.getCommand().toLowerCase().startsWith("helpop")) {
            NucleusBridge.getInstance().onHelpOp(player, Text.of(event.getArguments()));
        }
    }

    @Listener
    public void broadcastHandler(NucleusTextTemplateEvent.Broadcast event) {
        NucleusTextTemplate template = event.getMessage();
        NucleusBridge.getInstance().onBroadcast(
                template.getPrefix().orElse(null),
                template.getSuffix().orElse(null),
                template.getForCommandSource(Sponge.getServer().getConsole())
        );
    }

    @Override
    public Text getNickname(UUID player) {
        User user = Sponge.getServer().getPlayer(player).orElse(null);
        if (user == null)
            user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                    .get(player)
                    .orElse(null);
        return NucleusAPI.getNicknameService().get().getNickname(user).orElse(Text.of(user.getName()));
    }

    @Override
    public boolean isStaffChatEnabled() {
        return NucleusAPI.getStaffChatService().isPresent();
    }

    @Override
    public boolean isDirectedToStaffChannel(MessageChannelEvent.Chat event) {
        NucleusStaffChatService service = NucleusAPI.getStaffChatService().get();
        return service.isDirectedToStaffChat(event);
    }

    @Override
    public MessageChannel getStaffChannel() {
        return MessageChannel.fixed(NucleusAPI.getStaffChatService().get().getStaffChannelMembers());
    }

    @Override
    public Text replacePlaceholders(String string, CommandSource commandSource) {
        NucleusTextTemplate template = NucleusAPI.getTextTemplateFactory().createFromString(string);
        return template.getForCommandSource(commandSource);
    }
}
