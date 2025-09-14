package com.friendmesh.networking;

import com.friendmesh.FriendMeshMod;
import net.minecraft.client.MinecraftClient;

public class FreezeManager {
    private static FreezeManager instance;
    private boolean isFrozen = false;
    private Thread unfreezeTimer;
    
    private FreezeManager() {
        // Private constructor for singleton
    }
    
    public static FreezeManager getInstance() {
        if (instance == null) {
            instance = new FreezeManager();
        }
        return instance;
    }
    
    public void startFreeze() {
        isFrozen = true;
        FriendMeshMod.LOGGER.info("Controls frozen - disabling all player input");
        
        // Cancel any existing unfreeze timer
        if (unfreezeTimer != null && unfreezeTimer.isAlive()) {
            unfreezeTimer.interrupt();
        }
        
        // Start 5-second timer to automatically unfreeze
        unfreezeTimer = new Thread(() -> {
            try {
                Thread.sleep(5000); // 5 seconds
                endFreeze();
                
                // Notify player that freeze ended
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal("§6[FM] §aFreeze effect has expired - controls restored!"), false);
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, freeze probably ended manually
                FriendMeshMod.LOGGER.info("Freeze timer interrupted");
            }
        });
        unfreezeTimer.start();
    }
    
    public void endFreeze() {
        isFrozen = false;
        FriendMeshMod.LOGGER.info("Controls unfrozen - player input restored");
        
        // Cancel the timer if it's still running
        if (unfreezeTimer != null && unfreezeTimer.isAlive()) {
            unfreezeTimer.interrupt();
        }
    }
    
    public boolean isFrozen() {
        return isFrozen;
    }
    
    /**
     * Call this method to check if input should be blocked
     * @return true if input should be blocked, false otherwise
     */
    public boolean shouldBlockInput() {
        return isFrozen;
    }
}
