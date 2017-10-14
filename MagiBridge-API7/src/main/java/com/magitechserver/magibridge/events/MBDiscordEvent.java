package com.magitechserver.magibridge.events;

import com.magitechserver.magibridge.api.DiscordEvent;
import net.dv8tion.jda.core.entities.Guild;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * Created by Frani on 27/09/2017.
 */
public class MBDiscordEvent extends AbstractEvent implements DiscordEvent {

    private Guild guild;
    private Cause cause;
    private boolean isCancelled;

    public MBDiscordEvent(Guild guild, Cause cause) {
        this.guild = guild;
        this.cause = cause;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
