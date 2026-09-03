package com.aspctt.wildlandspatch.data;

import java.util.List;

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
 * Datapacks shipped inside this mod, forced above every other mod's data.
 *
 * <p>Plenty of what breaks in a pack is a JSON file in someone else's mod: a recipe written in the
 * syntax of a later Minecraft version, an advancement pointing at a parent that does not exist. The
 * file is the whole bug and replacing it is the whole fix, but a mod's own {@code data/} directory
 * is merged with every other mod's into one pack, and which copy of a duplicated file wins there is
 * not something to rely on. Registering a separate pack at {@link Pack.Position#TOP} is, since it
 * is applied after the merged mod data and therefore replaces it.
 *
 * <p>There are two, and the split is deliberate. {@code data_overrides/} holds corrections to files
 * that are broken, and {@code data_balance/} holds changes to files that work exactly as their
 * author intended and that this pack wants different anyway. They are separately switchable because
 * they are separate decisions: someone disabling a balance choice should not quietly lose a crash
 * fix with it, and someone turning the fixes off to test a bug should not find the pack's balance
 * reverting underneath them.
 *
 * <p>To change another mod's file, drop a copy into the right directory at the same path it has in
 * the mod it came from, and change only what needs changing. Everything in both directories is
 * someone else's file, so each one has to be re-checked when that mod updates: an override keeps
 * applying long after upstream has moved on.
 *
 * <p>Both packs are marked always active, so neither can be switched off in the datapack list of a
 * world, where turning one off would look like a way to fix something rather than to break it. The
 * config toggles are the way to disable them.
 */
public final class DataOverrides {
    /**
     * A pack directory in the JAR, its config toggle, and the name it shows under. Directories are
     * resolved relative to the JAR root.
     */
    private record BuiltInPack(String directory, String fixKey, String nameKey) {}

    private static final List<BuiltInPack> PACKS = List.of(
            new BuiltInPack("data_overrides", Config.BROKEN_DATA_OVERRIDES, "wildlands_patch.pack.data_overrides"),
            new BuiltInPack("data_balance", Config.BALANCE_TWEAKS, "wildlands_patch.pack.data_balance"));

    private DataOverrides() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DataOverrides::addPackFinders);
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        for (BuiltInPack pack : PACKS) {
            if (!Config.enabled(pack.fixKey())) {
                continue;
            }

            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(WildlandsFinalPatch.MODID, pack.directory()),
                    PackType.SERVER_DATA,
                    Component.translatable(pack.nameKey()),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP);
        }
    }
}
