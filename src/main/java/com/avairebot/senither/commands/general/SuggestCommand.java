package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import com.avairebot.senither.contracts.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SuggestCommand extends Command {

    public SuggestCommand(AutoSenither app) {
        super(app);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("suggest");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        TextChannel textChannelById = app.getShardManager().getTextChannelById(Constants.SUGGESTION_CHANNEL_ID);
        if (textChannelById == null) {
            return;
        }

        User author = event.getAuthor();
        String[] parts = event.getMessage().getContentRaw().split(" ");
        String message = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        if (message.length() < 30) {
            event.getMessage().getChannel().sendMessage("You must include at least 30 characters or more in your suggestion.").queue(errorMessage -> {
                errorMessage.delete().queueAfter(25, TimeUnit.SECONDS);
            });
            return;
        }

        textChannelById.sendMessage(new EmbedBuilder()
            .setAuthor("Suggestion by " + String.format("%s#%s",
                author.getName(), author.getDiscriminator()
            ), null, author.getEffectiveAvatarUrl())
            .setDescription(message)
            .setFooter("User ID: " + author.getId(), null)
            .setTimestamp(Instant.now())
            .build())
            .queue(newMessage -> {
                event.getMessage().getChannel().sendMessage(
                    "<:tickYes:319985232306765825> Thanks for your suggestion, you can now see your suggestion in the <#" + Constants.SUGGESTION_CHANNEL_ID + "> channel!"
                ).queue(thanksMessage -> {
                    thanksMessage.delete().queueAfter(25, TimeUnit.SECONDS);
                });

                newMessage.addReaction(app.getShardManager().getEmoteById(Constants.YES_EMOTE_ID)).queue();
                newMessage.addReaction(app.getShardManager().getEmoteById(Constants.NO_EMOTE_ID)).queueAfter(2500, TimeUnit.MILLISECONDS);
            });
    }
}
