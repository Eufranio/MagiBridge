package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.categories.Channel;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
        ConfigCategory config = MagiBridge.getInstance().getConfig();

        boolean consoleMessage = e.getMessage().getContentDisplay().startsWith(config.CHANNELS.CONSOLE_COMMAND);
        String[] args = e.getMessage().getContentDisplay()
                .replace(config.CHANNELS.CONSOLE_COMMAND + " ", "")
                .split(" ");

        if (!canUseCommand(e.getMember(), args[0])) {
            e.getChannel().sendMessage(config.MESSAGES.CONSOLE_NO_PERMISSION).queue();
            return;
        }

        String cmd = String.join(" ", args);
        Sponge.getCommandManager().process(
                consoleMessage ? new BridgeCommandSource(e.getChannel().getId(), Sponge.getServer().getConsole()) :
                        Sponge.getServer().getConsole(),
                cmd);
    }

    public static void dispatchList(MessageChannel channel, Message originalMessage) {
        ConfigCategory config = MagiBridge.getInstance().getConfig();
        boolean shouldDelete = config.CHANNELS.DELETE_LIST;
        int delay = config.CHANNELS.LIST_DELAY;

        List<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.get(Keys.VANISH).orElse(false))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());

        String message = config.MESSAGES.NO_PLAYERS;
        if (!onlinePlayers.isEmpty()) {
            String format = config.MESSAGES.PLAYER_LIST_NAME;
            String players = onlinePlayers.stream()
                    .map(p -> format.replace("%player%", p.getName())
                            .replace("%topgroup%", Utils.getHighestGroup(p))
                            .replace("%prefix%", p.getOption("prefix").orElse("")))
                    .collect(Collectors.joining(", "));

            message = "**"+ config.MESSAGES.LIST_TEXT + " (" + onlinePlayers.size() + "/" + Sponge.getServer().getMaxPlayers() + "):** "
                    + "```" + players + "```";
        }

        channel.sendMessage(message).queue(m -> {
            if (shouldDelete) {
                m.delete().queueAfter(delay, TimeUnit.SECONDS);
                originalMessage.delete().queueAfter(delay, TimeUnit.SECONDS);
            }
        });
    }

    private static boolean canUseCommand(Member m, String command) {
        ConfigCategory config = MagiBridge.getInstance().getConfig();
        Map<String, String> override = config.CHANNELS.COMMANDS_ROLE_OVERRIDE;
        if (override.getOrDefault(command, "")
                .equalsIgnoreCase("everyone")) {
            return true;
        }

        if (m.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase(config.CHANNELS.CONSOLE_REQUIRED_ROLE) ||
                r.getId().equals(config.CHANNELS.CONSOLE_REQUIRED_ROLE))) {
            return true;
        }

        String r = override.get(command);
        return r != null && m.getRoles().stream().anyMatch(role ->
                role.getName().equalsIgnoreCase(r) ||
                role.getId().equals(r));
    }

    public static Text toText(String s) {
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    public static String getNick(Player p) {
        if (!Sponge.getPluginManager().getPlugin("nucleus").isPresent()) return p.getName();
        return NucleusAPI.getNicknameService()
                .flatMap(s -> s.getNickname(p).map(Text::toPlain))
                .orElse(p.getName());
    }

    public static void turnAllConfigChannelsNumeric() {
        Channel channels = MagiBridge.getInstance().getConfig().CHANNELS;
        channels.MAIN_CHANNEL = replaceIfNotNumeric(channels.MAIN_CHANNEL);
        channels.JOIN_MESSAGES_CHANNEL = replaceIfNotNumeric(channels.JOIN_MESSAGES_CHANNEL);
        channels.ADVANCEMENT_MESSAGES_CHANNEL = replaceIfNotNumeric(channels.ADVANCEMENT_MESSAGES_CHANNEL);
        channels.DEATH_MESSAGES_CHANNEL = replaceIfNotNumeric(channels.DEATH_MESSAGES_CHANNEL);
        channels.WELCOME_MESSAGES_CHANNEL = replaceIfNotNumeric(channels.WELCOME_MESSAGES_CHANNEL);
        channels.TOPIC_UPDATER_CHANNEL = replaceIfNotNumeric(channels.TOPIC_UPDATER_CHANNEL);
        channels.START_MESSAGES_CHANNEL = replaceIfNotNumeric(channels.START_MESSAGES_CHANNEL);

        Channel.UChatCategory uchat = channels.UCHAT;
        uchat.UCHAT_CHANNELS = uchat.UCHAT_CHANNELS.entrySet().stream()
                .collect(Collectors.toMap(e -> replaceIfNotNumeric(e.getKey()), Map.Entry::getValue));

        Channel.NucleusCategory nucleus = channels.NUCLEUS;
        nucleus.GLOBAL_CHANNEL = replaceIfNotNumeric(nucleus.GLOBAL_CHANNEL);
        nucleus.HELPOP_CHANNEL = replaceIfNotNumeric(nucleus.HELPOP_CHANNEL);
        nucleus.STAFF_CHANNEL = replaceIfNotNumeric(nucleus.STAFF_CHANNEL);
        nucleus.AFK_MESSAGES_CHANNEL = replaceIfNotNumeric(nucleus.AFK_MESSAGES_CHANNEL);
    }

    private static String replaceIfNotNumeric(String s) {
        return !isNumeric(s) ? s.replaceAll("[^0-9]", "") : s;
    }

    // from StringUtils.isNumeric
    private static boolean isNumeric(String cs) {
        if (cs.isEmpty()) {
            return false;
        } else {
            int sz = cs.length();
            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static Map<String, String> playerPlaceholders(Player player) {
        return new HashMap<String, String>() {{
            put("player", player.getName());
            put("prefix", player.getOption("prefix").orElse(""));
            put("suffix", player.getOption("suffix").orElse(""));
            put("topgroup", getHighestGroup(player));
            put("nick", getNick(player));
        }};
    }
}
