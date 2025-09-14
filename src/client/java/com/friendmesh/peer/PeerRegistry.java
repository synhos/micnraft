package com.friendmesh.peer;

import com.friendmesh.FriendMeshMod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerRegistry {
    private final Map<String, Peer> peers = new ConcurrentHashMap<>();
    private final Timer cleanupTimer;
    
    public PeerRegistry() {
        // Start a cleanup timer to remove expired peers
        this.cleanupTimer = new Timer("FriendMesh-PeerCleanup", true);
        this.cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredPeers();
            }
        }, 30000, 30000); // Every 30 seconds
    }
    
    public void addPeer(String uuidShort, String fullUuid, String name) {
        if (!peers.containsKey(uuidShort)) {
            Peer peer = new Peer(uuidShort, fullUuid, name);
            peers.put(uuidShort, peer);
            FriendMeshMod.LOGGER.info("Added peer: {} ({})", name, uuidShort);
        } else {
            // Update existing peer
            Peer existing = peers.get(uuidShort);
            existing.setName(name);
            existing.updateLastSeen();
        }
    }
    
    public void removePeer(String uuidShort) {
        Peer removed = peers.remove(uuidShort);
        if (removed != null) {
            FriendMeshMod.LOGGER.info("Removed peer: {} ({})", removed.getName(), uuidShort);
        }
    }
    
    public Peer getPeer(String uuidShort) {
        return peers.get(uuidShort);
    }
    
    public List<Peer> getAllPeers() {
        return new ArrayList<>(peers.values());
    }
    
    public int getPeerCount() {
        return peers.size();
    }
    
    public boolean hasPeers() {
        return !peers.isEmpty();
    }
    
    public void updatePeerLastSeen(String uuidShort) {
        Peer peer = peers.get(uuidShort);
        if (peer != null) {
            peer.updateLastSeen();
        }
    }
    
    private void cleanupExpiredPeers() {
        List<String> expiredPeers = new ArrayList<>();
        
        for (Map.Entry<String, Peer> entry : peers.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredPeers.add(entry.getKey());
            }
        }
        
        for (String expiredPeer : expiredPeers) {
            removePeer(expiredPeer);
        }
        
        if (!expiredPeers.isEmpty()) {
            FriendMeshMod.LOGGER.debug("Cleaned up {} expired peers", expiredPeers.size());
        }
    }
    
    public void clear() {
        peers.clear();
        FriendMeshMod.LOGGER.debug("Cleared all peers");
    }
    
    public void shutdown() {
        if (cleanupTimer != null) {
            cleanupTimer.cancel();
        }
        clear();
    }
}
