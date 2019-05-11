package com.magitechserver.magibridge;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.magitechserver.magibridge.config.ConfigManager;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import com.magitechserver.magibridge.discord.DiscordHandler;
import com.magitechserver.magibridge.discord.MessageBuilder;
import com.magitechserver.magibridge.discord.MessageListener;
import com.magitechserver.magibridge.listeners.*;
import com.magitechserver.magibridge.util.CommandHandler;
import com.magitechserver.magibridge.config.FormatType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frani on 22/06/2017.
 */

@Plugin(id = "magibridge",
        name = "MagiBridge",
        description = "A utility Discord <-> Minecraft chat relay plugin",
        authors = {"Eufranio"},
        dependencies = {
                @Dependency(id = "ultimatechat", optional = true),
                @Dependency(id = "nucleus", optional = true),
                @Dependency(id = "boop", version = "[1.5.0,)", optional = true)})

public class MagiBridge {

    public static boolean useVanillaChat = false;
    public static MagiBridge instance = null;
    public static ConfigCategory Config;
    public static JDA jda;
    private Task updaterTask;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    public GuiceObjectMapperFactory factory;

    @Inject
    private Logger logger;

    public static MagiBridge getInstance() {
        return instance;
    }

    public static ConfigCategory getConfig() {
        return Config;
    }

    @Listener
    public void init(GamePostInitializationEvent event) {
        instance = this;
        initStuff(false);
        Sponge.getServiceManager().provide(PermissionService.class).ifPresent(svc -> {
            svc.getDefaults().getTransientSubjectData().setPermission(Sets.newHashSet(), "magibridge.chat", Tristate.TRUE);
            svc.getDefaults().getTransientSubjectData().setPermission(Sets.newHashSet(), "magibridge.mention", Tristate.TRUE);
        });
    }

    @Listener
    public void stop(GameStoppingEvent event) {
        stopStuff(false);
    }

    @Listener
    public void reload(GameReloadEvent event) {
        stopStuff(true);
        initStuff(true);
        logger.info("Plugin reloaded successfully!");
    }

    public void initStuff(Boolean fake) {
        logger.info("MagiBridge is starting!");
        Config = new ConfigManager(instance).loadConfig();
        Task.builder().async().execute(() -> {
            this.initJDA();
            Task.builder().execute(() -> {
                this.registerListeners();

                if (!Config.MESSAGES.BOT_GAME_STATUS.isEmpty()) {
                    jda.getPresence().setActivity(Activity.playing(Config.MESSAGES.BOT_GAME_STATUS));
                }

                if (!fake) {
                    DiscordHandler.init();

                    MessageBuilder.forChannel(Config.CHANNELS.MAIN_CHANNEL)
                            .format(FormatType.SERVER_STARTING)
                            .useWebhook(false)
                            .send();
                    CommandHandler.registerBroadcastCommand();
                }

                if (getConfig().CORE.ENABLE_UPDATER && getJDA().getStatus() == JDA.Status.CONNECTED) {
                    this.updaterTask = Task.builder()
                            .interval(Math.max(MagiBridge.getConfig().CORE.UPDATER_INTERVAL, 10), TimeUnit.SECONDS)
                            .execute(() -> {
                                TextChannel channel = getJDA().getTextChannelById(MagiBridge.getConfig().CHANNELS.MAIN_CHANNEL);
                                if (channel == null) {
                                    logger.error("The main-discord-channel is INVALID, replace it with a valid one and restart the server!");
                                    return;
                                }

                                String topic = FormatType.TOPIC_FORMAT.get()
                                        .replace("%players%", "" + Sponge.getServer().getOnlinePlayers().stream().filter(p -> !p.get(Keys.VANISH).orElse(false)).count())
                                        .replace("%maxplayers%", Integer.valueOf(Sponge.getServer().getMaxPlayers()).toString())
                                        .replace("%tps%", Long.valueOf(Math.round(Sponge.getServer().getTicksPerSecond())).toString())
                                        .replace("%daysonline%", Long.valueOf(ManagementFactory.getRuntimeMXBean().getUptime() / (24 * 60 * 60 * 1000)).toString())
                                        .replace("%hoursonline%", Long.valueOf((ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 60 * 1000)) % 24).toString())
                                        .replace("%minutesonline%", Long.valueOf((ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 1000)) % 60).toString());

                                channel.getManager().setTopic(topic).queue();
                            })
                            .submit(this);
                }
            }).submit(instance);
        }).submit(instance);
    }

    public void stopStuff(Boolean fake) {
        if (this.updaterTask != null) this.updaterTask.cancel();

        if (!fake) {
            if (jda != null) {
                MessageBuilder.forChannel(Config.CHANNELS.MAIN_CHANNEL)
                        .format(FormatType.SERVER_STOPPING)
                        .useWebhook(false)
                        .send();

                DiscordHandler.close();

                if (this.updaterTask != null) this.updaterTask.cancel();
                TextChannel channel = getJDA().getTextChannelById(Config.CHANNELS.MAIN_CHANNEL);
                if (channel != null) channel.getManager().setTopic(FormatType.OFFLINE_TOPIC_FORMAT.get()).queue();

                jda.shutdown();
                long time = System.currentTimeMillis();
                while ((System.currentTimeMillis() - time < 10000) && (jda != null && jda.getStatus() != JDA.Status.SHUTDOWN)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        }

        logger.info("Disconnecting from Discord...");
        if (jda != null && jda.getStatus() != JDA.Status.SHUTDOWN && jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.shutdownNow();
        }

        // Unregistering listeners
        Sponge.getEventManager().unregisterPluginListeners(this);
        Sponge.getEventManager().registerListeners(this, this);

        Config = null;
    }

    private boolean initJDA() {
        try {
            jda = new JDABuilder(Config.CORE.BOT_TOKEN).build().awaitReady();
            jda.addEventListener(new MessageListener());
        } catch (LoginException e) {
            logger.error("ERROR STARTING THE PLUGIN:");
            logger.error("THE TOKEN IN THE CONFIG IS INVALID!");
            logger.error("You probably didn't set the token yet, edit your config!");
            return false;
        } catch (Exception e) {
            logger.error("Error connecting to discord. This is NOT a plugin error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void registerListeners() {
        if (Config.CHANNELS.USE_NUCLEUS) {
            useVanillaChat = true;
            if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                Sponge.getEventManager().registerListeners(this, new NucleusListeners());
                logger.info("Hooking into Nucleus");
            } else {
                logger.error(" ");
                logger.error(" MagiBridge is configured to hook into Nucleus, but it isn't loaded! Please disable using-nucleus or load Nucleus on your server!");
                logger.error(" ");
            }
        } else if (Config.CHANNELS.USE_UCHAT) {
            if (Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
                Sponge.getEventManager().registerListeners(this, new UChatListeners());
                logger.info("Hooking into UltimateChat");
            } else {
                logger.error(" ");
                logger.error(" MagiBridge is configured to hook into UltimateChat, but it isn't loaded! Please disable using-ultimatechat or load UltimateChat on your server!");
                logger.error(" ");
                useVanillaChat = true;
            }
        } else {
            logger.info(" No Chat Hook enabled, hooking into the vanilla chat system");
            logger.info(" Some features may not work, and there will be no staff chat. If you want a more complete chat handling, use either Nucleus or UltimateChat.");
            useVanillaChat = true;
        }

        Sponge.getEventManager().registerListeners(this, new VanillaListeners());
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public JDA getJDA() {
        return jda;
    }
}
