package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.discord.DiscordHandler;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Frani on 01/05/2019.
 */
public class Utils {

    public static String getHighestGroup(Player player) {
        try {
            if (!Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).isPresent()) return "";
            PermissionService ps = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
            HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();
            for (SubjectReference sub : player.getParents()) {
                if (sub.getCollectionIdentifier().equals(ps.getGroupSubjects().getIdentifier()) /*&& !sub.getSubjectIdentifier().isEmpty()*/) {
                    Subject subj = sub.resolve().get();
                    subs.put(subj.getParents().size(), subj);
                }
            }
            return subs.isEmpty() ? "" : subs.get(Collections.max(subs.keySet())).getFriendlyIdentifier().isPresent() ? subs.get(Collections.max(subs.keySet())).getFriendlyIdentifier().get() : "";
        } catch (InterruptedException | ExecutionException e) {
        }
        return "";
    }

    public static String replaceEach(String text, Map<String, String> replacements) {
        String finalString = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (finalString.contains(entry.getKey())) {
                finalString = finalString.replace(entry.getKey(), entry.getValue());
            }
        }

        return finalString;
    }

    public static void dispatchCommand(MessageReceivedEvent e) {
        String[] args = e.getMessage().getContentDisplay()
                .replace(MagiBridge.getConfig().CHANNELS.CONSOLE_COMMAND + " ", "")
                .split(" ");

        if (!canUseCommand(e.getMember(), args[0])) {
            e.getChannel().sendMessage(MagiBridge.getConfig().MESSAGES.CONSOLE_NO_PERMISSION);
            DiscordHandler.sendMessageToChannel(e.getChannel().getId(), MagiBridge.getConfig().MESSAGES.CONSOLE_NO_PERMISSION);
            return;
        }

        String cmd = String.join(" ", args);
        Sponge.getCommandManager().process(new BridgeCommandSource(e.getChannel().getId(), Sponge.getServer().getConsole()), cmd);
    }

    public static void dispatchList(MessageChannel channel) {
        boolean shouldDelete = MagiBridge.getConfig().CHANNELS.DELETE_LIST;

        List<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.get(Keys.VANISH).orElse(false))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());

        String message = MagiBridge.getConfig().MESSAGES.NO_PLAYERS;
        if (!onlinePlayers.isEmpty()) {
            String format = MagiBridge.getConfig().MESSAGES.PLAYER_LIST_NAME;
            String players = onlinePlayers.stream()
                    .map(p -> format.replace("%player%", p.getName())
                            .replace("%topgroup%", Utils.getHighestGroup(p))
                            .replace("%prefix%", p.getOption("prefix").orElse("")))
                    .collect(Collectors.joining(", "));

            message = "**Players online (" + onlinePlayers.size() + "/" + Sponge.getServer().getMaxPlayers() + "):** "
                    + "```" + players + "```";
        }

        channel.sendMessage(message).queue(m -> {
            if (shouldDelete) {
                m.delete().queueAfter(10, TimeUnit.SECONDS);
            }
        });
    }

    private static boolean canUseCommand(Member m, String command) {
        Map<String, String> override = MagiBridge.getConfig().CHANNELS.COMMANDS_ROLE_OVERRIDE;
        if (override.getOrDefault(command, "")
                .equalsIgnoreCase("everyone")) {
            return true;
        }

        if (m.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(MagiBridge.getConfig().CHANNELS.CONSOLE_REQUIRED_ROLE))) {
            return true;
        }

        String r = override.get(command);
        return r != null && m.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(r));
    }

    public static Text toText(String s) {
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    public static String getNick(Player p) {
        if (!Sponge.getPluginManager().getPlugin("nucleus").isPresent()) return p.getName();
        return NucleusAPI.getNicknameService()
                .map(s -> s.getNickname(p).map(Text::toPlain).orElse(null))
                .orElse(p.getName());
    }
}
