package com.avairebot.senither.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilterUtil {

    private static final int MAX_REDIRECTS = 10;

    private static final LoadingCache<String, String> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(2, TimeUnit.HOURS)
        .build(new CacheLoader<String, String>() {
            @Override
            public String load(@Nonnull String key) throws Exception {
                return getTrueUrl(key, 0);
            }
        });

    private static final Pattern urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );

    public static boolean isAdvertisement(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        Matcher matcher = urlPattern.matcher(message);

        while (matcher.find()) {
            String url = CacheUtil.getUncheckedUnwrapped(cache, message.substring(matcher.start(1), matcher.end()));

            if (url.contains("discordapp.com/invite/")) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    private static String getTrueUrl(String url, int redirectsAttempts) throws IOException {
        if (redirectsAttempts > MAX_REDIRECTS) {
            return url;
        }

        HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setInstanceFollowRedirects(false);
        con.connect();

        if (con.getHeaderField("Location") == null) {
            return url;
        }
        return getTrueUrl(con.getHeaderField("Location"), ++redirectsAttempts);
    }
}
