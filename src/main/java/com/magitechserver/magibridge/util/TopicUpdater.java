package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
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
            if(MagiBridge.getConfig().getBool("misc", "enable-topic-updater")) {
                String topic = MagiBridge.getConfig().getString("messages", "channel-topic-message")
                        .replace("%players%", Integer.valueOf(Sponge.getServer().getOnlinePlayers().size()).toString())
                        .replace("%maxplayers%", Integer.valueOf(Sponge.getServer().getMaxPlayers()).toString())
                        .replace("%tps%", Long.valueOf(Math.round(Sponge.getServer().getTicksPerSecond())).toString())
                        .replace("%hoursonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime() / 3600000).toString())
                        .replace("%minutesonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime() / 60000).toString());

                try {

                    if (MagiBridge.jda.getTextChannelById(MagiBridge.getConfig().getString("channel", "main-discord-channel")) == null) {
                        MagiBridge.logger.error("The main-discord-channel is INVALID, replace it with a valid one and restart the server!");
                        continue;
                    }

                    MagiBridge.jda.getTextChannelById(MagiBridge.getConfig().getString("channel", "main-discord-channel")).getManager().setTopic(topic).queue();

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            int interval = MagiBridge.getConfig().getInt("misc", "topic-updater-interval") * 1000 * 60;
            try {
                Thread.sleep(interval < 5 ? 5 * 1000 * 60 : interval * 1000 * 60);
            } catch (InterruptedException e) {
                MagiBridge.logger.error("Something interrupted the topic updater!");
            }
        }
    }

}
