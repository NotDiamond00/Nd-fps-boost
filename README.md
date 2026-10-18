# ND FPS Boost (prototype source)

Target: Minecraft Java 26.3, Fabric, Android launchers (Mojo/Zalith), LTW renderer.

## Important status
This is **source code, not a compiled or tested JAR**. It has not been verified on Minecraft 26.3 or Mojo/LTW. The build environment used to prepare this archive only has Java 21 and no Gradle, and external dependency downloads are unavailable, so producing a valid installable JAR here would be misleading.

## Build
Use a JDK 25 installation and a Gradle installation/wrapper compatible with Fabric Loom 1.17. Run `gradle build`; output should appear in `build/libs/` if the selected Minecraft/Fabric versions and APIs resolve correctly.

## In-game settings for maximum FPS
- Render Distance: 4-6 chunks
- Simulation Distance: 4-5 chunks
- Particles: Minimal
- VSync: Off (if frame pacing/tearing is acceptable)
- Graphics: Fast
- Clouds: Off
- Entity distance: lower if available

The mod attempts to set particles to Minimal and VSync off. It does not promise a fixed FPS increase; results depend on the device, renderer and bottleneck. LTW compatibility remains unverified.
