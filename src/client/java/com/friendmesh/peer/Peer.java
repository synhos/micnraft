package com.friendmesh.peer;

import javax.crypto.SecretKey;

public class Peer {
    private final String uuidShort;
    private final String fullUuid;
    private String name;
    private boolean secure;
    private long lastSeen;
    private byte[] publicKey;
    private SecretKey sessionKey;
    private String sessionId;
    
    public Peer(String uuidShort, String fullUuid, String name) {
        this.uuidShort = uuidShort;
        this.fullUuid = fullUuid;
        this.name = name;
        this.secure = false;
        this.lastSeen = System.currentTimeMillis();
    }
    
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - lastSeen > 120000; // 2 minutes
    }
    
    // Getters
    public String getUuidShort() { return uuidShort; }
    public String getFullUuid() { return fullUuid; }
    public String getName() { return name; }
    public boolean isSecure() { return secure; }
    public long getLastSeen() { return lastSeen; }
    public byte[] getPublicKey() { return publicKey; }
    public SecretKey getSessionKey() { return sessionKey; }
    public String getSessionId() { return sessionId; }
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setSecure(boolean secure) { this.secure = secure; }
    public void setPublicKey(byte[] publicKey) { this.publicKey = publicKey; }
    public void setSessionKey(SecretKey sessionKey) { this.sessionKey = sessionKey; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    @Override
    public String toString() {
        return String.format("Peer{name='%s', uuid='%s', secure=%s}", name, uuidShort, secure);
    }
}
