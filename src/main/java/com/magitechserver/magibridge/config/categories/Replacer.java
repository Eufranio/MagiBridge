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

    private Map<String, String> defaultValues;

    public Replacer() {
        REPLACER.put(":smiley:", ":)");
        REPLACER.put(":smile:", ":D");
        REPLACER.put(":joy:", ";D");
        REPLACER.put(":laughing:", "xD");
        REPLACER.put(":frowning:", ":(");
        REPLACER.put(":sob:", ";(");
        REPLACER.put(":tired_face:", "x(");
        REPLACER.put(":wink:", ";)");
        REPLACER.put(":stuck_out_tongue:", ":P");
        REPLACER.put(":stuck_out_tongue_winking_eye:", ";P");
        REPLACER.put(":stuck_out_tongue_closed_eyes:", "xP");
        REPLACER.put(":open_mouth:", ":O");
        REPLACER.put(":dizzy_face:", "xO");
        REPLACER.put(":neutral_face:", ":|");
        REPLACER.put(":sunglasses:", "B)");
        REPLACER.put(":kissing:", ":*");
        REPLACER.put(":heart:", "<3");
    }

    @Setting(value = "discord-to-mc-replacer", comment = "Text that will be replaced in messages sent TO the game\n" +
            "Format: \"word-to-replace\" = \"word-replaced\"")
    public Map<String, String> REPLACER = Maps.newHashMap();

}
