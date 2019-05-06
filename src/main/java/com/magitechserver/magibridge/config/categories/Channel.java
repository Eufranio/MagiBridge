package com.magitechserver.magibridge.config.categories;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class Channel {

    @Setting(value = "use-nucleus", comment = "Should MagiBridge use Nucleus? If this and use-ultimatechat are both false, MagiBridge will use the vanilla chat system")
    public boolean USE_NUCLEUS = true;
    @Setting(value = "use-ultimatechat", comment = "Should MagiBridge use UltimateChat instead? If this and use-nucleus are both false, MagiBridge will use the vanilla chat system")
    public boolean USE_UCHAT = false;
    @Setting(value = "use-webhooks", comment = "Should MagiBridge send messages trough Webhooks instead a bot?")
    public boolean USE_WEBHOOKS = true;
    @Setting(value = "player-list-command", comment = "Discord command that shows the current online player list")
    public String LIST_COMMAND = "!online";
    @Setting(value = "delete-list-message", comment = "Should MagiBridge delete the player list message?")
    public boolean DELETE_LIST = true;
    @Setting(value = "console-command", comment = "Discord command that executes server console commands")
    public String CONSOLE_COMMAND = "!cmd";
    @Setting(value = "commands-role-override", comment = "If a command is defined here, it will ONLY run if the user has the defined role.\n" +
            "In this example, ONLY who have the admin role can stop the server\n" +
            "Set the role to \"everyone\" to allow everyone use the command")
    public Map<String, String> COMMANDS_ROLE_OVERRIDE = Maps.newHashMap();
    @Setting(value = "console-command-required-role", comment = "Role that a user needs to have in order to run the console command")
    public String CONSOLE_REQUIRED_ROLE = "admin";
    @Setting(value = "color-allowed-role", comment = "Role that users need to have to be able to send colored chat to minecraft\n" +
            "Set to \"everyone\" to allow everyone use colors in the messages")
    public String COLOR_REQUIRED_ROLE = "vip";
    @Setting(value = "ultimatechat", comment = "IGNORE IF USING NUCLEUS!")
    public UChatCategory UCHAT = new UChatCategory();
    @Setting(value = "nucleus", comment = "IGNORE IF USING ULTIMATECHAT!")
    public NucleusCategory NUCLEUS = new NucleusCategory();
    @Setting(value = "main-discord-channel", comment = "ID of the main Discord Channel, where start/stop messages will be sent to")
    public String MAIN_CHANNEL = "MAIN_CHANNEL_ID_HERE";

    public Channel() {
        COMMANDS_ROLE_OVERRIDE.put("stop", "admin");
        COMMANDS_ROLE_OVERRIDE.put("ban", "mod");
    }

    @ConfigSerializable
    public static class UChatCategory {
        @Setting(value = "channels", comment = "Format: ChannelID = IngameChannelName, Example:\n" +
                "12345678912345 = global\n" +
                "Replace the default value with your own channels")
        public Map<String, String> UCHAT_CHANNELS = Maps.newHashMap();
        @Setting(value = "channel-overrides", comment = "Channels defined here override the global format, so you can have per-channel formats")
        public Map<String, String> UCHAT_OVERRIDES = Maps.newHashMap();

        public UChatCategory() {
            UCHAT_CHANNELS.put("12345678912345", "global");
            UCHAT_OVERRIDES.put("global", "&7[&a&lG&7] &f%user%&7: %message%");
        }
    }

    @ConfigSerializable
    public static class NucleusCategory {
        @Setting(value = "global-discord-channel", comment = "Discord Channel ID which global messages are sent to")
        public String GLOBAL_CHANNEL = "GLOBAL_DISCORD_CHANNEL_ID_HERE";

        @Setting(value = "staff-discord-channel", comment = "Discord Channel ID which messages from the staff chat are sent to")
        public String STAFF_CHANNEL = "STAFF_DISCORD_CHANNEL_ID_HERE";

        @Setting(value = "helpop-channel", comment = "Channel that HelpOp messages will be sent to (if enabled)")
        public String HELPOP_CHANNEL = "";
    }

}
