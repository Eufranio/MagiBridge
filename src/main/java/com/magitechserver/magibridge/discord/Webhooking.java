package com.magitechserver.magibridge.discord;

import com.magitechserver.magibridge.MagiBridge;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Frani on 15/07/2017.
 */
public class Webhooking {

    public static void sendWebhookMessage(String hook, String player, String message, String channelID) {

        String content = message.replaceAll("&([0-9a-fA-FlLkKrR])", "");
        List<String> usersMentioned = new ArrayList<>();
        Arrays.stream(content.split(" ")).filter(word ->
                word.startsWith("@")).forEach(mention ->
                usersMentioned.add(mention.substring(1)));

        if (!usersMentioned.isEmpty()) {
            for (String user : usersMentioned) {
                List<User> users = MagiBridge.jda.getUsersByName(user, true);
                if (!users.isEmpty()) {
                    content = content.replaceAll("@" + user, "<@" + users.get(0).getId() + ">");
                }
            }
        }

        Player p = Sponge.getServer().getPlayer(player).get();
        String format = MagiBridge.getConfig().MESSAGES.WEBHOOK_PICTURE_URL.replace("%player%", player).replace("%uuid%", p.getUniqueId().toString());
        final String c = content;
        Task.builder()
                .async()
                .execute(() -> {
                    Webhook webhook = getWebhook(channelID);
                    if (webhook == null) return;
                    sendWebhook(webhook, WebhookContent.of(format, hook, c));
                })
                .submit(MagiBridge.getInstance());
    }

    public static void sendWebhook(String channel_id, WebhookContent webhook) {
        Task.builder()
                .async()
                .execute(() -> sendWebhook(getWebhook(channel_id), webhook))
                .submit(MagiBridge.getInstance());
    }

    public static void sendWebhook(Webhook webhook, WebhookContent content) {
        JSONObject json = new JSONObject();
        json.put("content", content.message);
        json.put("username", content.name.replaceAll("&([0-9a-fA-FlLkKrR])", ""));
        json.put("avatar_url", content.avatarUrl);
        try {
            Unirest.post(webhook.getUrl())
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asJsonAsync();
        } catch (Exception e) {
            MagiBridge.getLogger().error("Error delivering Webhook request: ");
            e.printStackTrace();
        }
    }

    private static Webhook getWebhook(String channelID) {
        TextChannel channel = MagiBridge.jda.getTextChannelById(channelID);
        if (!channel.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            MagiBridge.getLogger().error("The bot does not have the MANAGE WEBHOOKS permission, so it can't create webhooks!");
            MagiBridge.getLogger().error("Please give it or disable the use-webhooks feature!");
            return null;
        }

        Webhook webhook = channel.getGuild()
                .retrieveWebhooks().complete().stream().filter(wh ->
                        wh.getName().equals("MB: " + channel.getName())).findFirst().orElse(null);

        if (webhook == null) {
            webhook = channel.createWebhook("MB: " + channel.getName()).complete();
        }

        return webhook;
    }

}
