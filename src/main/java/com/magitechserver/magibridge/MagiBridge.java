package com.magitechserver.magibridge;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.magitechserver.magibridge.config.ConfigManager;
import com.magitechserver.magibridge.config.categories.ConfigCategory;
import com.magitechserver.magibridge.discord.MessageListener;
import com.magitechserver.magibridge.listeners.*;
import com.magitechserver.magibridge.util.CommandHandler;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.TopicUpdater;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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

    public static MagiBridge instance = null;
    public static ConfigCategory Config;
    public static JDA jda;

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    public GuiceObjectMapperFactory factory;

    @Inject
    private Logger logger;

    private TopicUpdater updater;

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
                    jda.getPresence().setGame(Game.playing(Config.MESSAGES.BOT_GAME_STATUS));
                }

                if (!fake) {
                    DiscordHandler.init();
                    DiscordHandler.sendMessageToChannel(Config.CHANNELS.MAIN_CHANNEL, Config.MESSAGES.SERVER_STARTING);
                    CommandHandler.registerBroadcastCommand();

                    if (updater != null) {
                        if (updater.getState() == Thread.State.NEW) {
                            updater.start();
                        } else {
                            updater.interrupt();
                            updater = new TopicUpdater();
                            updater.start();
                        }
                    } else {
                        updater = new TopicUpdater();
                        updater.start();
                    }

                }
            }).submit(instance);
        }).submit(instance);
    }

    public void stopStuff(Boolean fake) {
        if (!fake) {
            if (jda != null) {
                DiscordHandler.sendMessageToChannel(Config.CHANNELS.MAIN_CHANNEL, Config.MESSAGES.SERVER_STOPPING);
                DiscordHandler.close();
                if (updater != null) updater.interrupt();
                if (MagiBridge.shouldUpdateTopic()) {
                    try {
                        String topic = FormatType.OFFLINE_TOPIC_FORMAT.get();
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
                            }
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
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
            jda = new JDABuilder(AccountType.BOT).setToken(Config.CORE.BOT_TOKEN).build().awaitReady();
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
            if (Sponge.getPluginManager().getPlugin("nucleus").isPresent()) {
                Sponge.getEventManager().registerListeners(this, new SpongeChatListener());
                logger.info("Hooking into Nucleus");
            } else {
                logger.error(" ");
                logger.error(" MagiBridge is configured to hook into Nucleus, but it isn't loaded! Please disable using-nucleus or load Nucleus on your server!");
                logger.error(" ");
            }
        } else if (Config.CHANNELS.USE_UCHAT) {
            if (Sponge.getPluginManager().getPlugin("ultimatechat").isPresent()) {
                Sponge.getEventManager().registerListeners(this, new ChatListener());
                logger.info("Hooking into UltimateChat");
            } else {
                logger.error(" ");
                logger.error(" MagiBridge is configured to hook into UltimateChat, but it isn't loaded! Please disable using-ultimatechat or load UltimateChat on your server!");
                logger.error(" ");
            }
        } else {
            Sponge.getEventManager().registerListeners(this, new VanillaChatListener());
            logger.info(" No Chat Hook enabled, hooking into the vanilla chat system");
            logger.info(" Some features may not work, and there will be no staff chat. If you want a more complete chat handling, use either Nucleus or UltimateChat.");
        }

        if (Config.CORE.DEATH_MESSAGES_ENABLED) {
            Sponge.getEventManager().registerListeners(this, new DeathListener());
        }

        if (Config.CORE.ADVANCEMENT_MESSAGES_ENABLED) {
            Sponge.getEventManager().registerListeners(this, new AdvancementListener());
        }

        Sponge.getEventManager().registerListeners(this, new SpongeLoginListener());
    }

    public static Boolean UpdateTopic(String str_id, String topic) {
        try {
            long id = Long.parseLong(str_id);

            if (jda.getTextChannelById(id) == null) {
                MagiBridge.getLogger().error("The main-discord-channel is INVALID, replace it with a valid one and restart the server!");
                return false;
            }

            jda.getTextChannelById(id).getManager().setTopic(topic).queue();
            return true;
        } catch (NumberFormatException e) {
            MagiBridge.getLogger().error("Error parsing the channel ID!");
            e.printStackTrace();
            return false;
        }
    }

    public static Boolean UpdateTopic(Long id, String topic) {
        try {
            if (jda.getTextChannelById(id) == null) {
                MagiBridge.getLogger().error("The main-discord-channel is INVALID, replace it with a valid one and restart the server!");
                return false;
            }

            jda.getTextChannelById(id).getManager().setTopic(topic).queue();
            return true;
        } catch (NumberFormatException e) {
            MagiBridge.getLogger().error("Error parsing the channel ID!");
            e.printStackTrace();
            return false;
        }
    }

    public static Boolean shouldUpdateTopic()
    {
        if (MagiBridge.getConfig().CORE.ENABLE_UPDATER && jda.getStatus() == JDA.Status.CONNECTED) {
            if (MagiBridge.getConfig().CHANNELS.TOPIC_CHANNELS != null) {
                if(!MagiBridge.getConfig().CHANNELS.TOPIC_CHANNELS.isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    public static Logger getLogger() {
        return instance.logger;
    }
}
