package Bots;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static Bots.Main.printlnTime;

/**
 * Central manager used to handle the reading, saving, and management of JSON files
 */
public class ConfigManager {
    public static String configFolder = "config";
    public static HashMap<Long, JSONObject> Configs = new HashMap<>();

    public static void Init() {
        Path folder = Paths.get(configFolder);
        boolean newConfig = folder.toFile().mkdir();
        if (newConfig) {
            printlnTime("Created new config folder");
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                SaveConfigs();
            }
        };
        timer.scheduleAtFixedRate(task,60000,120000); //Actively save every 2 minutes
        printlnTime("Loaded config manager");
    }

    private static JSONObject CreateConfigObject() { //Useful base-plate config
        JSONObject defaultConfig = new JSONObject();
        defaultConfig.put("BlockedChannels",new JSONObject());
        defaultConfig.put("DJRoles",new JSONArray());
        defaultConfig.put("DJUsers",new JSONArray());
        defaultConfig.put("Users",new JSONObject());
        return defaultConfig;
    }

    private static void WriteConfig(long GuildID, JSONObject config) throws IOException { //So I don't have to write it multiple times
        String filePath = configFolder+"/"+GuildID+".json";
        FileWriter writer = new FileWriter(filePath);
        writer.write(config.toString());
        writer.close();
    }

    private static JSONObject CreateConfig(long GuildID) throws IOException { //Helper to GetConfig, defaults a guild's config
        String filePath = configFolder+"/"+GuildID+".json";
        boolean madeFile = Paths.get(filePath).toFile().createNewFile();
        if (!madeFile) {
            printlnTime("Warning: Potentially overwriting an existing config ("+GuildID+")");
        }
        JSONObject defaultConfig = CreateConfigObject();
        WriteConfig(GuildID,defaultConfig);
        return defaultConfig;
    }

    private static JSONObject ReadConfig(long GuildID) throws IOException { //Helper to GetConfig, safely fetches a guild's config
        //Get the config
        String filePath = configFolder+"/"+GuildID+".json";
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(filePath);
        JSONObject config;
        try {
            config = (JSONObject) parser.parse(reader);
        } catch (ParseException exception) { //Useless config, just discard it and start fresh
            printlnTime("Usurping config for "+GuildID+" with a generic one due to bad formatting");
            return CreateConfig(GuildID);
        }

        //Check the config to ensure it follows the format of any changes
        JSONObject baseConfig = CreateConfigObject();
        for (Object key : baseConfig.keySet()) {
            if (!config.containsKey(key)) {
                printlnTime("Config " + GuildID + " missing key " + key);
                config.put(key, baseConfig.get(key)); //Overwrite
            } else {
                if (config.get(key).getClass() != baseConfig.get(key).getClass()) {
                    printlnTime("Config " + GuildID + " has invalid value type for key " + key);
                    config.put(key, baseConfig.get(key)); //Overwrite
                }
            }
        }
        for (Object key : config.keySet()) {
            if (!baseConfig.containsKey(key)) {
                printlnTime("Config " + GuildID + " has unrecognised key " + key);
                //Purely informational - don't take automatic action
            }
        }

        //Done, store and send away
        Configs.put(GuildID,config);
        return config;
    }

    public static JSONObject GetConfig(long GuildID) { //Gets the config for the requested GuildID
        if (Configs.containsKey(GuildID)) { //Already loaded
            return Configs.get(GuildID);
        } else { //Need to load it
            String filePath = configFolder+"/"+GuildID+".json";
            Path existingConfig = Paths.get(filePath);
            if (existingConfig.toFile().exists()) { //It exists
                try {
                    return ReadConfig(GuildID);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } else { //It doesn't exist
                try {
                    return CreateConfig(GuildID);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        throw new NullPointerException(); // I guess?
    }

    public static void SaveConfigs() { //Saves all configs. Should ideally be run just before shutdown
        for (long GuildID : Configs.keySet()) {
            JSONObject config = Configs.get(GuildID);
            //printlnTime(GuildID+" - "+config);
            try {
                WriteConfig(GuildID, config);
            } catch (IOException exception) {
                printlnTime("Unable to save config for Guild "+GuildID);
                exception.printStackTrace();
            }
        }
    }
}
