package com.friendmesh.ui;

import com.friendmesh.peer.PeerRegistry;

public class HudIndicator {
    private final PeerRegistry peerRegistry;
    
    public HudIndicator(PeerRegistry peerRegistry) {
        this.peerRegistry = peerRegistry;
    }
    
    public void render() {
        // TODO: Render small "FM" indicator on HUD
        // Show green for secure mode, gray for simple mode
        // Display peer count on hover
        
        // This would use Fabric's HUD rendering events
    }
    
    public boolean shouldShow() {
        return peerRegistry.hasPeers();
    }
}
