package com.avairebot.senither.handlers;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import com.avairebot.senither.commands.CommandHandler;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.contracts.handlers.EventListener;
import com.avairebot.senither.utils.ChatFilterUtil;
import com.avairebot.senither.utils.RoleUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MessageEventListener extends EventListener {

    private static final Cache<Long, MessageCache> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(6, TimeUnit.HOURS)
        .build();

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEventListener.class);
    private final static String COMMAND_OUTPUT = "Executing Command \"%command%\""
        + "\n\t\tUser:\t %author%"
        + "\n\t\tServer:\t %server%"
        + "\n\t\tChannel: %channel%"
        + "\n\t\tMessage: %message%";

    public MessageEventListener(AutoSenither app) {
        super(app);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            cache.put(event.getMessage().getIdLong(), new MessageCache(event));
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        @Nullable MessageCache oldContent = cache.getIfPresent(event.getMessage().getIdLong());
        if (oldContent == null) {
            return;
        }

        TextChannel messageLogChannel = app.getShardManager().getTextChannelById(Constants.MESSAGE_LOG_ID);
        if (messageLogChannel == null) {
            return;
        }

        messageLogChannel.sendMessage(new EmbedBuilder()
            .setColor(Color.decode("#2D7DD7"))
            .setTitle("Edited a message by " + buildUserString(event.getAuthor()))
            .addField("Before", oldContent.message, false)
            .addField("After", event.getMessage().getContentRaw(), false)
            .setFooter("#" + event.getChannel().getName() + " (" + event.getChannel().getId() + ")", null)
            .build()
        ).queue();
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        @Nullable MessageCache oldContent = cache.getIfPresent(event.getMessageIdLong());
        if (oldContent == null) {
            return;
        }

        TextChannel messageLogChannel = app.getShardManager().getTextChannelById(Constants.MESSAGE_LOG_ID);
        if (messageLogChannel == null) {
            return;
        }

        cache.invalidate(event.getMessageIdLong());

        if (oldContent.createdAt + 2500 > System.currentTimeMillis()) {
            return;
        }

        messageLogChannel.sendMessage(new EmbedBuilder()
            .setColor(Color.decode("#E84A1F"))
            .setTitle("Deleted message in #" + event.getChannel().getName() + " (" + event.getChannel().getId() + ")")
            .addField("Message Author", oldContent.author + " (" + oldContent.authorId + ")", false)
            .setDescription(oldContent.message)
            .build()
        ).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && event.getAuthor().getIdLong() != Constants.AVAIRE_BOT_ID) {
            return;
        }

        if (!event.getChannelType().isGuild()) {
            event.getChannel().sendMessage(
                "I'm a bot, if you have any questions or concerns about AvaIre or related projects, try and DM <@88739639380172800>."
            ).queue();
            return;
        }

        if (!RoleUtil.hasRole(event.getMember().getRoles(), Constants.STAFF_ROLE_NAME) && ChatFilterUtil.isAdvertisement(event)) {
            TextChannel messageLogChannel = app.getShardManager().getTextChannelById(Constants.MESSAGE_LOG_ID);
            if (messageLogChannel != null) {
                messageLogChannel.sendMessage(new EmbedBuilder()
                    .setColor(Color.decode("#EEDE28"))
                    .setTitle("Blocked advertisement by " + event.getAuthor().getName() + "(" + event.getAuthor().getId() + ")")
                    .setDescription(event.getMessage().getContentRaw())
                    .setFooter("#" + event.getChannel().getName() + " (" + event.getChannel().getId() + ")", null)
                    .build()
                ).queue();
            }
            cache.invalidate(event.getMessage().getIdLong());
            event.getMessage().delete().queue();
            return;
        }

        Command command = CommandHandler.getCommand(event.getMessage().getContentRaw());
        if (command != null) {
            LOGGER.info(COMMAND_OUTPUT
                .replace("%command%", command.getClass().getSimpleName())
                .replace("%author%", generateUsername(event.getMessage()))
                .replace("%channel%", generateChannel(event.getMessage()))
                .replace("%server%", generateServer(event.getMessage()))
                .replace("%message%", event.getMessage().getContentRaw())
            );
            CommandHandler.invokeCommand(event, command);
        }

        if (event.getChannel().getIdLong() == Constants.BETA_SANDBOX_ID) {
            if (RoleUtil.hasRole(event.getMember().getRoles(), Constants.STAFF_ROLE_NAME)) {
                return;
            }

            Member betaBot = event.getGuild().getMemberById(Constants.BETA_BOT_ID);
            if (betaBot != null && betaBot.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                event.getMessage().getChannel().sendMessage(
                    "The beta bot is currently offline, you can test the live bot in <#284100870440878081>"
                ).queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        }
    }

    private String generateUsername(Message message) {
        return String.format("%s#%s [%s]",
            message.getAuthor().getName(),
            message.getAuthor().getDiscriminator(),
            message.getAuthor().getId()
        );
    }

    private String generateServer(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
            message.getGuild().getName(),
            message.getGuild().getId()
        );
    }

    private CharSequence generateChannel(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
            message.getChannel().getName(),
            message.getChannel().getId()
        );
    }

    private String buildUserString(User user) {
        return user.getName() + "#" + user.getDiscriminator() + " (" + user.getId() + ")";
    }

    private class MessageCache {
        private final String message;
        private final long messageId;
        private final long authorId;
        private final String author;
        private final String authorUsername;
        private final String authorDiscriminator;
        private final long createdAt;

        MessageCache(GuildMessageReceivedEvent event) {
            this(event.getMessage(), event.getAuthor());
        }

        MessageCache(GuildMessageUpdateEvent event) {
            this(event.getMessage(), event.getAuthor());
        }

        MessageCache(Message message, User author) {
            this.message = message.getContentRaw();
            this.messageId = message.getIdLong();
            this.authorId = author.getIdLong();
            this.author = author.getName() + "#" + author.getDiscriminator();
            this.authorUsername = author.getName();
            this.authorDiscriminator = author.getDiscriminator();
            this.createdAt = System.currentTimeMillis();
        }
    }
}
