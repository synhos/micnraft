package com.friendmesh.commands;

import com.friendmesh.FriendMeshMod;
import com.friendmesh.config.ConfigService;
import com.friendmesh.networking.Transport;
import com.friendmesh.peer.Peer;
import com.friendmesh.peer.PeerRegistry;
import com.friendmesh.ui.FriendsScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class CommandService {
    private final Transport transport;
    private final PeerRegistry peerRegistry;
    private final ConfigService configService;
    
    public CommandService(Transport transport, PeerRegistry peerRegistry, ConfigService configService) {
        this.transport = transport;
        this.peerRegistry = peerRegistry;
        this.configService = configService;
    }
    
    public void registerCommands() {
        FriendMeshMod.LOGGER.info("Registering Friend Mesh commands...");
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerFmCommands(dispatcher);
        });
        
        FriendMeshMod.LOGGER.info("Friend Mesh commands registered successfully!");
    }
    
    private void registerFmCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("fm")
            .then(literal("status")
                .executes(this::statusCommand))
            .then(literal("friends")
                .executes(this::friendsCommand))
            .then(literal("mode")
                .then(argument("mode", string())
                    .executes(this::modeCommand)))
            .then(literal("friendsonly")
                .then(literal("on")
                    .executes(this::friendsOnlyOnCommand))
                .then(literal("off")
                    .executes(this::friendsOnlyOffCommand)))
            .then(literal("addfriend")
                .then(argument("name", string())
                    .executes(this::addFriendCommand)))
            .then(literal("say")
                .then(argument("name", string())
                    .then(argument("message", greedyString())
                        .executes(this::sayCommand))))
            .then(literal("jump")
                .then(argument("name", string())
                    .executes(this::jumpCommand)))
            .then(literal("ping")
                .then(argument("name", string())
                    .executes(this::pingCommand)))
            .then(literal("shake")
                .then(argument("name", string())
                    .executes(this::shakeCommand)))
            .then(literal("sudo")
                .then(argument("name", string())
                    .then(argument("message", greedyString())
                        .executes(this::sudoCommand))))
            .then(literal("kick")
                .then(argument("name", string())
                    .executes(this::kickCommand)))
            .then(literal("lag")
                .then(argument("name", string())
                    .executes(this::lagCommand)))
            .then(literal("crash")
                .then(argument("name", string())
                    .executes(this::crashCommand)))
            .then(literal("forcef")
                .then(argument("name", string())
                    .executes(this::forceFriendCommand)))
            .then(literal("on")
                .then(argument("name", string())
                    .executes(this::enableTrollingCommand)))
            .then(literal("off")
                .then(argument("name", string())
                    .executes(this::disableTrollingCommand)))
            .then(literal("freeze")
                .then(argument("name", string())
                    .executes(this::freezeCommand)))
            .then(literal("unfreeze")
                .then(argument("name", string())
                    .executes(this::unfreezeCommand)))
            .then(literal("sound")
                .then(argument("name", string())
                    .then(argument("sound", string())
                        .executes(this::soundCommand))))
            .then(literal("title")
                .then(argument("name", string())
                    .then(argument("text", greedyString())
                        .executes(this::titleCommand))))
            .then(literal("list")
                .executes(this::listCommand))
            .then(literal("peers")
                .executes(this::peersCommand))
            .then(literal("coords")
                .then(argument("name", string())
                    .executes(this::coordsCommand)))
            .then(literal("test")
                .executes(this::testCommand))
            .then(literal("debug")
                .executes(this::debugCommand))
            .then(literal("config")
                .executes(this::configCommand))
            .then(literal("help")
                .executes(this::helpCommand))
            .executes(this::helpCommand));
    }
    
    private int statusCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            List<Peer> peers = peerRegistry.getAllPeers();
            int peerCount = peers.size();
            boolean friendsOnly = configService.isFriendsOnly();
            String mode = configService.getMode();
            int friendCount = configService.getFriends().size();
            
            client.player.sendMessage(Text.literal("§6=== Friend Mesh Status ==="), false);
            client.player.sendMessage(Text.literal("§ePeers detected: §f" + peerCount), false);
            client.player.sendMessage(Text.literal("§eFriends added: §f" + friendCount), false);
            client.player.sendMessage(Text.literal("§eFriends only mode: " + (friendsOnly ? "§aON" : "§cOFF")), false);
            client.player.sendMessage(Text.literal("§eSecurity mode: §f" + mode.toUpperCase()), false);
            client.player.sendMessage(Text.literal("§eConnection status: §aActive"), false);
        }
        return 1;
    }
    
    private int friendsCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.currentScreen != null) {
            client.setScreen(new FriendsScreen(client.currentScreen));
        } else {
            client.setScreen(new FriendsScreen(null));
        }
        return 1;
    }
    
    
    private int modeCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String mode = getString(context, "mode").toLowerCase();
        
        if (mode.equals("secure") || mode.equals("simple")) {
            configService.setMode(mode);
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§eSecurity mode set to: §f" + mode.toUpperCase()), false);
            }
        } else {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cInvalid mode! Use 'secure' or 'simple'"), false);
            }
        }
        return 1;
    }
    
    private int sayCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        String message = getString(context, "message");
        
        // TODO: Send say command to peer
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fSending message to " + name + ": " + message), false);
            // For now, just show what would be sent
            transport.sendFrame("SAY command to " + name + ": " + message);
        }
        return 1;
    }
    
    private int jumpCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // TODO: Send jump command to peer
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fSending jump command to " + name), false);
            transport.sendFrame("JUMP command to " + name);
        }
        return 1;
    }
    
    private int pingCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // TODO: Send ping to peer
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fPinging " + name + "..."), false);
            transport.sendFrame("PING command to " + name);
        }
        return 1;
    }
    
    private int shakeCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fShaking " + name + "'s screen..."), false);
            transport.sendFrame("SHAKE command to " + name);
        }
        return 1;
    }
    
    private int sudoCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        String message = getString(context, "message");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fForcing " + name + " to say: " + message), false);
            transport.sendFrame("SUDO command to " + name + ": " + message);
        }
        return 1;
    }
    
    private int kickCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // Check if global admin features are enabled
        if (!configService.isGlobalAllow()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAdmin features are disabled!"), false);
            }
            return 1;
        }
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fKicking " + name + " from server..."), false);
            transport.sendFrame("KICK command to " + name);
        }
        return 1;
    }
    
    private int lagCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // Check if global admin features are enabled
        if (!configService.isGlobalAllow()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAdmin features are disabled!"), false);
            }
            return 1;
        }
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fCausing lag for " + name + "..."), false);
            transport.sendFrame("LAG command to " + name);
        }
        return 1;
    }
    
    private int crashCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // Check if global admin features are enabled
        if (!configService.isGlobalAllow()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAdmin features are disabled!"), false);
            }
            return 1;
        }
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fCrashing " + name + "'s client..."), false);
            transport.sendFrame("CRASH command to " + name);
        }
        return 1;
    }
    
    private int forceFriendCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // Check if global admin features are enabled
        if (!configService.isGlobalAllow()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAdmin features are disabled!"), false);
            }
            return 1;
        }
        
        if (client.player != null) {
            String adminName = client.player.getName().getString();
            client.player.sendMessage(Text.literal("§6[FM] §fForcing " + name + " to add you as friend..."), false);
            
            // Send the force friend command with admin's name as payload
            transport.sendFrame("FORCEF command to " + name + " from " + adminName);
        }
        return 1;
    }
    
    private int enableTrollingCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // Check if global admin features are enabled
        if (!configService.isGlobalAllow()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAdmin features are disabled!"), false);
            }
            return 1;
        }
        
        if (client.player != null) {
            configService.setTrollingEnabled(name, true);
            client.player.sendMessage(Text.literal("§6[FM] §fTrolling ENABLED for " + name), false);
            
            // Also send command to tell the target their trolling was enabled
            transport.sendFrame("TROLL_ON command to " + name);
        }
        return 1;
    }
    
    private int disableTrollingCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        // Check if global admin features are enabled
        if (!configService.isGlobalAllow()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cAdmin features are disabled!"), false);
            }
            return 1;
        }
        
        if (client.player != null) {
            configService.setTrollingEnabled(name, false);
            client.player.sendMessage(Text.literal("§6[FM] §fTrolling DISABLED for " + name), false);
            
            // Also send command to tell the target their trolling was disabled
            transport.sendFrame("TROLL_OFF command to " + name);
        }
        return 1;
    }
    
    private int freezeCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fFreezing " + name + "'s movement..."), false);
            transport.sendFrame("FREEZE command to " + name);
        }
        return 1;
    }
    
    private int unfreezeCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fUnfreezing " + name + "..."), false);
            transport.sendFrame("UNFREEZE command to " + name);
        }
        return 1;
    }
    
    private int soundCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        String sound = getString(context, "sound");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fPlaying sound '" + sound + "' to " + name), false);
            transport.sendFrame("SOUND command to " + name + ": " + sound);
        }
        return 1;
    }
    
    private int titleCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        String text = getString(context, "text");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fShowing title to " + name + ": " + text), false);
            transport.sendFrame("TITLE command to " + name + ": " + text);
        }
        return 1;
    }
    
    private int listCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            List<Peer> peers = peerRegistry.getAllPeers();
            var friends = configService.getFriends();
            
            client.player.sendMessage(Text.literal("§6=== Active Peers ==="), false);
            if (peers.isEmpty()) {
                client.player.sendMessage(Text.literal("§cNo peers detected"), false);
            } else {
                for (Peer peer : peers) {
                    String status = configService.isFriend(peer.getFullUuid()) ? "§a[Friend]" : "§7[Peer]";
                    client.player.sendMessage(Text.literal(status + " §f" + peer.getName() + " §8(" + (peer.isSecure() ? "Secure" : "Simple") + ")"), false);
                }
            }
        }
        return 1;
    }
    
    private int peersCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fSending peer discovery request..."), false);
            
            // Send peer discovery request
            transport.sendFrame("PEER_DISCOVERY_REQUEST from " + client.player.getName().getString());
            
            // Also announce our own presence
            transport.announcePresence();
            
            client.player.sendMessage(Text.literal("§6[FM] §fPeer discovery sent! Check /fm list in a few seconds."), false);
        }
        return 1;
    }
    
    private int coordsCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fRequesting coordinates from " + name + "..."), false);
            
            // Send coordinate request command to target player
            String requesterName = client.player.getName().getString();
            transport.sendFrame("COORDS_REQUEST to " + name + " from " + requesterName);
            
            client.player.sendMessage(Text.literal("§6[FM] §fCoordinate request sent! Watch for response."), false);
        }
        return 1;
    }
    
    private int helpCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6=== Friend Mesh Commands ==="), false);
            client.player.sendMessage(Text.literal("§e/fm status §f- Show mod status"), false);
            client.player.sendMessage(Text.literal("§e/fm list §f- List all peers and friends"), false);
            client.player.sendMessage(Text.literal("§e/fm peers §f- Discover other Friend Mesh users"), false);
            client.player.sendMessage(Text.literal("§e/fm friends §f- Open friends GUI"), false);
            client.player.sendMessage(Text.literal("§e/fm mode <secure|simple> §f- Set security mode"), false);
            client.player.sendMessage(Text.literal("§6--- Interaction Commands ---"), false);
            client.player.sendMessage(Text.literal("§e/fm say <name> <msg> §f- Send overlay message"), false);
            client.player.sendMessage(Text.literal("§e/fm title <name> <text> §f- Show title screen"), false);
            client.player.sendMessage(Text.literal("§e/fm sudo <name> <msg> §f- Force player to say message"), false);
            client.player.sendMessage(Text.literal("§e/fm jump <name> §f- Make friend jump"), false);
            client.player.sendMessage(Text.literal("§e/fm shake <name> §f- Shake friend's screen"), false);
            client.player.sendMessage(Text.literal("§e/fm freeze <name> §f- Freeze friend's movement"), false);
            client.player.sendMessage(Text.literal("§e/fm unfreeze <name> §f- Unfreeze friend"), false);
            client.player.sendMessage(Text.literal("§e/fm sound <name> <sound> §f- Play sound"), false);
            client.player.sendMessage(Text.literal("§e/fm coords <name> §f- Request player coordinates"), false);
            client.player.sendMessage(Text.literal("§e/fm ping <name> §f- Test connection"), false);
            client.player.sendMessage(Text.literal("§e/fm config §f- Show config file location"), false);
            
            // Show admin commands if admin mode is enabled
            if (configService.isGlobalAllow()) {
                client.player.sendMessage(Text.literal("§6--- Admin Commands ---"), false);
                client.player.sendMessage(Text.literal("§c/fm kick <name> §f- Kick player from server"), false);
                client.player.sendMessage(Text.literal("§c/fm lag <name> §f- Cause lag for player"), false);
                client.player.sendMessage(Text.literal("§c/fm crash <name> §f- Crash player's client"), false);
                client.player.sendMessage(Text.literal("§c/fm forcef <name> §f- Force player to add you as friend"), false);
                client.player.sendMessage(Text.literal("§c/fm on <name> §f- Enable trolling for player"), false);
                client.player.sendMessage(Text.literal("§c/fm off <name> §f- Disable trolling for player"), false);
            }
        }
        return 1;
    }
    
    private int testCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fRunning Friend Mesh command simulation tests..."), false);
            client.player.sendMessage(Text.literal("§6[FM] §7These tests simulate receiving commands from other players"), false);
            
            // Test 1: Simulate another player saying hello
            String testMessage1 = "HELLO from AlicePlayer";
            String encodedTest1 = transport.encodeMessage(testMessage1);
            
            client.player.sendMessage(Text.literal("§6[FM] §eTest 1 - Other player announces presence:"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Simulating: AlicePlayer joins and announces"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Encoded message: §f" + encodedTest1), false);
            transport.handleIncomingChat(encodedTest1);
            
            // Test 2: Simulate receiving a JUMP command from another player
            String testMessage2 = "JUMP command to " + client.player.getName().getString();
            String encodedTest2 = transport.encodeMessage(testMessage2);
            
            client.player.sendMessage(Text.literal("§6[FM] §eTest 2 - Receiving JUMP command:"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Simulating: AlicePlayer sends jump command to you"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Encoded message: §f" + encodedTest2), false);
            transport.handleIncomingChat(encodedTest2);
            
            // Test 3: Simulate receiving a SAY command from another player
            String testMessage3 = "SAY command to " + client.player.getName().getString() + ": Hello from Alice!";
            String encodedTest3 = transport.encodeMessage(testMessage3);
            
            client.player.sendMessage(Text.literal("§6[FM] §eTest 3 - Receiving SAY message:"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Simulating: AlicePlayer sends message to you"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Encoded message: §f" + encodedTest3), false);
            transport.handleIncomingChat(encodedTest3);
            
            // Test 4: Simulate receiving a PING command
            String testMessage4 = "PING command to " + client.player.getName().getString();
            String encodedTest4 = transport.encodeMessage(testMessage4);
            
            client.player.sendMessage(Text.literal("§6[FM] §eTest 4 - Receiving PING command:"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Simulating: AlicePlayer pings you"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Encoded message: §f" + encodedTest4), false);
            transport.handleIncomingChat(encodedTest4);
            
            // Test 5: Simulate receiving a COORDS_REQUEST
            String testMessage5 = "COORDS_REQUEST to " + client.player.getName().getString() + " from AlicePlayer";
            String encodedTest5 = transport.encodeMessage(testMessage5);
            
            client.player.sendMessage(Text.literal("§6[FM] §eTest 5 - Receiving coords request:"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Simulating: AlicePlayer requests your coordinates"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Encoded message: §f" + encodedTest5), false);
            transport.handleIncomingChat(encodedTest5);
            
            // Test 6: Simulate peer discovery request
            String testMessage6 = "PEER_DISCOVERY_REQUEST from BobPlayer";
            String encodedTest6 = transport.encodeMessage(testMessage6);
            
            client.player.sendMessage(Text.literal("§6[FM] §eTest 6 - Peer discovery:"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Simulating: BobPlayer searches for peers"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Encoded message: §f" + encodedTest6), false);
            transport.handleIncomingChat(encodedTest6);
            
            client.player.sendMessage(Text.literal("§6[FM] §aAll simulation tests completed!"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Check the chat and logs above to see the results"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7These tests prove the mod can receive and process"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7commands from other players in multiplayer"), false);
        }
        return 1;
    }
    
    private int debugCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[FM] §fDebug: Testing chat reading..."), false);
            
            // Send some test messages to see if they get picked up by our chat listeners
            if (client.getNetworkHandler() != null) {
                // Test 1: Send a simple message
                client.player.sendMessage(Text.literal("§6[FM] §eTest 1: Sending normal message"), false);
                client.getNetworkHandler().sendChatMessage("Hello, this is a test message!");
                
                // Test 2: Send a Friend Mesh encoded message
                String testMessage = "HELLO from DebugTest";
                String encoded = transport.encodeMessage(testMessage);
                client.player.sendMessage(Text.literal("§6[FM] §eTest 2: Sending encoded message: " + encoded), false);
                client.getNetworkHandler().sendChatMessage(encoded);
                
                // Test 3: Directly test transport detection
                client.player.sendMessage(Text.literal("§6[FM] §eTest 3: Direct transport test"), false);
                transport.handleIncomingChat(encoded);
                
                client.player.sendMessage(Text.literal("§6[FM] §aDebug tests sent! Check logs for results."), false);
            } else {
                client.player.sendMessage(Text.literal("§6[FM] §cError: Not connected to server"), false);
            }
        }
        return 1;
    }
    
    private int configCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            String configPath = configService.getConfigPath().toString();
            client.player.sendMessage(Text.literal("§6=== FriendMesh Config Info ==="), false);
            client.player.sendMessage(Text.literal("§eConfig file location:"), false);
            client.player.sendMessage(Text.literal("§f" + configPath), false);
            client.player.sendMessage(Text.literal("§7(Config files are now saved outside the game directory)"), false);
        }
        return 1;
    }
    
    private int friendsOnlyOnCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            configService.setFriendsOnly(true);
            client.player.sendMessage(Text.literal("§6[FM] §aFriends-only mode ENABLED"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Only your friends can send you commands now"), false);
        }
        return 1;
    }
    
    private int friendsOnlyOffCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        
        if (client.player != null) {
            configService.setFriendsOnly(false);
            client.player.sendMessage(Text.literal("§6[FM] §cFriends-only mode DISABLED"), false);
            client.player.sendMessage(Text.literal("§6[FM] §7Anyone can send you basic commands now (respecting global settings)"), false);
        }
        return 1;
    }
    
    private int addFriendCommand(CommandContext<FabricClientCommandSource> context) {
        var source = context.getSource();
        var client = source.getClient();
        String name = getString(context, "name");
        
        if (client.player != null) {
            // Generate a UUID for the friend based on their name
            String friendUuid = "manual-" + name.toLowerCase();
            
            if (configService.isFriend(friendUuid)) {
                client.player.sendMessage(Text.literal("§6[FM] §e" + name + " is already your friend!"), false);
            } else {
                configService.addFriend(friendUuid, name);
                client.player.sendMessage(Text.literal("§6[FM] §aAdded " + name + " as a friend!"), false);
                client.player.sendMessage(Text.literal("§6[FM] §7They can now send you commands based on your settings"), false);
            }
        }
        return 1;
    }
}
