package com.friendmesh.config;

import com.friendmesh.FriendMeshMod;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigService {
    private static final String CONFIG_FILE = "friendmesh.json";
    private final Path configPath;
    private FriendMeshConfig config;
    private final Gson gson;
    
    public ConfigService() {
        this.configPath = getCustomConfigPath();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }
    
    private Path getCustomConfigPath() {
        // Use user home directory instead of Minecraft config directory
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, ".friendmesh");
        
        // Create the config directory if it doesn't exist
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                FriendMeshMod.LOGGER.info("Created FriendMesh config directory at: {}", configDir);
            }
        } catch (Exception e) {
            FriendMeshMod.LOGGER.error("Failed to create config directory, falling back to default", e);
            // Fall back to the default Fabric config directory if we can't create our custom one
            return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        }
        
        return configDir.resolve(CONFIG_FILE);
    }
    
    private void load() {
        if (!Files.exists(configPath)) {
            config = createDefaultConfig();
            save();
            FriendMeshMod.LOGGER.info("Created default FriendMesh config at: {}", configPath);
        } else {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                config = gson.fromJson(reader, FriendMeshConfig.class);
                if (config == null) {
                    config = createDefaultConfig();
                }
                FriendMeshMod.LOGGER.info("Loaded FriendMesh config from: {}", configPath);
            } catch (Exception e) {
                FriendMeshMod.LOGGER.error("Failed to load config from {}, using defaults", configPath, e);
                config = createDefaultConfig();
            }
        }
    }
    
    public void save() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            gson.toJson(config, writer);
            FriendMeshMod.LOGGER.debug("Saved FriendMesh config");
        } catch (Exception e) {
            FriendMeshMod.LOGGER.error("Failed to save config", e);
        }
    }
    
    private FriendMeshConfig createDefaultConfig() {
        FriendMeshConfig defaultConfig = new FriendMeshConfig();
        defaultConfig.version = 1;
        defaultConfig.globalAllow = true; // Enable by default
        defaultConfig.allowFriendTrolling = true; // Friends can always troll friends
        defaultConfig.allowEveryoneTrolling = true; // Allow trolling everyone by default
        defaultConfig.mode = "secure";
        defaultConfig.rate = new RateConfig();
        defaultConfig.rate.outMs = 500;
        defaultConfig.rate.jumpMinMs = 2000;
        defaultConfig.rate.sayMinMs = 3000;
        defaultConfig.friends = new ArrayList<>();
        defaultConfig.trollingSettings = new HashMap<>(); // Individual player trolling settings
        return defaultConfig;
    }
    
    // Getters
    public Path getConfigPath() { return configPath; }
    public boolean isGlobalAllow() { return config.globalAllow; }
    public boolean isFriendsOnly() { return config.friendsOnly; }
    public boolean allowFriendTrolling() { return config.allowFriendTrolling; }
    public boolean allowEveryoneTrolling() { return config.allowEveryoneTrolling; }
    public String getMode() { return config.mode; }
    public RateConfig getRate() { return config.rate; }
    public List<Friend> getFriends() { return config.friends; }
    
    // Setters
    public void setGlobalAllow(boolean globalAllow) { 
        config.globalAllow = globalAllow; 
        save();
    }
    public void setFriendsOnly(boolean friendsOnly) { 
        config.friendsOnly = friendsOnly; 
        save();
    }
    public void setMode(String mode) { 
        config.mode = mode; 
        save();
    }
    public void setAllowFriendTrolling(boolean allowFriendTrolling) {
        config.allowFriendTrolling = allowFriendTrolling;
        save();
    }
    public void setAllowEveryoneTrolling(boolean allowEveryoneTrolling) {
        config.allowEveryoneTrolling = allowEveryoneTrolling;
        save();
    }
    
    // Friend management
    public boolean isFriend(String uuid) {
        return config.friends.stream().anyMatch(f -> f.uuid.equals(uuid));
    }
    
    public Friend getFriend(String uuid) {
        return config.friends.stream()
            .filter(f -> f.uuid.equals(uuid))
            .findFirst()
            .orElse(null);
    }
    
    public void addFriend(String uuid, String name) {
        if (!isFriend(uuid)) {
            Friend friend = new Friend();
            friend.uuid = uuid;
            friend.name = name;
            friend.actions = new HashMap<>();
            friend.actions.put("JUMP", true);
            friend.actions.put("SAY", true);
            friend.actions.put("PING", true);
            config.friends.add(friend);
            save();
        }
    }
    
    public void removeFriend(String uuid) {
        config.friends.removeIf(f -> f.uuid.equals(uuid));
        save();
    }
    
    public boolean isActionAllowed(String uuid, String action) {
        Friend friend = getFriend(uuid);
        return friend != null && friend.actions.getOrDefault(action, false);
    }
    
    public void setActionAllowed(String uuid, String action, boolean allowed) {
        Friend friend = getFriend(uuid);
        if (friend != null) {
            friend.actions.put(action, allowed);
            save();
        }
    }
    
    // Trolling management
    public boolean isTrollingEnabled(String playerName) {
        if (config.trollingSettings == null) {
            config.trollingSettings = new HashMap<>();
        }
        // Default to enabled (true) if no specific setting exists
        return config.trollingSettings.getOrDefault(playerName.toLowerCase(), true);
    }
    
    public void setTrollingEnabled(String playerName, boolean enabled) {
        if (config.trollingSettings == null) {
            config.trollingSettings = new HashMap<>();
        }
        config.trollingSettings.put(playerName.toLowerCase(), enabled);
        save();
    }
    
    public Map<String, Boolean> getTrollingSettings() {
        if (config.trollingSettings == null) {
            config.trollingSettings = new HashMap<>();
        }
        return config.trollingSettings;
    }
    
    // Inner classes for config structure
    public static class FriendMeshConfig {
        public int version;
        public boolean globalAllow;
        public boolean friendsOnly; // Legacy field
        public boolean allowFriendTrolling; // Friends can troll friends (admin-only setting)
        public boolean allowEveryoneTrolling; // Allow trolling everyone (user setting)
        public String mode;
        public RateConfig rate;
        public List<Friend> friends;
        public Map<String, Boolean> trollingSettings; // Individual player trolling on/off
    }
    
    public static class RateConfig {
        public long outMs;
        public long jumpMinMs;
        public long sayMinMs;
    }
    
    public static class Friend {
        public String uuid;
        public String name;
        public Map<String, Boolean> actions;
    }
}
