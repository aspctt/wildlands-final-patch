# <p align=center> Wildlands Final Patch </p>

![Version](https://img.shields.io/badge/Available_for-1.21.1-blue)
![Mod Loader](https://img.shields.io/badge/Mod_Loader-NeoForge-orange)
![Part of](https://img.shields.io/badge/Part_of-Wildlands-green)
![License](https://img.shields.io/badge/License-All_Rights_Reserved-red)

## Description

Wildlands Final Patch is the compatibility layer for the Wildlands modpack. It fixes bugs that appear when the pack's mods are run together, and adds the glue between them where a mod stops short of supporting another.

It ships no content. There is nothing to craft, no items, no blocks, and no creative tab. Everything in it is a correction to behaviour that comes from somewhere else, which is why it is loaded last and why it is the first thing to disable when diagnosing a problem.

Every fix is separately switchable. Pulling the JAR to test one suspect fix would take the rest of them out with it, so each one gets a toggle in the config file instead, and the enabled set is written to the log at startup so a report from a player says which fixes were actually running.

Fixes are applied in the lightest way that works. Where an event or an existing API can do the job it is used, and mixins are kept for the cases where nothing else reaches. Anything that patches another mod is guarded by a mod-loaded check, so removing a mod from the pack disables its fixes rather than breaking the game.

## Installation

The JAR ships with the Wildlands modpack. It is not designed to be installed on its own, and it has no purpose outside that pack.

The config is written to `config/wildlands_patch-common.toml` on first launch, and every fix can be switched off there or through the Config button on the Mods screen.

## Requirements

* Minecraft 1.21.1
* NeoForge 21.1.249 or newer

Individual fixes require the mods they patch, and are inert when those mods are absent.

## Repository layout

```
src/main/java/com/aspctt/wildlandspatch/
    WildlandsFinalPatch.java        mod entry point
    WildlandsFinalPatchClient.java  client entry point, client only fixes
    Config.java                     config, and the registry of fix toggles
    Mods.java                       mod ids, and the loaded check every fix is guarded by
    client/sodium/                  options pages rebuilt on Sodium's config API
    data/DataOverrides.java         registers the data override pack
    mixin/                          mixins, one class per target
src/main/resources/
    META-INF/neoforge.mods.toml     mod metadata and dependency declarations
    wildlands_patch.mixins.json     mixin config, every mixin class is listed here
    data_overrides/                 datapack forced above other mods' data, see below
external-files/                     local only, never published, see .gitignore
    bug-reports/                    crash reports and logs a fix is being written against
    context-files/                  notes and references for work in progress
    dev-mods/                       pack JARs the development client loads
    other-mods/                     third-party sources kept for reference
```

## What it currently fixes

**Datapacks with their own `minecraft:empty` loot table.** Loot tables became a registry in 1.21, and Minecraft registers its own `minecraft:empty` into it after the datapacks have loaded, without checking whether one is already there. A datapack carrying `data/minecraft/loot_table/empty.json` is a duplicate key, and the game crashes on the click that opens world creation, pointing at the screen rather than at the file. Packs written for 1.20 and earlier carry that file because back then it simply replaced vanilla's. This keeps the datapack's copy, which is what used to happen.

**Sodium 0.8 options pages.** Sodium 0.6 let a mod add its settings to the video settings screen by mixing into `SodiumGameOptionPages`. Sodium 0.8 deleted that class and replaced it with a config API. Mods that have not been updated still carry the old mixin, which now fails to apply, and the failure is quiet: the settings simply stop appearing, with nothing in the log beyond a mixin target warning. Better Biome Reblend and Cubes Without Borders are both in that state in this pack, which left the biome blend radius and the borderless fullscreen mode with no in game control at all. Both are re-registered here through the supported API, as pages under this mod's own entry in the video settings screen: Sodium allows registering on another mod's behalf, but two registrations under one mod id crash the game at startup, which is what would happen the day one of these mods ships its own Sodium 0.8 integration.

**Serene Seasons crashing single player worlds.** Its precipitation hook, added in 10.1.0.7, intercepts `Biome.hasPrecipitation()` and answers it from `SeasonHooks`, which for any biome not tagged tropical falls back to asking the biome the same question, calling the hook again until the stack runs out. The world loads and then dies a few seconds later with a `StackOverflowError`. The fallback is redirected to read the biome's own precipitation flag directly.

**Broken JSON in other mods.** Create Deco's placard recipe uses `id` where 1.21.1 requires `item` for an ingredient, a syntax that only became valid in 1.21.2, so the recipe never loads and the placard cannot be crafted. Dungeons and Taverns' quest trader advancement names `minecraft:root` as its parent, which is not an advancement in 1.21, so it never loads and the trade it grants never happens. Both are replaced from a datapack shipped in this JAR, described below.

## Adding a fix

1. Declare a toggle in `Config` with `fix(key, comment, default)` and keep the returned key on the class that implements the fix. Write the comment for a player reading the config file: what breaks without it, and which mods are involved.
2. Implement the fix. Check `Config.enabled(key)` inside the handler, not around the registration, so the toggle takes effect on a config reload rather than only at startup.
3. Guard anything mod-specific with a `ModList.get().isLoaded(...)` check.
4. Declare the patched mod in `neoforge.mods.toml` as an `optional` dependency with `ordering="AFTER"`, so this mod loads after the one it corrects.
5. Add an entry to [CHANGE_LOG.md](./CHANGE_LOG.md) naming the mods involved and the symptom, not the implementation. That entry is what makes the fix findable a year later when the mod updates and the fix has to be re-checked.

When the fix is a broken JSON file rather than behaviour, it needs no code at all. Drop a corrected copy into `src/main/resources/data_overrides/`, at the same path it has in the mod it came from, and change only what is broken. That directory is a datapack registered above every other mod's data, so the copy there wins. Everything in it is someone else's file, so each one has to be re-checked when that mod updates: an override keeps applying long after upstream has fixed the file itself.

## Development

Put the pack's JARs in `external-files/dev-mods/`, or point `dev_mods_dir` at the live instance, then:

```
./gradlew installDevMods
```

That mirrors them into `run/client/mods`, which is where the development client loads them from. To run against the pack as it is actually installed, set the source once in `~/.gradle/gradle.properties` rather than in the repository:

```
dev_mods_dir=C:/Users/<you>/AppData/Roaming/PrismLauncher/instances/Wildlands/minecraft/mods
```
 It is a mirror rather than a copy, so a JAR removed from the source folder is removed from the run folder too and an updated mod cannot leave its old version behind. `./gradlew runClient` does this automatically. Run configurations launched from an IDE bypass Gradle, so run the task yourself after changing the folder.

To compile against another mod, add its repository and a `compileOnly` dependency in `build.gradle`. Cursemaven and the Modrinth Maven both serve pack JARs directly and are listed there, commented out.

Useful flags when a mixin is not behaving:

* `-Dmixin.debug.verbose=true` reports what was applied where.
* `-Dmixin.debug.export=true` writes every transformed class to `.mixin.out`, which is how you read what your injection actually compiled to. It is slow with a full pack loaded, so turn it on for one launch rather than leaving it in the run config.

## Licensing

Wildlands Final Patch is **All Rights Reserved**, and is published only as part of the Wildlands modpack. The full terms are in [LICENSE](./LICENSE).

It contains no third-party code and redistributes no third-party files. The mods it patches remain the property of their authors and are governed by their own licences. Please note the copyrights and trademarks in [NOTICE](./NOTICE).

## Credits

* aspctt - design, implementation

Built on [NeoForge](https://neoforged.net/), with mixins through [SpongePowered Mixin](https://github.com/SpongePowered/Mixin).
