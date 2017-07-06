package com.magitechserver.discord;

import com.magitechserver.UCHandler;
import com.magitechserver.util.Config;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Created by Frani on 04/07/2017.
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getChannel().getId().equals(Config.DISCORD_MAIN_CHANNEL)) {

            // User is not a bot
            if(event.getAuthor().getId() == event.getJDA().getSelfUser().getId() || event.getAuthor().isFake()) {
                return;
            }

            String message = event.getMessage().getContent();

            // Get nickname and not real name
            String name = event.getMember().getEffectiveName();

            // Message is not empty
            if(message == null || message.equalsIgnoreCase("") || message.equalsIgnoreCase(" ")) {
                return;
            }

            // Message is not higher than 120 chars
            if(message.length() > 120) {
                message = message.substring(0, message.length() - 120);
            }

            // Message is not in a code block
            if(message.startsWith("```")) {
                message = message.substring(0, message.length() - 3).substring(3);
            }
            if(message.startsWith("``")) {
                message = message.substring(0, message.length() - 2).substring(2);
            }

            // Replace placeholders from config to their actual values
            String msg = Config.DISCORD_TO_SERVER_FORMAT.replace("%user%", name).replace("%msg%", message);

            // Finally sends the message to the channel
            UCHandler.sendMessageToChannel("global", msg);

        }
    }

}
