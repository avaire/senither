package com.avairebot.senither.commands.general;

import com.avairebot.senither.AutoSenither;
import com.avairebot.senither.contracts.commands.Command;
import com.avairebot.senither.time.Carbon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UptimeCommand extends Command {

    private static final DecimalFormat niceFormat = new DecimalFormat("#,##0");

    public UptimeCommand(AutoSenither app) {
        super(app);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("uptime");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (event.getMember() == null || !isStaff(event.getMember())) {
            return;
        }

        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

        int uptimeInSeconds = (int) rb.getUptime() / 1000;
        Carbon time = Carbon.now().subSeconds(uptimeInSeconds);

        event.getChannel().sendMessage(new EmbedBuilder()
            .setColor(Color.decode("#2ECC71"))
            .setDescription(formatUptimeNicely(uptimeInSeconds))
            .setFooter(time.format("EEEEEEEE, dd MMM yyyy HH:mm:ss z"), null)
            .setTimestamp(Instant.now())
            .build())
            .queue();
    }

    private boolean isStaff(Member member) {
        for (Role role : member.getRoles()) {
            if (role.getName().toLowerCase().contains("staff")) {
                return true;
            }
        }
        return false;
    }

    private String formatUptimeNicely(int total) {
        long days = TimeUnit.SECONDS.toDays(total);
        total -= TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(total);
        total -= TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(total);
        total -= TimeUnit.MINUTES.toSeconds(minutes);

        long seconds = TimeUnit.SECONDS.toSeconds(total);

        if (days != 0) {
            return String.format("%s, %s, %s, and %s.",
                appendIfMultiple(days, "day"),
                appendIfMultiple(hours, "hour"),
                appendIfMultiple(minutes, "minute"),
                appendIfMultiple(seconds, "second")
            );
        }

        if (hours != 0) {
            return String.format("%s, %s, and %s.",
                appendIfMultiple(hours, "hour"),
                appendIfMultiple(minutes, "minute"),
                appendIfMultiple(seconds, "second")
            );
        }

        if (minutes != 0) {
            return String.format("%s, and %s.",
                appendIfMultiple(minutes, "minute"),
                appendIfMultiple(seconds, "second")
            );
        }

        return String.format("%s.", appendIfMultiple(seconds, "second"));
    }

    private String appendIfMultiple(long value, String singularType) {
        return niceFormat.format(value) + " " + (value == 1 ? singularType : singularType + "s");
    }
}
