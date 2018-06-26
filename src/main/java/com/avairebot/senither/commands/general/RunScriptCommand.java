package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.utils.RoleUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RunScriptCommand extends Command {

    private static final Map<String, String> SCRIPTS = new HashMap<>();

    static {
        SCRIPTS.put("mutual", "avaire.getShardManager().getMutualGuilds(avaire.getShardManager().getUserById(\"%s\"));");
        SCRIPTS.put("mutual-shard", "Arrays.asList(avaire.getShardManager().getMutualGuilds(avaire.getShardManager().getUserById(\"%s\")).stream().map(function(guild) { return guild.getId() + \": \" + guild.getJDA().getShardInfo().getShardString() + \"\\n\";\n}).toArray());");
        SCRIPTS.put("restart", "avaire.getShardManager().restart(%s);");
    }

    public RunScriptCommand(AutoSenither app) {
        super(app);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("run");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (!event.getChannel().getType().isGuild()) {
            return;
        }

        if (!RoleUtil.hasRole(event.getMember().getRoles(), Constants.BOT_ADMIN_ROLE_NAME)) {
            return;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage("**Scripts:**\n" + String.join("\n", SCRIPTS.keySet()))
                .queue(newMessage -> newMessage.delete().queueAfter(1, TimeUnit.MINUTES));
            return;
        }

        if (args.length < 2) {
            event.getChannel().sendMessage("You must include a minimum of two arguments to this command.")
                .queue(newMessage -> newMessage.delete().queueAfter(1, TimeUnit.MINUTES));
            return;
        }

        if (!SCRIPTS.containsKey(args[0])) {
            event.getChannel().sendMessage("Invalid script name given, no script exists with the given name.")
                .queue(newMessage -> newMessage.delete().queueAfter(1, TimeUnit.MINUTES));
            return;
        }

        try {
            long value = Long.parseLong(args[1].toLowerCase());

            event.getChannel().sendMessage(
                ";eval return " + String.format(SCRIPTS.get(args[0].toLowerCase()), value)
            ).queue();
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Invalid second argument given, the 2nd argument must be a valid number.")
                .queue(newMessage -> newMessage.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    }
}
