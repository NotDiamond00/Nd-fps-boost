package com.notdiamond.ndfps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.particle.ParticleStatus;

/**
 * ND FPS Boost - conservative FPS-oriented settings profile prototype.
 * This is not a rendering engine replacement; performance gains depend on device.
 */
public final class NDFpsBoostClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> applyProfile(client));
    }

    private static void applyProfile(MinecraftClient client) {
        GameOptions options = client.options;
        trySet(options.getParticles(), ParticleStatus.MINIMAL);
        trySet(options.getEnableVsync(), false);
        // Intentionally do not force render/simulation distance via source-level
        // accessors: names and APIs can change across Minecraft versions. Set these
        // two values in Video Settings for a reliable mobile-friendly profile.
        System.out.println("[ND FPS Boost] Loaded. Set Render Distance to 4-6 and Simulation Distance to 4-5 for the FPS profile.");
    }

    private static <T> void trySet(SimpleOption<T> option, T value) {
        if (option == null || value == null) return;
        try {
            option.setValue(value);
        } catch (RuntimeException e) {
            System.err.println("[ND FPS Boost] Could not apply an option: " + e.getMessage());
        }
    }
}
