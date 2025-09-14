package com.friendmesh;

import com.friendmesh.config.ConfigService;
import com.friendmesh.networking.Transport;
import com.friendmesh.peer.PeerRegistry;

import java.lang.reflect.Method;

public class TransportTest {
    
    public static void main(String[] args) throws Exception {
        ConfigService configService = new ConfigService();
        PeerRegistry peerRegistry = new PeerRegistry();
        Transport transport = new Transport(peerRegistry, configService);
        
        // Use reflection to access private methods
        Method encodeMethod = Transport.class.getDeclaredMethod("encodeMessage", String.class);
        encodeMethod.setAccessible(true);
        
        Method decodeMethod = Transport.class.getDeclaredMethod("decodeMessage", String.class);
        decodeMethod.setAccessible(true);
        
        // Test messages
        String[] testMessages = {
            "HELLO from TestPlayer",
            "PING command to target",
            "COORDS_REQUEST from requester",
            "SAY command to player: Hello world!",
            "Special chars: éñ中文🎮"
        };
        
        for (String original : testMessages) {
            System.out.println("Testing: " + original);
            
            // Encode
            String encoded = (String) encodeMethod.invoke(transport, original);
            System.out.println("Encoded: " + encoded);
            
            // Decode
            String decoded = (String) decodeMethod.invoke(transport, encoded);
            System.out.println("Decoded: " + decoded);
            
            // Verify
            if (original.equals(decoded)) {
                System.out.println("✓ Success\n");
            } else {
                System.out.println("✗ FAILED - Expected: '" + original + "', Got: '" + decoded + "'\n");
                throw new RuntimeException("Test failed");
            }
        }
    }
}
