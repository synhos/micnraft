package com.friendmesh.mixin;

import com.friendmesh.ui.FriendsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private static final int BUTTON_SIZE = 20;
    private static final int MARGIN = 4;

    // Target by method name (avoid raw descriptor mismatch)
    @Inject(method = "render", at = @At("TAIL"))
    private void renderFriendMeshButton(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) {
            return;
        }

        // Only show in-game, not when a screen is open
        if (client.currentScreen != null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int x = MARGIN;
        int y = screenHeight - BUTTON_SIZE - MARGIN;

        // Button background & borders
        context.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, 0x80000000);
        context.fill(x, y, x + BUTTON_SIZE, y + 1, 0xFF666666);
        context.fill(x, y, x + 1, y + BUTTON_SIZE, 0xFF666666);
        context.fill(x + BUTTON_SIZE - 1, y, x + BUTTON_SIZE, y + BUTTON_SIZE, 0xFF333333);
        context.fill(x, y + BUTTON_SIZE - 1, x + BUTTON_SIZE, y + BUTTON_SIZE, 0xFF333333);

        // Draw "F" centered
        String text = "F";
        int textWidth = client.textRenderer.getWidth(text);
        int textX = x + (BUTTON_SIZE - textWidth) / 2;
        int textY = y + (BUTTON_SIZE - client.textRenderer.fontHeight) / 2;
        context.drawText(client.textRenderer, text, textX, textY, 0xFFFFFF, true);

        // Simple click handling (works for a HUD button)
        if (client.mouse.wasLeftButtonClicked()) {
            double mouseX = client.mouse.getX() * (double) screenWidth / client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * (double) screenHeight / client.getWindow().getHeight();

            if (mouseX >= x && mouseX <= x + BUTTON_SIZE &&
                mouseY >= y && mouseY <= y + BUTTON_SIZE) {
                client.setScreen(new FriendsScreen()); // <<-- now uses no-arg constructor
            }
        }
    }
}
