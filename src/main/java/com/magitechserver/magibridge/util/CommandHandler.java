package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import net.dv8tion.jda.api.entities.TextChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Frani on 14/07/2017.
 */
public class CommandHandler {

    public static void registerBroadcastCommand() {
        CommandSpec cs = CommandSpec.builder()
                .description(Text.of("Broadcasts a message to the specified Discord channel name"))
                .permission("magibridge.admin.broadcast")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("channel"))),
                        GenericArguments.remainingJoinedStrings(Text.of("message")))
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        if (args.<String>getOne("channel").isPresent() && args.<String>getOne("message").isPresent()) {
                            String channel = args.<String>getOne("channel").get();
                            String message = args.<String>getOne("message").get();
                            for (TextChannel discordChannel : MagiBridge.jda.getTextChannels()) {
                                if (discordChannel.getName().equals(channel)) {
                                    for (TextChannel ch : MagiBridge.jda.getTextChannelsByName(channel, true)) {
                                        ch.sendMessage(message).queue();
                                    }
                                }
                            }
                            src.sendMessage(Text.builder("Message sent!").color(TextColors.GREEN).build());
                            return CommandResult.success();
                        }
                        src.sendMessage(Text.builder("Could not send message! Are you sure the channel exists?").color(TextColors.RED).build());
                        return CommandResult.empty();
                    }
                })
                .build();
        Sponge.getCommandManager().register(MagiBridge.getInstance(), cs, "mbroadcast", "mb", "mbc");
    }

}
