package com.friendmesh.mixin;

import com.friendmesh.ui.FriendsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    
    @Shadow @Final private MinecraftClient client;
    
    private static final int BUTTON_SIZE = 20;
    private static final int MARGIN = 4;
    
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        // Only handle left clicks (button 0) when pressed (action 1)
        if (button != 0 || action != 1) {
            return;
        }
        
        // Only handle in-game, not in menus
        if (client.currentScreen != null || client.player == null) {
            return;
        }
        
        // Check if click is on the F button
        if (isClickOnFButton()) {
            client.setScreen(new FriendsScreen(null));
            ci.cancel(); // Prevent other click handling
        }
    }
    
    private boolean isClickOnFButton() {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // F button position (bottom left)
        int buttonX = MARGIN;
        int buttonY = screenHeight - BUTTON_SIZE - MARGIN;
        
        // Get mouse position
        double mouseX = client.mouse.getX() * screenWidth / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * screenHeight / client.getWindow().getHeight();
        
        return mouseX >= buttonX && mouseX < buttonX + BUTTON_SIZE && 
               mouseY >= buttonY && mouseY < buttonY + BUTTON_SIZE;
    }
}
