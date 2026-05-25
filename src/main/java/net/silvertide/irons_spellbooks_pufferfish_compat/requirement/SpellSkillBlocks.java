package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SpellSkillBlocks extends SimpleJsonResourceReloadListener {
    public static final String DATAPACK_DIRECTORY = "skill_blocks";
    public static final SpellSkillBlocks INSTANCE = new SpellSkillBlocks();

    private volatile Map<ResourceLocation, Set<SkillKey>> blockersBySpell = Map.of();

    private SpellSkillBlocks() {
        super(new Gson(), DATAPACK_DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> rawEntries, ResourceManager resourceManager, ProfilerFiller profiler) {
        blockersBySpell = parseAll(rawEntries);
        IronsSpellbooksPufferfishCompat.LOGGER.info(
                "Loaded {} skill_blocks entries spanning {} blocked spells: {}",
                rawEntries.size(), blockersBySpell.size(), blockersBySpell.keySet());
    }

    public Set<SkillKey> blockersFor(ResourceLocation spellId) {
        return blockersBySpell.getOrDefault(spellId, Set.of());
    }

    static Map<ResourceLocation, Set<SkillKey>> parseAll(Map<ResourceLocation, JsonElement> rawEntries) {
        Map<ResourceLocation, Set<SkillKey>> mutableBlockers = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : rawEntries.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            SkillBlockedSpells.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> IronsSpellbooksPufferfishCompat.LOGGER.warn(
                            "Skipping malformed skill_blocks file {}: {}", fileId, error))
                    .ifPresent(parsed -> {
                        for (ResourceLocation blockedSpell : parsed.blockedSpells()) {
                            mutableBlockers
                                    .computeIfAbsent(blockedSpell, key -> new HashSet<>())
                                    .add(parsed.blockingSkill());
                        }
                    });
        }
        Map<ResourceLocation, Set<SkillKey>> immutable = new HashMap<>();
        mutableBlockers.forEach((spellId, blockers) -> immutable.put(spellId, Set.copyOf(blockers)));
        return Map.copyOf(immutable);
    }
}
