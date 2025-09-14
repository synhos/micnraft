package com.friendmesh;

import com.friendmesh.config.ConfigService;
import com.friendmesh.networking.Transport;
import com.friendmesh.peer.PeerRegistry;
import com.friendmesh.commands.CommandService;
import com.friendmesh.ui.HudIndicator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendMeshMod implements ClientModInitializer {
    public static final String MOD_ID = "friendmesh";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Core services
    private static ConfigService configService;
    private static PeerRegistry peerRegistry;
    private static Transport transport;
    private static CommandService commandService;
    private static HudIndicator hudIndicator;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Friend Mesh initializing...");
        
        // Initialize core services
        configService = new ConfigService();
        peerRegistry = new PeerRegistry();
        transport = new Transport(peerRegistry, configService);
        commandService = new CommandService(transport, peerRegistry, configService);
        hudIndicator = new HudIndicator(peerRegistry);
        
        // Register event handlers
        registerEventHandlers();
        
        // Register commands
        commandService.registerCommands();
        
        LOGGER.info("Friend Mesh initialized successfully!");
    }
    
    private void registerEventHandlers() {
        // Handle chat messages
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            handleIncomingMessage(message);
            return true; // Allow the message to be processed by other mods
        });
        
        // Handle system/game messages
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            handleIncomingMessage(message);
            return true; // Allow the message to be processed by other mods
        });
        
        // Handle client startup/shutdown
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            LOGGER.debug("Client started");
        });
        
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.debug("Client stopping");
            if (configService != null) {
                configService.save();
            }
        });
        
        // Handle server join/leave
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            peerRegistry.clear();
            LOGGER.info("Joined world, cleared peer list");
        });
        
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            peerRegistry.clear();
            LOGGER.info("Left world, cleared peer list");
        });
    }
    
    private void handleIncomingMessage(Text message) {
        String raw = message.getString();
        LOGGER.debug("[CHAT] Received message: {}", raw);
        
        // Process for Friend Mesh if it starts with our prefix
        if (raw.startsWith(Transport.FM_PREFIX)) {
            LOGGER.debug("Processing Friend Mesh message");
            transport.handleIncomingChat(raw);
        }
    }
    
    // Static getters for services
    public static ConfigService getConfigService() {
        return configService;
    }
    
    public static PeerRegistry getPeerRegistry() {
        return peerRegistry;
    }
    
    public static Transport getTransport() {
        return transport;
    }
    
    public static HudIndicator getHudIndicator() {
        return hudIndicator;
    }
    
    public static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
}
