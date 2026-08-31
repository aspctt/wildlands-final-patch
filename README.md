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
* NeoForge 21.1.235 or newer

Individual fixes require the mods they patch, and are inert when those mods are absent.

## Repository layout

```
src/main/java/com/aspct/wildlandspatch/
    WildlandsFinalPatch.java        mod entry point
    WildlandsFinalPatchClient.java  client entry point, client only fixes
    Config.java                     config, and the registry of fix toggles
    mixin/                          mixins, one class per target
src/main/resources/
    META-INF/neoforge.mods.toml     mod metadata and dependency declarations
    wildlands_patch.mixins.json     mixin config, every mixin class is listed here
external-files/                     local only, never published, see .gitignore
    bug-reports/                    crash reports and logs a fix is being written against
    context-files/                  notes and references for work in progress
    dev-mods/                       pack JARs the development client loads
    other-mods/                     third-party sources kept for reference
```

## Adding a fix

1. Declare a toggle in `Config` with `fix(key, comment, default)` and keep the returned key on the class that implements the fix. Write the comment for a player reading the config file: what breaks without it, and which mods are involved.
2. Implement the fix. Check `Config.enabled(key)` inside the handler, not around the registration, so the toggle takes effect on a config reload rather than only at startup.
3. Guard anything mod-specific with a `ModList.get().isLoaded(...)` check.
4. Declare the patched mod in `neoforge.mods.toml` as an `optional` dependency with `ordering="AFTER"`, so this mod loads after the one it corrects.
5. Add an entry to [CHANGE_LOG.md](./CHANGE_LOG.md) naming the mods involved and the symptom, not the implementation. That entry is what makes the fix findable a year later when the mod updates and the fix has to be re-checked.

## Development

Put the pack's JARs in `external-files/dev-mods/`, then:

```
./gradlew installDevMods
```

That mirrors them into `run/client/mods`, which is where the development client loads them from. It is a mirror rather than a copy, so a JAR removed from the source folder is removed from the run folder too and an updated mod cannot leave its old version behind. `./gradlew runClient` does this automatically. Run configurations launched from an IDE bypass Gradle, so run the task yourself after changing the folder.

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
