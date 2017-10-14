package com.magitechserver.magibridge.util;

import me.lucko.luckperms.LuckPerms;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.Collections;
import java.util.HashMap;


/**
 * Created by Frani on 20/07/2017.
 */
public class GroupUtil {

    private static Subject getGroup(Player player) {
        PermissionService ps = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
        HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();
        try {
            for (SubjectReference sub : player.getParents()) {
                if (sub.getCollectionIdentifier().equals(ps.getGroupSubjects().getIdentifier())) {
                    Subject subj = sub.resolve().get();
                    subs.put(subj.getParents().size(), subj);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return subs.isEmpty() ? null : subs.get(Collections.max(subs.keySet()));
    }

    public static String getHighestGroup(Player player) {
        return getGroup(player) != null ? getGroup(player).getFriendlyIdentifier().orElse("") : "";
    }
}
