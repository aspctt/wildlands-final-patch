package com.aspctt.wildlandspatch.data;

import com.aspctt.wildlandspatch.Config;
import com.aspctt.wildlandspatch.WildlandsFinalPatch;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * A datapack shipped inside this mod, forced above every other mod's data.
 *
 * <p>Plenty of what breaks in a pack is a JSON file in someone else's mod: a recipe written in the
 * syntax of a later Minecraft version, an advancement pointing at a parent that does not exist. The
 * file is the whole bug and replacing it is the whole fix, but a mod's own {@code data/} directory
 * is merged with every other mod's into one pack, and which copy of a duplicated file wins there is
 * not something to rely on. Registering a separate pack at {@link Pack.Position#TOP} is, since it
 * is applied after the merged mod data and therefore replaces it.
 *
 * <p>The pack lives at {@code data_overrides/} in the JAR. To fix another file, drop it in at the
 * same path it has in the mod it comes from and change only what is broken. Every file in there is
 * a copy of someone else's, so each one has to be checked against the mod when that mod updates:
 * an override does not stop applying just because upstream fixed the file itself.
 *
 * <p>The pack is marked always active, so it cannot be switched off in the datapack list of a
 * world, where turning it off would look like a way to fix something rather than to break it. The
 * config toggle is the way to disable it.
 */
public final class DataOverrides {
    /** Directory inside the JAR holding the pack, resolved relative to the JAR root. */
    private static final String PACK_DIRECTORY = "data_overrides";

    private DataOverrides() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DataOverrides::addPackFinders);
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        if (!Config.enabled(Config.BROKEN_DATA_OVERRIDES)) {
            return;
        }

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(WildlandsFinalPatch.MODID, PACK_DIRECTORY),
                PackType.SERVER_DATA,
                Component.translatable("wildlands_patch.pack.data_overrides"),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP);
    }
}
