package com.magitechserver.magibridge.config.categories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 27/09/2017.
 */
@ConfigSerializable
public class Replacer extends ConfigCategory {

    private Map<String, String> defaultValues;

    public Replacer() {
        defaultValues = new HashMap<>();

        defaultValues.put(":smiley:", ":)");
        defaultValues.put(":smile:", ":D");
        defaultValues.put(":joy:", ";D");
        defaultValues.put(":laughing:", "xD");
        defaultValues.put(":frowning:", ":(");
        defaultValues.put(":sob:", ";(");
        defaultValues.put(":tired_face:", "x(");
        defaultValues.put(":wink:", ";)");
        defaultValues.put(":stuck_out_tongue:", ":P");
        defaultValues.put(":stuck_out_tongue_winking_eye:", ";P");
        defaultValues.put(":stuck_out_tongue_closed_eyes:", "xP");
        defaultValues.put(":open_mouth:", ":O");
        defaultValues.put(":dizzy_face:", "xO");
        defaultValues.put(":neutral_face:", ":|");
        defaultValues.put(":sunglasses:", "B)");
        defaultValues.put(":kissing:", ":*");
        defaultValues.put(":heart:", "<3");
    }

    @Setting(value = "discord-to-mc-replacer", comment = "Text that will be replaced in messages sent TO the game\n" +
            "Format: \"word-to-replace\" = \"word-replaced\"")
    public Map<String, String> REPLACER = defaultValues;

}
