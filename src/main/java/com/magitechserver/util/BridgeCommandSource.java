package com.magitechserver.util;

import com.magitechserver.DiscordHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Frani on 10/07/2017.
 */
public abstract class BridgeCommandSource implements CommandSource {

    private final CommandSource actualSource;

    private String channel;

    public void channelTo(String c) {
        this.channel = c;
    }

    @Override
    public void sendMessage(Text message) {
        DiscordHandler.sendMessageToChannel(channel, message.toPlain());
        this.channel = null;
    }

    public BridgeCommandSource(CommandSource actualSource) {
        this.actualSource = actualSource;
    }

    @Override
    public void sendMessages(Iterable<Text> messages) {
        this.actualSource.sendMessages(messages);
    }

    @Override
    public void sendMessages(Text... messages) {
        this.actualSource.sendMessages(messages);
    }

    @Override
    public String getName() {
        return this.actualSource.getName();
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.actualSource.getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        this.actualSource.setMessageChannel(channel);
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return this.actualSource.getCommandSource();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.actualSource.getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return this.actualSource.getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return this.actualSource.getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return this.actualSource.hasPermission(contexts, permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.actualSource.hasPermission(permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return this.actualSource.getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(Subject parent) {
        return this.actualSource.isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return this.actualSource.isChildOf(contexts, parent);
    }

    @Override
    public List<Subject> getParents() {
        return this.actualSource.getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return this.actualSource.getParents(contexts);
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return this.actualSource.getOption(contexts, key);
    }

    @Override
    public String getIdentifier() {
        return this.actualSource.getIdentifier();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return this.actualSource.getActiveContexts();
    }

}
