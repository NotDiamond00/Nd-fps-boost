package com.notdiamond.ndfps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * ND FPS Boost for Minecraft Java 26.3 / Fabric.
 *
 * Uses reflection for option access because the 26.x client option API has
 * changed from older Minecraft releases. If a setting name differs, the mod
 * logs it and continues instead of crashing the client.
 */
public final class NDFpsBoostClient implements ClientModInitializer {
    private static final String MOD_ID = "nd_fps_boost";
    private static long lastLogTime;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> applyPerformanceProfile(client));

        // HUD API for current Fabric versions (post-HudRenderCallback).
        HudElementRegistry.addLast(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "performance_hud"),
            NDFpsBoostClient::renderHud
        );
    }

    private static void applyPerformanceProfile(Object client) {
        Object options = readMember(client, "options", "options");
        if (options == null) {
            System.err.println("[ND FPS Boost] Could not locate client options; HUD will still attempt to run.");
            return;
        }

        // Particle count: minimum.
        setEnumOption(options, new String[]{"particles", "particleStatus"}, "MINIMAL");

        // Rendering load: lower chunk distance and entity draw distance.
        setNumericOption(options, new String[]{"renderDistance"}, 6);
        setNumericOption(options, new String[]{"simulationDistance"}, 4);
        setNumericOption(options, new String[]{"entityDistanceScaling"}, 0.5D);

        // Reduce optional visual work where the option exists.
        setEnumOption(options, new String[]{"graphicsMode", "graphics"}, "FAST");
        setEnumOption(options, new String[]{"cloudStatus", "clouds"}, "OFF");
        setBooleanOption(options, new String[]{"enableVsync", "vsync"}, false);

        System.out.println("[ND FPS Boost] Profile applied where supported: particles minimal, render distance 6, simulation distance 4, entity distance 50%, clouds off, VSync off.");
    }

    private static void renderHud(Object graphics, Object deltaTracker) {
        try {
            Object client = findMinecraftInstance();
            if (client == null) return;

            int fps = readInt(client, new String[]{"getFps", "fps", "getFrameRate"}, -1);
            Runtime runtime = Runtime.getRuntime();
            long usedMiB = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long maxMiB = runtime.maxMemory() / (1024L * 1024L);

            String fpsText = fps >= 0 ? "FPS: " + fps : "FPS: --";
            String memoryText = "RAM: " + usedMiB + "/" + maxMiB + " MiB";

            // Text rendering is resolved reflectively to tolerate 26.x GUI API changes.
            drawText(graphics, client, "ND FPS Boost", 6, 6, 0xFF55DDFF);
            drawText(graphics, client, fpsText, 6, 18, 0xFFFFFFFF);
            drawText(graphics, client, memoryText, 6, 30, usedMiB > maxMiB * 0.85 ? 0xFFFF7777 : 0xFFB8FFB8);
        } catch (Throwable ignored) {
            // HUD must never be allowed to crash gameplay.
        }
    }

    private static Object findMinecraftInstance() {
        try {
            Class<?> minecraft = Class.forName("net.minecraft.client.Minecraft");
            for (String name : new String[]{"getInstance", "getMinecraft"}) {
                try { return minecraft.getMethod(name).invoke(null); }
                catch (ReflectiveOperationException ignored) { }
            }
        } catch (Throwable ignored) { }
        return null;
    }

    private static void drawText(Object graphics, Object client, String text, int x, int y, int color) {
        Object font = readMember(client, "font", "font");
        if (font == null) return;

        for (java.lang.reflect.Method method : graphics.getClass().getMethods()) {
            String name = method.getName();
            if (!name.equals("drawString") && !name.equals("drawText")) continue;
            Class<?>[] p = method.getParameterTypes();
            try {
                if (p.length == 5 && p[0].isInstance(font) && p[1] == String.class
                    && p[2] == int.class && p[3] == int.class && p[4] == int.class) {
                    method.invoke(graphics, font, text, x, y, color);
                    return;
                }
                if (p.length == 6 && p[0].isInstance(font) && p[1] == String.class
                    && p[2] == int.class && p[3] == int.class && p[4] == int.class
                    && p[5] == boolean.class) {
                    method.invoke(graphics, font, text, x, y, color, true);
                    return;
                }
            } catch (ReflectiveOperationException ignored) { }
        }
    }

    private static Object readMember(Object object, String fieldName, String methodName) {
        if (object == null) return null;
        try {
            java.lang.reflect.Field field = object.getClass().getField(fieldName);
            return field.get(object);
        } catch (ReflectiveOperationException ignored) { }
        try {
            java.lang.reflect.Method method = object.getClass().getMethod(methodName);
            return method.invoke(object);
        } catch (ReflectiveOperationException ignored) { }
        return null;
    }

    private static Object getOption(Object options, String[] getterNames) {
        for (String name : getterNames) {
            try {
                return options.getClass().getMethod(name).invoke(options);
            } catch (ReflectiveOperationException ignored) { }
            try {
                java.lang.reflect.Field field = options.getClass().getField(name);
                return field.get(options);
            } catch (ReflectiveOperationException ignored) { }
        }
        return null;
    }

    private static boolean setOptionValue(Object option, Object value) {
        if (option == null || value == null) return false;
        for (java.lang.reflect.Method method : option.getClass().getMethods()) {
            if (!(method.getName().equals("set") || method.getName().equals("setValue"))) continue;
            if (method.getParameterCount() != 1) continue;
            try {
                method.invoke(option, value);
                return true;
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) { }
        }
        return false;
    }

    private static void setEnumOption(Object options, String[] names, String enumConstant) {
        Object option = getOption(options, names);
        Object current = option == null ? null : invokeNoArg(option, "get");
        if (!(current instanceof Enum<?> currentEnum)) return;
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object desired = Enum.valueOf((Class) currentEnum.getDeclaringClass(), enumConstant);
            if (!setOptionValue(option, desired)) logMissing(names[0]);
        } catch (IllegalArgumentException ignored) {
            // Enum names can change between game versions; skip safely.
        }
    }

    private static void setNumericOption(Object options, String[] names, Number value) {
        Object option = getOption(options, names);
        Object current = option == null ? null : invokeNoArg(option, "get");
        if (current instanceof Integer) {
            setOptionValue(option, value.intValue());
        } else if (current instanceof Double) {
            setOptionValue(option, value.doubleValue());
        } else if (current instanceof Float) {
            setOptionValue(option, value.floatValue());
        } else {
            logMissing(names[0]);
        }
    }

    private static void setBooleanOption(Object options, String[] names, boolean value) {
        Object option = getOption(options, names);
        if (!setOptionValue(option, value)) logMissing(names[0]);
    }

    private static Object invokeNoArg(Object object, String name) {
        try { return object.getClass().getMethod(name).invoke(object); }
        catch (ReflectiveOperationException ignored) { return null; }
    }

    private static int readInt(Object object, String[] names, int fallback) {
        for (String name : names) {
            try {
                Object result = object.getClass().getMethod(name).invoke(object);
                if (result instanceof Number number) return number.intValue();
            } catch (ReflectiveOperationException ignored) { }
        }
        return fallback;
    }

    private static void logMissing(String setting) {
        long now = System.currentTimeMillis();
        if (now - lastLogTime > 5000) {
            System.out.println("[ND FPS Boost] Setting unavailable on this build; skipped: " + setting);
            lastLogTime = now;
        }
    }
              }
  
