package com.magitechserver.magibridge.config.categories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class Messages {

    @Setting(value = "prefix", comment = "Prefix that will be sent before the actual message, with a hover and clickable link")
    public PrefixCategory PREFIX = new PrefixCategory();
    @Setting(value = "player-list-name", comment = "Format of the player name displayed in the channel list command (!online)")
    public String PLAYER_LIST_NAME = "%player%";
    @Setting(value = "no-players-message", comment = "Message shown when there are no players in the server \n" +
            "and the list command is executed")
    public String NO_PLAYERS = "**There are no players online!**";
    @Setting(value = "console-command-no-permisson", comment = "No permission message (for console command)")
    public String CONSOLE_NO_PERMISSION = "**You don't have permission to use this command!**";
    @Setting(value = "server-to-discord-format", comment = "Format of messages sent FROM the server TO discord")
    public String SERVER_TO_DISCORD_FORMAT = "**%player%**: %message%";
    @Setting(value = "server-to-discord-staff-format", comment = "Format of messages sent FROM the server staff channel TO the discord staff channel\n" +
            "Used only when Nucleus is running and enabled")
    public String SERVER_TO_DISCORD_STAFF_FORMAT = "**%player%**: %message%";
    @Setting(value = "discord-to-server-format", comment = "Format of the messsage sent from Discord to the server")
    public String DISCORD_TO_SERVER_FORMAT = "&f%user%&7: &7%message%";
    @Setting(value = "discord-to-server-staff-format", comment = "Format of the messages sent from the Discord Staff channel to the server\n" +
            "Node: This format is used ONLY when using the Nucleus hook!")
    public String DISCORD_TO_SERVER_STAFF_FORMAT = "&f%user%&7: &7%message%";
    @Setting(value = "server-starting-message", comment = "Message that will be sent to the discord-main-channel when the server starts")
    public String SERVER_STARTING = "**The server is starting!**";
    @Setting(value = "server-stopping-message", comment = "Message that will be sent to the discord-main-channel when the server stops")
    public String SERVER_STOPPING = "**The server is stopping!**";
    @Setting(value = "player-join-message", comment = "Message that will be sent to the discord-main-channel when a player joins the server")
    public String PLAYER_JOIN = "**%player%** joined the server";
    @Setting(value = "player-quit-message", comment = "Message that will be sent to the discord-main-channel when a player leaves the server")
    public String PLAYER_QUIT = "**Bye, %player%!**";
    @Setting(value = "channel-topic-offline", comment = "Message that will be set in the main discord channel topic when the server goes offline")
    public String OFFLINE_TOPIC = "The server is currently offline!";
    @Setting(value = "channel-topic-message", comment = "Message that will be set in the main discord channel topic every X seconds\n" +
            "Supports %tps%, %players%, %maxplayers%, %daysonline%, %hoursonline% and %minutesonline%")
    public String TOPIC_MESSAGE = "%players%/%maxplayers% players online | TPS: %tps% | Server online for %daysonline% days,  %hoursonline% hours and %minutesonline% minutes!";
    @Setting(value = "death-message", comment = "Message that will be sent to the main discord channel when a player dies")
    public String DEATH_MESSAGE = "**Bad day for %player%: %deathmessage%**";
    @Setting(value = "advancement-message", comment = "Message that will be sent to the main discord channel when a player receives an advancement\n" +
            "Supports %advancement%")
    public String ADVANCEMENT_MESSAGE = "**%player% got a new advancement: %advancement%**";
    @Setting(value = "new-players-message", comment = " Message that will be sent to the main discord channel when a new player joins the server")
    public String NEW_PLAYERS_MESSAGE = "**Enjoy playing on our server, %player%!**";
    @Setting(value = "webhook-name", comment = "Format of the name of the webhooks that will send messages to Discord, if enabled")
    public String WEBHOOK_NAME = "[%prefix%] %player%";
    @Setting(value = "webhook-picture-url", comment = "URL that webhooks should get the skin picture from\n" +
            "Note: the link SHOULD return a valid .png picture from the request!\n" +
            "Supports %player%")
    public String WEBHOOK_PICTURE_URL = "https://crafatar.com/avatars/%uuid%?default=MHF_Alex";
    @Setting(value = "no-role-placeholder", comment = "Text that will be used in %toprole% when the user don't have any roles (besides @everyone)\n" +
            "Use only if you need. If you don't, leave this option as \"\"")
    public String NO_ROLE_PLACEHOLDER = "";
    @Setting(value = "bot-playing-status", comment = "Game that will be displayed in the bot's game status")
    public String BOT_GAME_STATUS = "Playing on a nice server!";
    @Setting(value = "attachment-name", comment = "Name of the attachment tag shown in-game when someone sends an attachment to the Discord channel")
    public String ATTACHMENT_NAME = "[Attachment]";
    @Setting(value = "afk", comment = "Messages sent to Discord when a player goes/retuns AFK, if enabled")
    public AFKCategory AFK = new AFKCategory();

    @ConfigSerializable
    public static class PrefixCategory {

        @Setting(value = "enabled")
        public boolean ENABLED = true;

        @Setting(value = "text")
        public String TEXT = "&6[Discord]";

        @Setting(value = "hover")
        public String HOVER = "&bClick do join our Discord!";

        @Setting(value = "link")
        public String LINK = "https://github.com/Eufranio/MagiBridge";

    }

    @ConfigSerializable
    public static class AFKCategory {

        @Setting(value = "enabled")
        public boolean AFK_ENABLED = false;

        @Setting(value = "going", comment = "Message sent when a player goes AFK")
        public String GOING = "%player% is now AFK";

        @Setting(value = "returning", comment = "Message sent when a player is not AFK anymore")
        public String RETURNING = "%player% is not AFK anymore";

    }
}
