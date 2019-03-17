package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import org.spongepowered.api.Sponge;

import java.lang.management.ManagementFactory;

/**
 * Created by POQDavid on 17/03/2019.
 */
public class PresenceUpdater extends Thread {

    public PresenceUpdater() {
        setName("MagiBridge Presence Updater");
    }

    @Override
    public void run() {
        while (true) {
            if (MagiBridge.shouldUpdatePresence()) {
                String presence = FormatType.PRESENCE_FORMAT.get()
                        .replace("%players%", Integer.valueOf(Sponge.getServer().getOnlinePlayers().size()).toString())
                        .replace("%maxplayers%", Integer.valueOf(Sponge.getServer().getMaxPlayers()).toString())
                        .replace("%tps%", Long.valueOf(Math.round(Sponge.getServer().getTicksPerSecond())).toString())
                        .replace("%daysonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime() / (24 * 60 * 60 * 1000)).toString())
                        .replace("%hoursonline%", Long.valueOf((ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 60 * 1000)) % 24).toString())
                        .replace("%minutesonline%", Long.valueOf((ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 1000)) % 60).toString());
                try {
                        if (!MagiBridge.updatePresence(presence)) {
                            MagiBridge.getLogger().error("Failed to update presence!");
                        }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            int interval = MagiBridge.getConfig().CORE.PRESENCE_UPDATER_INTERVAL * 1000;
            try {
                Thread.sleep(interval < 10000 ? 10000 : interval);
            } catch (InterruptedException e) {
                MagiBridge.getLogger().error("The presence updater was interrupted!");
                e.printStackTrace();
            }
        }
    }
}
