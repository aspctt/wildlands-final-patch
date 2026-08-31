# Wildlands Final Patch :: Change Log
- - -

* Unreleased: 1.0.0

	+ **Fixes**
		* None yet

	+ **Framework**
		* Per-fix config toggles, so one fix can be disabled without taking the rest of them out with it
		* Toggles readable before the config file loads, falling back to their declared defaults, since mixins run against classes loaded long before config loading happens
		* Toggles read inside the handler rather than around the registration, so switching one off takes effect on a config reload instead of only at startup
		* Active fixes written to the log at startup, so a log from a player says which fixes were running
		* Fix toggles exposed through the Config button on the Mods screen

	+ **Project**
		* NeoForge 21.1.235 on Minecraft 1.21.1, Java 21, Parchment mappings
		* Mixin config registered and ready, with conventions documented in the mixin package
		* Access transformer wired but commented out, for the visibility-only cases where a mixin would be overkill
		* `installDevMods` mirrors the pack JARs from `external-files/dev-mods` into the development client, so fixes are tested against the mods they patch. A mirror rather than a copy, so an updated mod cannot leave a stale duplicate behind
		* `runClient` runs `installDevMods` first
		* Cursemaven and the Modrinth Maven declared but commented out, for compiling against a pack mod
		* Removed the example content, config, and creative tab from the mod template
		* Dropped the gametest run config, which crashes a server when no gametests are registered
		* Dropped the maven-publish block, which has nothing to publish to
		* Licence rewritten as All Rights Reserved, distributed only as part of the Wildlands modpack
