<p align="center">
  <img src="https://user-images.githubusercontent.com/2921172/28250066-27f28466-6a2f-11e7-81fb-98cfee3a7313.png" width="300" alt="MagiBridge Logo"/>
</p>
<p align="center">MagiBridge logo, made by <a href="https://github.com/fcpwiz">fcpwiz</a></p>

# MagiBridge, by Eufranio
MagiBridge is a Sponge plugin that creates a Discord <-> Minecraft chat bridge, forwarding messages from/to both sides, through JDA and supported chat plugins.

## How to MagiBridge:
1) Go to https://discordapp.com/developers/applications/me and create a new App
2) Scroll down and click on "Create a Bot User"
3) Go to the **Bot** section, reveal the Token and copy-paste it into the bot-token="..." field in the `config/magibridge/MagiBridge.conf` file
4) **In the Privileged Gateway Intents section, make sure to ENABLE the SERVER MEMBERS INTENT. Without it, MagiBridge cannot track the discord users.
5) Create a new Discord Server.  It will have a #general channel.  You can create other channels.
6) Add the bot to your server: Find the Client ID of the new App, and replace the CLIENTID in the following link with your bot's client ID in
https://discordapp.com/oauth2/authorize?client_id=CLIENTID&scope=bot (for example, if you bot's Client ID is 168682538028630017, your link would be https://discordapp.com/oauth2/authorize?client_id=168682538028630017&scope=bot), select Your Server from the drop-down menu, and Authorize. You must have the Manage Server permission to add a bot to a server!
7) On _Server Settings_, in _Roles_, create a _New Role_ named e.g. "bot", give it the _Manage Channels_ permission, and in _Members_ [grant your Bot that role](https://support.discordapp.com/hc/en-us/articles/206029707).
8) [Enable Discord Developer Mode](https://discordia.me/developer-mode) and [copy-paste the channel ID you want to use](https://support.discordapp.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-) into the main-discord-channel=" "

8) Make sure you have the compatibility features enabled and properly configured in `MagiBridge.conf` for Nucleus, UltimateChat and Boop if you're using them
9) Configure the rest of the stuff you want and reload the plugin/start the server
10) Profit

## Features:
* Link Nucleus/UltimateChat channels to discord channels (sends messages from the in-game chat to discord and vice versa)
* Link as many UltimateChat channels you want
* Sends server start/stop messages to Discord
* Respects Nucleus' staff channel (forward chat from it only to the discord staff channel and vice versa)
* Run console commands from Discord (by default, !cmd <command>), if you have the defined Discord role
* Sends join/quit messages to Discord
* Includes a player count command (by default, !online). It will show you a nice list of players online
* Per-channel message format override: you can set different message templates for every channel you want
* Adds the **/mb <channel> <message>** that sends a raw message to a specific channel using the bot. Admin only! (the permission is magibridge.admin.broadcast)
* Respects the channels the bot is listening for the !online and console commands
* Updates the channel topic with info like the server TPS, online player count, uptime and etc every second
* Sends achievement get messages to Discord
* Sends death messages to Discord
* Supports getting the player's prefix in messages
* Supports Webhooks: if enabled, instead a bot sending the messages, webhooks will. They will have the player's skin and name. Try it out!
* AFK messages: send messages on AFK events to Discord!

**Note**: almost all messages and/or commands are completely translatable/modifiables. You can change them by modifying the message string of the message in the config!

### Permissions
* `magibridge.silentjoin` - doesn't trigger a join message when the player joins the server
* `magibridge.silentquit` - doesn't trigger a quit message when the player leaves the server
* `magibridge.everyone` - allow the player to use @everyone on messages sent to Discord
* `magibridge.admin.broadcast` - allows the use of the `/mb <channel> <message>` command to send messages to Discord
* `magibridge.chat` (granted by default) - allows messages from the player to be sent to Discord
* `magibridge.mention` (granted by default) - allows players to mention discord users

### Supported plugins:
[UltimateChat, from FabioZumbi12](https://forums.spongepowered.org/t/thought-balloon-ultimatechat-v-1-7-0-api-5-6), [Nucleus, from dualspiral](https://ore.spongepowered.org/Nucleus/Nucleus)

### Developers
You can add MagiBridge to your workspace by using Jitpack:
```
dependencies {
    ...
    compile 'com.github.Eufranio:MagiBridge:api-7-SNAPSHOT'
}

repositories {
    ...
    maven { url 'https://jitpack.io' }
}
```

If you're searching for the API, take a look in the [Developers Wiki page](https://github.com/Eufranio/MagiBridge/wiki/Developers).

### Planned support:
None, suggest one if you want in the Issues tab =)

### Download
You can download MagiBridge from the [releases tab](https://github.com/Eufranio/MagiBridge/releases).

### Issues / Suggestions
You can suggest additions or report issues by [creating a ticket](https://github.com/Eufranio/MagiBridge/issues/new) in the Issues tab, and I'll try to fix it asap.

### Support me!
If you like the plugin, consider donating via PayPal to eufraniow@gmail.com

