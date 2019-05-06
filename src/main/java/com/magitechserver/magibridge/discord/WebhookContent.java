package com.magitechserver.magibridge.discord;

/**
 * Created by Frani on 27/09/2017.
 */
public class WebhookContent {

    public String avatarUrl;

    public String name;

    public String message;

    private WebhookContent(String avatarUrl, String name, String message) {
        this.avatarUrl = avatarUrl;
        this.name = name;
        this.message = message;
    }

    public static WebhookContent of(String avatarUrl, String name, String message) {
        return new WebhookContent(avatarUrl, name, message);
    }

}
