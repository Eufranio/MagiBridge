package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 29/12/2019.
 */
public class ConsoleHandler extends AbstractAppender {

    MessageBuilder messageBuilder = new MessageBuilder();
    MagiBridge plugin;

    public ConsoleHandler(MagiBridge plugin) {
        super("MagiBridgeConsoleWatcher", null, null);

        this.plugin = plugin;
        Task.builder().interval(2, TimeUnit.SECONDS)
                .execute(this::sendMessages)
                .submit(MagiBridge.getInstance());
    }

    @Override
    public void append(LogEvent event) {
        if (!event.getLevel().isInRange(Level.FATAL, Level.INFO)) {
            return;
        }

        FormatType format = FormatType.SERVER_CONSOLE_TO_DISCORD_FORMAT;
        if (plugin.getConfig() == null)
            return;
        String channel = plugin.getConfig().CHANNELS.CONSOLE_CHANNEL;

        String msg = DiscordMessageBuilder.forChannel(channel)
                .placeholder("threadname", event.getThreadName())
                .placeholder("loglevel", event.getLevel().name())
                .placeholder("loggername", event.getLoggerName())
                .placeholder("message", event.getMessage().getFormattedMessage())
                .format(format)
                .useWebhook(false)
                .get();
        if (msg == null)
            return;

        messageBuilder.append(msg).append('\n');
    }

    public void sendMessages() {
        if (messageBuilder.isEmpty())
            return;

        JDA jda = plugin.getJDA();
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED)
            return;

        TextChannel textChannel = jda.getTextChannelById(plugin.getConfig().CHANNELS.CONSOLE_CHANNEL);
        if (textChannel == null)
            return;
        try {
            messageBuilder.buildAll().forEach(m -> textChannel.sendMessage(m).queue());
            messageBuilder = new MessageBuilder();
        } catch (Exception e) { e.printStackTrace(); }
    }
}


