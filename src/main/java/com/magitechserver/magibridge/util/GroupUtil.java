package com.magitechserver.magibridge.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


/**
 * Created by Frani on 20/07/2017.
 */
public class GroupUtil {

    public static String getHighestGroup(Player player) {
        try {
            if (!Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).isPresent()) return "";
            PermissionService ps = Sponge.getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
            HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();
            for (SubjectReference sub : player.getParents()) {
                if (sub.getCollectionIdentifier().equals(ps.getGroupSubjects().getIdentifier()) && (sub.getSubjectIdentifier() != null)) {
                    Subject subj = sub.resolve().get();
                    subs.put(subj.getParents().size(), subj);
                }
            }
            return subs.isEmpty() ? "" : subs.get(Collections.max(subs.keySet())).getFriendlyIdentifier().isPresent() ? subs.get(Collections.max(subs.keySet())).getFriendlyIdentifier().get() : "";
        } catch (InterruptedException | ExecutionException e) {
        }
        return "";
    }

}
