# <p align=center> Wildlands Final Patch </p>

![Version](https://img.shields.io/badge/Available_for-1.21.1-blue)
![Mod Loader](https://img.shields.io/badge/Mod_Loader-NeoForge-orange)
![Part of](https://img.shields.io/badge/Part_of-Wildlands-green)
![License](https://img.shields.io/badge/License-All_Rights_Reserved-red)

Wildlands Final Patch is the compatibility layer for the Wildlands modpack. It fixes the bugs that only appear once the pack's mods are running together, and adds the glue between them where one mod stops short of supporting another.

It ships no content of its own: no items, no blocks, no creative tab, nothing to craft. Everything in it is a correction to behaviour that comes from somewhere else.

### Fixes are individually switchable

Every fix has its own toggle in `config/wildlands_patch-common.toml`, reachable from the Config button on the Mods screen as well. Disabling one to test a theory leaves the rest in place, and the enabled set is written to the log at startup, so a log from a player is enough to tell what was actually running.

### Light touch

Where an event or a public API can do the job, it is used. Mixins are kept for the cases where nothing else reaches. Anything that patches another mod is guarded by a mod-loaded check, so a mod leaving the pack disables its fixes rather than breaking the game.

### Requirements

Minecraft 1.21.1 and NeoForge 21.1.249 or newer. Individual fixes require the mods they patch, and do nothing when those mods are absent.

This mod is built for Wildlands and ships with it. It is not meant to be installed on its own, and it will not do anything useful in another pack.

### License

Wildlands Final Patch is All Rights Reserved and is published only as part of the Wildlands modpack. Redistribution, re-hosting, and inclusion in other modpacks are not permitted. The full terms are in [LICENSE](LICENSE).

It contains no third-party code and redistributes no third-party files. The mods it patches remain the property of their authors, under their own licences. Trademarks and third-party notices are covered in [NOTICE](NOTICE).
