# Wildlands Final Patch :: Change Log
- - -

* Unreleased: 1.0.0

	+ **Fixes**
		* Serene Seasons no longer crashes a single player world a few seconds after it loads. Its precipitation hook, added in 10.1.0.7, intercepts `Biome.hasPrecipitation` and then falls back to asking the biome that same question, so for any biome not tagged tropical it calls itself until the stack runs out
		* Re-entry into the hook is answered from the biome's own precipitation flag rather than falling through. NeoForge routes that through the same biome modifiers the hook would have seen, so the answer is unchanged
		* The guard sits at the method's entry and exit rather than on the fallback call it used to redirect. 10.1.0.9 moved that call into a lambda without fixing the recursion, which left the redirect matching nothing, and a required injection that matches nothing takes the game down. Depending on the method existing rather than on the shape of its body survives that kind of refactor
		* Still needed as of 10.1.0.9: the client mixin is still there and every biome that is not tagged tropical still falls through to asking the biome the same question
		* Create Deco's placard recipe loads again. It is written with `id` rather than `item` for an ingredient, which Minecraft only accepts from 1.21.2, so on 1.21.1 the recipe was dropped and the placard could not be crafted
		* Dungeons and Taverns' quest trader advancement loads again. It named `minecraft:root` as its parent, which is not an advancement in 1.21, so it never loaded and the trade it grants never happened
		* Its companion `wander_add_map` is deliberately left broken: the reward function it calls, `nova_structures:choose_wander`, is not in the mod at all, so loading the advancement would trade one error at startup for one on every interaction with a wandering trader
		* World creation no longer dies on a datapack that ships its own `minecraft:empty` loot table. Minecraft registers its own copy after the datapacks have loaded without checking first, so the second one is a duplicate key in a registry, and the game crashes on the click that opens the world creation screen rather than reporting a bad file
		* The datapack's copy is kept, which is what happened before 1.21 turned loot tables into a registry, and is why packs written for older versions carry the file at all
		* Better Biome Reblend's blend radius restored to Sodium's video settings, up to 29x29, with the label its own lang file provides
		* Cubes Without Borders' fullscreen mode restored to Sodium's video settings, as the three way off, fullscreen, or borderless choice the mod adds
		* Both were lost when Sodium 0.8 deleted `SodiumGameOptionPages`, which their own integrations mix into. The mixin silently fails to apply and the settings simply stop appearing, so both are rebuilt on Sodium's config API instead
		* Sodium's own fullscreen toggle left alone rather than replaced, since other options on its General page read it as a boolean
		* Better Biome Reblend's slider formatted by this patch rather than by Sodium, whose own biome blend formatter rejects anything past vanilla's 7 and renders it as an error string
		* Each page built separately, so one of these mods moving a class in an update costs its own page rather than Sodium's whole video settings screen
		* Both pages registered under this mod rather than on each mod's behalf. Sodium allows the latter, but two registrations under one mod id are a startup crash, which is what would happen the day one of these mods ships its own Sodium 0.8 integration alongside this patch

	+ **Balance**
		* Gasoline from Create Diesel Generators takes a liquid blaze burner to super heated rather than heated. Create Crafts & Additions decides the level from a `liquid_burning` recipe, so this is its own recipe with `superheated` set, not a code change
		* Diesel, biodiesel, crude oil and the rest are left at heated, so gasoline is the one fluid worth refining for

	+ **Framework**
		* Balance changes ship as a second datapack, separate from the one correcting broken files, and separately switchable. Disabling a balance choice should not quietly take a crash fix with it, and turning the fixes off to test a bug should not revert the pack's balance underneath you
		* Data overrides: a datapack shipped inside the JAR and forced above every other mod's data, for the fixes that are a single wrong JSON file in someone else's mod
		* Registered at `Pack.Position.TOP` rather than relying on file precedence inside the merged mod data, which is not defined between mods
		* Marked always active, so it cannot be switched off in a world's datapack list where doing so would look like a repair rather than a break. The config toggle is the way off
		* Per-fix config toggles, so one fix can be disabled without taking the rest of them out with it
		* Toggles readable before the config file loads, falling back to their declared defaults, since mixins run against classes loaded long before config loading happens
		* Toggles read inside the handler rather than around the registration, so switching one off takes effect on a config reload instead of only at startup
		* Active fixes written to the log at startup, so a log from a player says which fixes were running
		* Fix toggles exposed through the Config button on the Mods screen

	+ **Project**
		* Serene Seasons and the other patched mods pinned by Modrinth version id where a version number is published for more than one loader
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
