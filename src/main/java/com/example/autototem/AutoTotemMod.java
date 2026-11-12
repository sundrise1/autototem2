package com.example.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoTotemMod implements ClientModInitializer {

    // 20 tick = 1 saniye -> 1.0 ile 1.5 saniye arası
    private static final int MIN_DELAY = 20;
    private static final int MAX_DELAY = 30;

    private final Random random = new Random();
    private int waitTicks = -1;
    private boolean enabled = true;

    private KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autototem.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.autototem"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            waitTicks = -1;
            if (player != null) {
                player.sendMessage(Text.literal("AutoTotem: " + (enabled ? "AÇIK" : "KAPALI")), true);
            }
        }

        if (!enabled || player == null || client.interactionManager == null) {
            waitTicks = -1;
            return;
        }

        if (client.currentScreen != null) {
            return;
        }

        if (!player.getOffHandStack().isEmpty()) {
            waitTicks = -1;
            return;
        }

        int totemSlot = findTotemSlot(player);
        if (totemSlot == -1) {
            waitTicks = -1;
            return;
        }

        if (waitTicks == -1) {
            waitTicks = MIN_DELAY + random.nextInt(MAX_DELAY - MIN_DELAY + 1);
        }

        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        client.interactionManager.clickSlot(
                player.playerScreenHandler.syncId,
                totemSlot,
                40,
                SlotActionType.SWAP,
                player
        );
        waitTicks = -1;
    }

    private int findTotemSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return 36 + i;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        return -1;
    }
}
