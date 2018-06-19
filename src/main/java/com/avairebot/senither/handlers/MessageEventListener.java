package com.avairebot.senither.handlers;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.commands.CommandHandler;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.contracts.handlers.EventListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEventListener extends EventListener {

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
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getChannelType().isGuild()) {
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
}
