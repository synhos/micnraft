package com.friendmesh.networking;

import com.friendmesh.FriendMeshMod;
import com.friendmesh.config.ConfigService;
import com.friendmesh.peer.PeerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class Transport {
    private final PeerRegistry peerRegistry;
    private final ConfigService configService;
    private final Random random = new Random();
    
    // Friend Mesh message detection
    public static final String FM_PREFIX = "F"; // Ultra-short prefix for maximum compression
    private static final String RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DISGUISE_LENGTH = 0; // No disguise for maximum compression
    
    public Transport(PeerRegistry peerRegistry, ConfigService configService) {
        this.peerRegistry = peerRegistry;
        this.configService = configService;
    }
    
    public void handleIncomingChat(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        FriendMeshMod.LOGGER.debug("[CHAT] Processing message: {}", message);
        
        // Look for encoded Friend Mesh messages
        if (message.startsWith(FM_PREFIX)) {
            try {
                String payload = message.substring(FM_PREFIX.length());
                String decoded = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
                FriendMeshMod.LOGGER.debug("[CHAT] Decoded message: {}", decoded);
                
                // Process the decoded message
                processFrame(decoded);
                
            } catch (Exception e) {
                FriendMeshMod.LOGGER.error("Failed to process Friend Mesh message: {}", e.getMessage(), e);
            }
        }
    }
    
    public void sendFrame(Object frame) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            FriendMeshMod.LOGGER.warn("Cannot send message - not connected to a server");
            return;
        }
        
        try {
            String frameJson = frame.toString();
            String encoded = FM_PREFIX + Base64.getEncoder()
                .encodeToString(frameJson.getBytes(StandardCharsets.UTF_8));
                
            FriendMeshMod.LOGGER.debug("[SEND] Sending message: {}", encoded);
            client.getNetworkHandler().sendChatMessage(encoded);
            
        } catch (Exception e) {
            FriendMeshMod.LOGGER.error("Failed to send message: {}", e.getMessage(), e);
        }
    }
    
    private void processFrame(String frame) {
        // Process the received frame
        // This is where you would handle different types of messages
        // For example, peer discovery, private messages, etc.
        FriendMeshMod.LOGGER.debug("Processing frame: {}", frame);
        
        // Example: Show the message in chat
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§6[FM] §r" + frame), false);
        }
    }
    
    // Add this method to announce presence
    public void announcePresence() {
        sendFrame("HELLO from " + MinecraftClient.getInstance().getSession().getUsername());
    }
    
    // Add this method to encode messages
    public String encodeMessage(String message) {
        try {
            return FM_PREFIX + Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            FriendMeshMod.LOGGER.error("Failed to encode message", e);
            return "";
        }
    }
    
    // Add any additional helper methods for message processing here
}
