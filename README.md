# ND FPS Boost — Minecraft Java 26.3 / Fabric

This source update targets Minecraft Java **26.3**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.3**, Java **25**, Loom **1.17**, and Gradle **9.6.0**. Fabric's 26.3 announcement recommends Loom 1.17 / Gradle 9.6 and Loader 0.19.5.

## Included implementation

- Attempts to set particles to Minimal.
- Attempts to set render distance to 6 chunks and simulation distance to 4.
- Attempts to set entity distance scaling to 50%.
- Attempts to use Fast graphics, disable clouds and turn VSync off where those options exist.
- FPS + JVM heap-use HUD.
- Uses reflective option access to skip settings that differ between 26.x mappings instead of crashing.

## Important limitations

- This source archive has **not been compiled or launched against Minecraft 26.3 in this environment**. Build and in-game testing are still required.
- Because Minecraft 26.3 changed APIs, the reflective option setters are intentionally defensive. If a particular option's getter or enum name changed, that setting will be skipped and logged.
- The HUD's text rendering also uses reflection to tolerate GUI API changes; if the current renderer exposes a different method, HUD text may not appear.
- The memory display measures Java heap use; it does not magically optimize or shrink the heap. Do not call forced garbage collection every frame. Set a suitable Java memory limit in the launcher instead.
- Entity distance scaling reduces how far entities are drawn; it does not optimize entity AI or server tick speed.
- FPS gains depend on the phone, renderer (including LTW), drivers, world and mods. No fixed FPS increase is guaranteed.

## Build

Use JDK 25 and Gradle 9.6.0. From the project directory run:

```sh
gradle build
```

The JAR should be in `build/libs/`. If the build fails, share the full Gradle error output and do not install a JAR unless the build succeeds.

## Recommended starting settings for low-end devices

- Render Distance: 4–6
- Simulation Distance: 4
- Particles: Minimal
- Graphics: Fast
- Clouds: Off
- VSync: Off if it gives better frame rate on your device

Back up worlds before testing mods. Client-side only; this mod does not change server gameplay.
