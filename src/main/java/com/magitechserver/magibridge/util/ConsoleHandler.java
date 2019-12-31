package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordHandler;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.spongepowered.api.scheduler.Task;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 29/12/2019.
 */
public class ConsoleHandler extends AbstractAppender {

    private LinkedList<String> messagesQueue = new LinkedList<>();

    public ConsoleHandler() {
        super("MagiBridgeConsoleWatcher", null, null);

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
        if (MagiBridge.getConfig() == null)
            return;
        String channel = MagiBridge.getConfig().CHANNELS.CONSOLE_CHANNEL;

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

        messagesQueue.add(msg);
        /*if (this.stopping) {
            this.sendMessages();
        }*/
    }

    public void sendMessages() {
        if (messagesQueue.isEmpty())
            return;
        StringBuilder buffer = new StringBuilder();
        int currentMessageSize = 0;
        while (!messagesQueue.isEmpty()) {
            if (currentMessageSize >= 2000)
                break;
            String message = messagesQueue.peek();
            int finalCount = currentMessageSize + message.length();
            if (finalCount >= 2000) {
                //split the message in 2
                int endIndex = finalCount % 1999;
                messagesQueue.poll();
                messagesQueue.add(0, message.substring(endIndex));
                messagesQueue.add(0, message.substring(0, endIndex));
                continue;
            }
            currentMessageSize += message.length();

            buffer.append(messagesQueue.poll()).append("\n");
        }
        if (currentMessageSize == 0)
            return;
        String m = buffer.toString();
        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().CHANNELS.CONSOLE_CHANNEL, m);
    }
}


