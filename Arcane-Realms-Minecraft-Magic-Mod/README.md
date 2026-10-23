# ✦ ARCANE REALMS ✦

**A magic-staff mod for Minecraft Java Edition 26.3 (Fabric).** Three ornate, actual 3D item models, 12 different spells, colorful particle effects, a regenerating mana system, recipes, and a compact spell HUD. All spell effects are server-authoritative; works in singleplayer or on a Fabric server with the mod installed by clients as well.

> **Build status:** Source and GitHub Actions workflow supplied. A full Minecraft/Gradle build could not be executed when this repository was generated because the generation environment had no access to Fabric's Maven servers and only Java 21. GitHub Actions downloads Java 25 and the dependencies and provides build results. The 26.2 / 26.1.2 matrix entries are *candidate backports*, not compatibility guarantees.

## The three staffs and twelve spells

| Staff | Spell 1 | Spell 2 | Spell 3 | Spell 4 |
|---|---|---|---|---|
| **Astral** | Starstep: safe short blink | Astral Mend: heals + regen | Cosmic Halo: absorption shield | Starlight Sight: night vision + speed |
| **Ember** | Phoenix Leap: boosted leap + feather landing | Flameward: fire protection | Solar Flare: fiery light ring + nearby entity glow | Blazing Stride: speed + jump |
| **Frost** | Frost Bridge: freezes nearby water into temporary ice | Snow Globe: swirling blizzard + slow falling | Winter Ward: protection + water breathing | Crystal Rush: swift travel + faster mining |

### Controls

- **Right-click** with a staff to cast its selected spell.
- **Sneak + right-click** to switch to the next spell (no mana consumed).
- Your **action bar** displays the selected spell and current mana while the staff is held in your main hand.
- Your current spell is stored in the staff's item data, so selections survive saving a world. Mana is a per-player session pool and starts full when you join.
- Maximum mana: **100**. Regeneration: **1 mana every 4 ticks** (5/s). Cooldowns: approximately 1.5–4.5 seconds depending on spell.
- Holding a staff creates orbiting color-matched particles every half-second. Casts create glowing rings, spirals, bursts, and beams.

### Getting the staffs

In Creative Mode, open **Tools & Utilities**, or type:

```mcfunction
/give @s arcanerealms:astral_staff
/give @s arcanerealms:ember_staff
/give @s arcanerealms:frost_staff
```

In Survival, arrange your crafting table this way (top-to-bottom center column):

| Staff | Top | Middle | Bottom |
|---|---|---|---|
| Astral | Amethyst Shard | Gold Ingot | Stick |
| Ember | Blaze Powder | Copper Ingot | Stick |
| Frost | Blue Ice | Iron Ingot | Stick |

## How to build the mod JAR using GitHub (no PC Java installation required)

1. Create a new **GitHub repository** and upload **the contents of this folder** (not the outer ZIP as a single file). Make sure `.github/workflows/build.yml` is included. Upload with **Add file → Upload files** on github.com, or use Git.
2. Commit your files to the repository's default branch.
3. Open **Actions → Build Arcane Realms JARs**. Allow Actions if GitHub asks.
4. Click the latest workflow run (or **Run workflow**) and open its **Artifacts** section.
5. Download the ZIP named `arcane-realms-minecraft-26.3`. It contains the actual compiled JAR if the build succeeded. Copy the `.jar` into your Minecraft `mods` directory.
6. Install **Fabric Loader** for your exact Minecraft version and the corresponding **Fabric API** mod. Java 25+ is required for Minecraft 26.x.

**Important:** The JAR is uploaded as a build artifact on each run. It is not committed to the repository itself. GitHub will only produce the JAR when Gradle compilation succeeds; check the failed step's error message if it does not.

## Build on your own computer

Prerequisites: **JDK 25** and **Gradle 9.7.1**. From the repository root:

```bash
gradle clean build
```

If Gradle finishes successfully, the mod file is in `build/libs/arcane-realms-1.0.0+mc26.3.jar`.

## Minecraft version compatibility

Minecraft 26.3, 26.2, and 26.1.2 can be tested using the three GitHub Actions jobs. The **26.3 build is the primary supported target**. Each job declares a separate exact `minecraft` version and Fabric API. A JAR for 26.3 will not install on 26.2, 1.21.11, or older versions. The two older 26.x jobs need successful CI testing before claiming support. Minecraft 1.21.x and earlier require actual version-specific ports (Java 21 for 1.21.x), not merely changing the declared version number.

For new versions, adjust `gradle.properties` plus API versions and port affected Java APIs where necessary. **One universal JAR for every historical Minecraft release is not possible.**

## Technical notes

- Fabric entrypoint: `dev.arcanerealms.ArcaneRealms`
- Game assets under `src/main/resources/assets/arcanerealms/`
- One handcrafted Minecraft JSON geometry model per staff; **these are genuine 3D item meshes**, not just flat 2D textures.
- 32×32 textures are included, generated with `generate_assets.py` (requires Pillow *only to regenerate*, not to build mod).
- Item spell selections use `DataComponents.CUSTOM_DATA`.
- No custom network packets or client-only hooks needed for the core spells; native particle packets are sent by the server.
- Java 25, Fabric Loader 0.19.5, Fabric API 0.164.0+26.3, Fabric Loom 1.18-SNAPSHOT, Gradle 9.7.1.
- License: MIT, including project source and original generated textures/models.

## Original-art statement

Models, textures, and code in this starter repository are original; no third-party Minecraft mods were copied. This project is unofficial and is not affiliated with Mojang or Microsoft.
