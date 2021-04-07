package com.magitechserver.magibridge.config.categories;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class Replacer {

    @Setting(value = "discord-to-mc-replacer", comment = "Text that will be replaced in messages sent TO the game\n" +
            "Format: \"word-to-replace\" = \"word-replaced\"")
    public Map<String, String> REPLACER = new HashMap<String, String>() {{
        put(":smiley:", ":)");
        put(":smile:", ":D");
        put(":joy:", ";D");
        put(":laughing:", "xD");
        put(":frowning:", ":(");
        put(":sob:", ";(");
        put(":tired_face:", "x(");
        put(":wink:", ";)");
        put(":stuck_out_tongue:", ":P");
        put(":stuck_out_tongue_winking_eye:", ";P");
        put(":stuck_out_tongue_closed_eyes:", "xP");
        put(":open_mouth:", ":O");
        put(":dizzy_face:", "xO");
        put(":neutral_face:", ":|");
        put(":sunglasses:", "B)");
        put(":kissing:", ":*");
        put(":heart:", "<3");
    }};

    @Setting(value = "mc-to-discord-replacer", comment = "Text that will be replaced in messages sent from the game to Discord\n" +
            "Format: \"word-to-replace\" = \"word-replaced\"")
    public Map<String, String> mcToDiscordReplacer = new HashMap<String, String>() {{
        put(":)", ":smiley:");
        put(":D", ":smile:");
    }};

}
