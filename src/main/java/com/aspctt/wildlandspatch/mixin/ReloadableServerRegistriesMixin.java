package com.aspctt.wildlandspatch.mixin;

import com.aspctt.wildlandspatch.Config;
import com.aspctt.wildlandspatch.WildlandsFinalPatch;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops a datapack that ships its own {@code minecraft:empty} loot table from crashing world
 * creation.
 *
 * <p>Loot tables became a real registry in 1.21, and {@code createUpdatedRegistries} registers
 * vanilla's {@code minecraft:empty} into it unconditionally, after everything the datapacks
 * provided is already in. A datapack carrying {@code data/minecraft/loot_table/empty.json} is
 * therefore a duplicate key, and {@code MappedRegistry.register} throws:
 *
 * <pre>java.lang.IllegalStateException: Adding duplicate key
 * 'ResourceKey[minecraft:loot_table / minecraft:empty]' to registry</pre>
 *
 * <p>The crash lands on the mouse click that opens world creation, which points at the screen
 * rather than at the datapack, and it is fatal rather than a load error naming the file. Before
 * 1.21 the loot tables were a plain map and the datapack's copy simply replaced vanilla's, which is
 * why packs written for older versions carry the file at all and why it was harmless there.
 *
 * <p>Restoring the old behaviour means keeping whichever copy is already registered, which is the
 * datapack's. Every such file seen so far is an empty pool list, exactly what vanilla's own
 * {@code LootTable.EMPTY} is, so nothing about the game changes.
 */
@Mixin(ReloadableServerRegistries.class)
public class ReloadableServerRegistriesMixin {
    @SuppressWarnings({"rawtypes", "unchecked"}) // The call site itself is raw in vanilla.
    @Redirect(
            method = "createUpdatedRegistries",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;"
                            + "Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"))
    private static Holder.Reference wildlands_patch$keepDatapackEmptyLootTable(
            WritableRegistry registry, ResourceKey key, Object value, RegistrationInfo registrationInfo) {
        if (!Config.enabled(Config.DUPLICATE_EMPTY_LOOT_TABLE) || !registry.containsKey(key)) {
            return registry.register(key, value, registrationInfo);
        }

        WildlandsFinalPatch.LOGGER.info(
                "A datapack provides its own {} loot table. Keeping it rather than letting vanilla register a second "
                        + "one, which would crash world creation.", key.location());

        // The caller discards this, but returning the holder that is actually in the registry keeps
        // the redirect honest for anything that reads it later.
        return (Holder.Reference) registry.getHolder(key).orElseThrow();
    }
}
