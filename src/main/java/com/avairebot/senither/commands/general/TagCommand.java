package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.contracts.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TagCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(TagCommand.class);

    private final File directory;
    private final boolean enabled;

    public TagCommand(AutoSenither app) {
        super(app);

        directory = new File("tags");
        if (!directory.exists() || !directory.isDirectory()) {
            if (!directory.mkdirs()) {
                enabled = false;
                log.error("Failed to create the tags directory, disabling the tag command.");
            } else {
                enabled = true;
            }
        } else {
            enabled = true;
        }
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("tag", "tags");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (!enabled) {
            sendAndDeleteAfter(
                event.getChannel(),
                new EmbedBuilder()
                    .setColor(Color.decode("#EF5350"))
                    .setTitle("Command is Disabled")
                    .setDescription("This command has been disable due to missing permissions to create directories."),
                true
            );
            return;
        }

        event.getMessage().delete().queue(null, error -> {
            // Ignore the error if one was thrown
        });

        if (args.length == 0) {
            sendAndDeleteAfter(
                event.getChannel(),
                new EmbedBuilder()
                    .setColor(Color.decode("#3A71C1"))
                    .setTitle("List Tags")
                    .setDescription("`" + String.join("`, `", loadTags()) + "`")
                    .setFooter("Use !tag <name> to display the content of a tag", null)
            );
            return;
        }

        String tag = String.join("-", args);
        File file = new File(directory, tag + ".txt");

        if (!file.exists() || !file.isFile()) {
            sendAndDeleteAfter(
                event.getChannel(),
                new EmbedBuilder()
                    .setColor(Color.decode("#EF5350"))
                    .setTitle("Invalid tag given")
                    .setDescription("Invalid tag given, `" + tag + "` is not a valid tag."),
                true
            );
            return;
        }

        try {
            String content = String.join("\n", Files.readAllLines(file.toPath()));
            sendAndDeleteAfter(
                event.getChannel(),
                new EmbedBuilder()
                    .setColor(Color.decode("#3A71C1"))
                    .setDescription(content)
            );
        } catch (IOException e) {
            sendAndDeleteAfter(
                event.getChannel(),
                new EmbedBuilder()
                    .setColor(Color.decode("#EF5350"))
                    .setTitle("Failed to load tag")
                    .setDescription("Failed to load the " + tag + "` tag, try again later.\nError: " + e.getMessage()),
                true
            );
            log.error("Failed to load a tag with the name {}, error: {}", tag, e.getMessage(), e);
        }
    }

    private void sendAndDeleteAfter(MessageChannel channel, EmbedBuilder builder, boolean delete) {
        channel.sendMessage(builder.build()).queue(message -> {
            if (delete) {
                message.delete().queueAfter(1, TimeUnit.MINUTES, null, error -> {
                    // Ignore the error if one is thrown
                });
            }
        });
    }

    private void sendAndDeleteAfter(MessageChannel channel, EmbedBuilder builder) {
        sendAndDeleteAfter(channel, builder, false);
    }

    @SuppressWarnings("ConstantConditions")
    private List<String> loadTags() {
        List<String> files = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                files.add(file.getName().substring(0, file.getName().length() - 4));
            }
        }
        return files;
    }
}
