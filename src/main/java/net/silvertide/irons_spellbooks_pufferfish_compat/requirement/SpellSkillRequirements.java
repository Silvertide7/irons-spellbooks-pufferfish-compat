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

public final class SpellSkillRequirements extends SimpleJsonResourceReloadListener {
    public static final String DATAPACK_DIRECTORY = "spell_skill_requirements";
    public static final SpellSkillRequirements INSTANCE = new SpellSkillRequirements();

    private volatile Map<ResourceLocation, Set<SkillKey>> requiredSkillsBySpell = Map.of();

    private SpellSkillRequirements() {
        super(new Gson(), DATAPACK_DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> rawEntries, ResourceManager resourceManager, ProfilerFiller profiler) {
        requiredSkillsBySpell = parseAll(rawEntries);
    }

    public Set<SkillKey> requiredSkillsFor(ResourceLocation spellId) {
        return requiredSkillsBySpell.getOrDefault(spellId, Set.of());
    }

    static Map<ResourceLocation, Set<SkillKey>> parseAll(Map<ResourceLocation, JsonElement> rawEntries) {
        Map<ResourceLocation, Set<SkillKey>> requiredSkillsBySpell = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : rawEntries.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            SpellSkillRequirement.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> IronsSpellbooksPufferfishCompat.LOGGER.warn(
                            "Skipping malformed spell_skill_requirements file {}: {}", fileId, error))
                    .ifPresent(rule -> {
                        for (ResourceLocation spell : rule.spells()) {
                            requiredSkillsBySpell
                                    .computeIfAbsent(spell, key -> new HashSet<>())
                                    .addAll(rule.requiredSkills());
                        }
                    });
        }
        requiredSkillsBySpell.replaceAll((spellId, skills) -> Set.copyOf(skills));
        return Map.copyOf(requiredSkillsBySpell);
    }
}
