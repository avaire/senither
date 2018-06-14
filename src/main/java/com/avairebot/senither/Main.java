package com.avairebot.senither;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws LoginException {
        Configuration configuration = loadConfiguration();
        if (configuration == null) {
            LOGGER.info("Configuration returned null, failed to load the config.");
            shutdown(0);
        }
        new AutoSenither(configuration);
    }

    public static void shutdown(int code) {
        LOGGER.info("Shutting down process with code {}", code);
        System.exit(code);
    }

    private static Configuration loadConfiguration() {
        File file = new File("config.json");
        if (!file.exists()) {
            LOGGER.info("The config.json file was not found!");
            shutdown(0);
        }

        if (!(file.canRead() || file.canWrite())) {
            LOGGER.info("The config file cannot be read or written to!");
            System.exit(0);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return new Gson().fromJson(sb.toString(), Configuration.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
