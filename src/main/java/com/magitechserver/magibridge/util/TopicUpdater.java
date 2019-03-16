package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import net.dv8tion.jda.core.JDA;
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
            if (MagiBridge.shouldUpdateTopic()) {
                    String topic = FormatType.TOPIC_FORMAT.get()
                            .replace("%players%", Integer.valueOf(Sponge.getServer().getOnlinePlayers().size()).toString())
                            .replace("%maxplayers%", Integer.valueOf(Sponge.getServer().getMaxPlayers()).toString())
                            .replace("%tps%", Long.valueOf(Math.round(Sponge.getServer().getTicksPerSecond())).toString())
                            .replace("%daysonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime() / (24 * 60 * 60 * 1000)).toString())
                            .replace("%hoursonline%", Long.valueOf((ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 60 * 1000)) % 24).toString())
                            .replace("%minutesonline%", Long.valueOf((ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 1000)) % 60).toString());

                    try {
                        if (MagiBridge.getConfig().CHANNELS.TOPIC_CHANNELS.contains(",")) {
                            String[] IDs = MagiBridge.getConfig().CHANNELS.TOPIC_CHANNELS.split(",");
                            for (String str_id : IDs) {
                                if (!MagiBridge.UpdateTopic(str_id, topic)) {
                                    MagiBridge.getLogger().error("The main-discord-channel is INVALID, replace it with a valid one and restart the server!");
                                }
                            }
                        } else {
                            String str_id = MagiBridge.getConfig().CHANNELS.TOPIC_CHANNELS;
                            if (!MagiBridge.UpdateTopic(str_id, topic)) {
                                MagiBridge.getLogger().error("The main-discord-channel is INVALID, replace it with a valid one and restart the server!");
                                continue;
                            }
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
            }

            int interval = MagiBridge.getConfig().CORE.UPDATER_INTERVAL * 1000;
            try {
                Thread.sleep(interval < 10000 ? 10000 : interval);
            } catch (InterruptedException e) {
                MagiBridge.getLogger().error("The topic updater was interrupted!");
                e.printStackTrace();
            }
        }
    }
}
