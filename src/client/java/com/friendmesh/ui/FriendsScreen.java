package com.friendmesh.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public class FriendsScreen extends Screen {
    private final List<String> friends = Arrays.asList("Alice", "Bob", "Charlie");
    private final Screen parent; // screen to return to (may be null)
    private int backX, backY, backW = 70, backH = 20;

    // No-arg constructor (keeps compatibility)
    public FriendsScreen() {
        this(null);
    }

    // Constructor that accepts a parent Screen (used elsewhere in your code)
    public FriendsScreen(Screen parent) {
        super(Text.literal("Friends"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        backX = (this.width - backW) / 2;
        backY = this.height - backH - 10;
    }

    // Note: render signature matches mappings (DrawContext, int mouseX, int mouseY, float delta)
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // call the correct renderBackground overload
        this.renderBackground(context, mouseX, mouseY, delta);

        // Title
        String titleStr = this.title.getString();
        int titleWidth = this.textRenderer.getWidth(titleStr);
        int centerX = this.width / 2;
        int titleY = 20;
        context.drawText(this.textRenderer, titleStr, centerX - titleWidth / 2, titleY, 0xFFFFFF, false);

        // Friend list
        int startY = titleY + 20;
        int lineHeight = this.textRenderer.fontHeight + 6;
        int left = 20;
        for (int i = 0; i < friends.size(); i++) {
            String name = friends.get(i);
            int y = startY + i * lineHeight;
            context.drawText(this.textRenderer, name, left, y, 0xDDDDDD, false);
        }

        // Draw Back button
        context.fill(backX, backY, backX + backW, backY + backH, 0x80000000);
        context.fill(backX, backY, backX + backW, backY + 1, 0xFF666666);
        context.fill(backX, backY, backX + 1, backY + backH, 0xFF666666);
        context.fill(backX + backW - 1, backY, backX + backW, backY + backH, 0xFF333333);
        context.fill(backX, backY + backH - 1, backX + backW, backY + backH, 0xFF333333);

        String backText = "Back";
        int btW = this.textRenderer.getWidth(backText);
        int btX = backX + (backW - btW) / 2;
        int btY = backY + (backH - this.textRenderer.fontHeight) / 2;
        context.drawText(this.textRenderer, backText, btX, btY, 0xFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }

    // Signature matches mappings: double mouse coords or keep existing mapping style - compiler used doubles earlier for mouse clicks?
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= backX && mouseX <= backX + backW &&
                mouseY >= backY && mouseY <= backY + backH) {
                // return to parent screen if present, otherwise close
                MinecraftClient.getInstance().setScreen(this.parent);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
