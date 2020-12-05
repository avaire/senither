package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.Constants;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.utils.RoleUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RunScriptCommand extends Command {

    private static final Map<String, Script> SCRIPTS = new HashMap<>();

    {
        SCRIPTS.put("mutual", new Script(
            ";eval return avaire.getShardManager().getMutualGuilds(avaire.getShardManager().retrieveUserById(\"%s\").complete());",
            Long::parseLong
        ));
        SCRIPTS.put("mutual-shard", new Script(
            ";eval return Arrays.asList(avaire.getShardManager().getMutualGuilds(avaire.getShardManager().retrieveUserById(\"%s\").complete()).stream().map(function(guild) { return guild.getId() + \": \" + guild.getJDA().getShardInfo().getShardString() + \"\\n\";\n}).toArray());",
            Long::parseLong
        ));
        SCRIPTS.put("restart", new Script(
            ";eval return avaire.getShardManager().restart(%s);",
            Long::parseLong
        ));
        SCRIPTS.put("has-voted", new Script(
            ";eval return avaire.getVoteManager().hasVoted(avaire.getShardManager().retrieveUserById(\"%s\").complete());",
            Long::parseLong
        ));
        SCRIPTS.put("register-vote", new Script(
            ";eval return avaire.getVoteManager().registerVoteFor(avaire.getShardManager().retrieveUserById(\"%s\").complete(), 1);",
            Long::parseLong
        ));
        SCRIPTS.put("restart", new Script(
            ";restart %s",
            String::valueOf
        ));
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
        if (!event.getChannel().getType().isGuild() || event.getMember() == null) {
            return;
        }

        if (!RoleUtil.hasRole(event.getMember().getRoles(), Constants.BOT_ADMIN_ROLE_ID)) {
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
            Script script = SCRIPTS.get(args[0].toLowerCase());

            event.getChannel().sendMessage(String.format(
                script.getAction(), script.getFunction().parse(args[1])
            )).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("Invalid second argument given, the 2nd argument must be a valid number.")
                .queue(newMessage -> newMessage.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    }

    @FunctionalInterface
    interface ScriptAction {
        Object parse(String value) throws Exception;
    }

    class Script {
        private final String action;
        private final ScriptAction function;

        Script(String action, ScriptAction function) {
            this.action = action;
            this.function = function;
        }

        public String getAction() {
            return action;
        }

        public ScriptAction getFunction() {
            return function;
        }
    }
}
