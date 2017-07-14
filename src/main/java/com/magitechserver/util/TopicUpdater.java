package com.magitechserver.util;

import com.magitechserver.MagiBridge;
import com.magitechserver.discord.MessageListener;
import org.spongepowered.api.Sponge;

import java.lang.management.ManagementFactory;

/**
 * Created by Frani on 14/07/2017.
 */
public class TopicUpdater extends Thread {

    public TopicUpdater() {
        setName("MagiBridge Topic Updater");
    }

    @Override
    public void run() {
        while (true) {

            String topic = MagiBridge.getConfig().getString("messages", "channel-topic-message")
                    .replace("%players%", Integer.valueOf(Sponge.getServer().getOnlinePlayers().size()).toString())
                    .replace("%maxplayers%", Integer.valueOf(Sponge.getServer().getMaxPlayers()).toString())
                    .replace("%tps%", Long.valueOf(Math.round(Sponge.getServer().getTicksPerSecond())).toString())
                    .replace("%hoursonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime()/3600000).toString())
                    .replace("%minutesonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime()/60000).toString());

            MagiBridge.jda.getTextChannelById(MagiBridge.getConfig().getString("channel", "main-discord-channel")).getManager().setTopic(topic).queue();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                MagiBridge.logger.error("Something interrupted the topic updater!");
            }
        }
    }

}
