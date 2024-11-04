package net.loudcats.wyatt.skidgen;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

   // public static String greeting = "Hello World";
    public static boolean debugMessages = false;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);


        //greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
debugMessages = configuration.getBoolean("Show debug messages?", Configuration.CATEGORY_GENERAL, false, "spams the console with every part of chunk generation for the Modern world type to make sure everything gets called.");
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
