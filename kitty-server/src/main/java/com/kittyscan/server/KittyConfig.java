package com.kittyscan.server;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class KittyConfig {
    private static final String HEADER = "This is the main configuration file for KittyPaper.\n"
            + "Website: https://kittypaper.com/ \n"
            + "Docs: https://kittypaper.com/docs \n";

    private static File CONFIG_FILE;
    public static YamlConfiguration config;
    public static boolean verbose;

    public static void init() {
        File configFile = new File("kittypaper.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        CONFIG_FILE = configFile;
        config = new YamlConfiguration();
        try {
            config.load(CONFIG_FILE);
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load kittypaper.yml, please correct your syntax errors", ex);
            throw Throwables.propagate(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        config.options().header(HEADER);
        config.options().copyDefaults(true);
        verbose = getBoolean("verbose", false);

        readConfig(KittyConfig.class, null);

        if (enableDashboard) KittyDash.run();
        if (enableSuspiciousReporting) KittyStats.run();
    }

    static void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException ex) {
                        throw Throwables.propagate(ex.getCause());
                    } catch (Exception ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Error invoking " + method, ex);
                    }
                }
            }
        }

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }
    }

    private static void set(String path, Object val) {
        config.addDefault(path, val);
        config.set(path, val);
    }

    private static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, config.getString(path));
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, config.getBoolean(path));
    }

    private static double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, config.getDouble(path));
    }

    private static int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, config.getInt(path));
    }

    private static <T> List<?> getList(String path, T def) {
        config.addDefault(path, def);
        return config.getList(path, config.getList(path));
    }

    static Map<String, Object> getMap(String path, Map<String, Object> def) {
        if (def != null && config.getConfigurationSection(path) == null) {
            config.addDefault(path, def);
            return def;
        }
        return toMap(config.getConfigurationSection(path));
    }

    private static Map<String, Object> toMap(ConfigurationSection section) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object obj = section.get(key);
                if (obj != null) {
                    builder.put(key, obj instanceof ConfigurationSection val ? toMap(val) : obj);
                }
            }
        }
        return builder.build();
    }

    public static boolean anonymizePlayerListing = true;
    private static void loadAnonymizePlayerListing() {
        anonymizePlayerListing = getBoolean("anonymize-player-listing", true);
    }

    public static boolean enableDashboard = true;
    private static void loadEnableDashboard() {
        enableDashboard = getBoolean("enable-dashboard", true);
    }

    public static boolean enableSuspiciousReporting = true;
    private static void loadEnableReporting() {
        enableSuspiciousReporting = getBoolean("enable-reporting", true);
    }

    public static ArrayList<BlocklistConfig> blocklists = new ArrayList<>();
    private static void loadBlocklists() {
        blocklists.clear();
        List<?> list = getList("blocklists", new ArrayList<>(
            List.of(
                ImmutableMap.of(
                    "url", "https://raw.githubusercontent.com/LillySchramm/KittyScanBlocklist/refs/heads/main/ips-24.txt",
                    "refresh-interval-minutes", 60,
                    "subnet-mask", 24
                )
        )));
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> map) {
                String url = (String) map.get("url");
                int refreshIntervalMinutes = (int) map.get("refresh-interval-minutes");
                int subnetMask = (int) map.get("subnet-mask");
                blocklists.add(new BlocklistConfig(url, refreshIntervalMinutes, subnetMask));
            }
        }

        for (BlocklistConfig blConfig : blocklists) {
            blConfig.update();
        }
    }

    public static boolean isIpBlocklisted(String ip) {
        for (BlocklistConfig blConfig : blocklists) {
            if (blConfig.isBlocklisted(ip)) {
                if (enableDashboard) KittyDash.addIp(ip);
                KittyStats.addBlockedIP(ip);

                return true;
            }
        }
        return false;
    }
}
