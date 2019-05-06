package com.magitechserver.magibridge.discord;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.util.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 05/07/2017.
 */
public class DiscordHandler {

    private static final int MESSAGE_SIZE_LIMIT = 2000;
    private static Queue<Tuple<String, String>> messageQueue;
    private static boolean initialized = false;
    private static Task messageSendingTask;

    public static void init() {
        if (initialized) return;
        initialized = true;
        messageQueue = new ConcurrentLinkedQueue<>();

        messageSendingTask = Task.builder()
                .execute(() -> {
                    if (messageQueue.isEmpty()) return;
                    Map<String, List<String>> groupedMessages = new HashMap<>();

                    // group messages to their channels
                    int size = messageQueue.size();
                    for (int i = 0; i < size; i++) {
                        Tuple<String, String> message = messageQueue.poll();
                        if (message == null) break;
                        List<String> messageGroup = groupedMessages.get(message.getFirst());
                        if (messageGroup == null) messageGroup = new ArrayList<>();
                        messageGroup.add(message.getSecond());
                        groupedMessages.put(message.getFirst(), messageGroup);
                    }

                    // send the messages in one code block
                    for (Map.Entry<String, List<String>> collectedMessages : groupedMessages.entrySet()) {
                        String channel = collectedMessages.getKey();
                        StringBuffer messageBuffer = new StringBuffer("```\n");
                        for (String message : collectedMessages.getValue()) {
                            // make sure our message isn't too large
                            if (messageBuffer.length() + message.length() + 1 < MESSAGE_SIZE_LIMIT - 10) {
                                messageBuffer.append(message).append("\n");
                            } else {
                                messageBuffer.append("\n```");
                                //sendMessageToChannel(channel, messageBuffer.toString());
                                messageBuffer = new StringBuffer("\n```").append(message);
                            }
                            // only send if we have something left to send
                            if (messageBuffer.length() > 4) {
                                messageBuffer.append("\n```");
                                //sendMessageToChannel(channel, messageBuffer.toString());
                            }
                        }
                    }
                })
                .async()
                .delay(3, TimeUnit.SECONDS).interval(2, TimeUnit.SECONDS)
                .name("MagiBridge - Send Messages to Discord")
                .submit(MagiBridge.getInstance());
    }

    public static void close() {
        if (!initialized) return;
        messageSendingTask.cancel();
        messageQueue.clear();
        initialized = false;
    }

    public static void sendMessageToChannel(String channel, String message) {
        TextChannel textChannel = MagiBridge.getInstance().getJDA().getTextChannelById(channel);
        if (textChannel == null) return;
        textChannel.sendMessage(message).queue();
        //messageQueue.offer(Tuple.of(channel, message));
    }

}
