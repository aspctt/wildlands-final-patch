# Wildlands Final Patch :: Change Log
- - -

* Unreleased: 1.0.0

	+ **Fixes**
		* Better Biome Reblend's blend radius restored to Sodium's video settings, up to 29x29, with the label its own lang file provides
		* Cubes Without Borders' fullscreen mode restored to Sodium's video settings, as the three way off, fullscreen, or borderless choice the mod adds
		* Both were lost when Sodium 0.8 deleted `SodiumGameOptionPages`, which their own integrations mix into. The mixin silently fails to apply and the settings simply stop appearing, so both are rebuilt on Sodium's config API instead
		* Sodium's own fullscreen toggle left alone rather than replaced, since other options on its General page read it as a boolean
		* Better Biome Reblend's slider formatted by this patch rather than by Sodium, whose own biome blend formatter rejects anything past vanilla's 7 and renders it as an error string
		* Each page built separately, so one of these mods moving a class in an update costs its own page rather than Sodium's whole video settings screen
		* Both pages registered under this mod rather than on each mod's behalf. Sodium allows the latter, but two registrations under one mod id are a startup crash, which is what would happen the day one of these mods ships its own Sodium 0.8 integration alongside this patch

	+ **Framework**
		* Per-fix config toggles, so one fix can be disabled without taking the rest of them out with it
		* Toggles readable before the config file loads, falling back to their declared defaults, since mixins run against classes loaded long before config loading happens
		* Toggles read inside the handler rather than around the registration, so switching one off takes effect on a config reload instead of only at startup
		* Active fixes written to the log at startup, so a log from a player says which fixes were running
		* Fix toggles exposed through the Config button on the Mods screen

	+ **Project**
		* NeoForge 21.1.249 on Minecraft 1.21.1, matching the version the pack ships, Java 21, Parchment mappings
		* Compiles against Sodium's separately published API artifact, so the config API is available without unpacking Sodium's nested JAR
		* Better Biome Reblend pinned by Modrinth version id, since two different files are published as 1.5.2 and only one carries the neoforge class tree
		* Mixin config registered and ready, with conventions documented in the mixin package
		* Access transformer wired but commented out, for the visibility-only cases where a mixin would be overkill
		* `installDevMods` mirrors the pack JARs into the development client, so fixes are tested against the mods they patch. A mirror rather than a copy, so an updated mod cannot leave a stale duplicate behind
		* `dev_mods_dir` points that at the installed Prism instance, so a dev run loads the pack as the player has it rather than a copy that has drifted
		* `runClient` runs `installDevMods` first
		* Cursemaven and the Modrinth Maven declared but commented out, for compiling against a pack mod
		* Built as `WildlandsPatch-<version>+<minecraft version>.jar`, with the Minecraft version in the mod version too, so a JAR on its own says what it was built for
		* Removed the example content, config, and creative tab from the mod template
		* Dropped the gametest run config, which crashes a server when no gametests are registered
		* Dropped the maven-publish block, which has nothing to publish to
		* Licence rewritten as All Rights Reserved, distributed only as part of the Wildlands modpack
