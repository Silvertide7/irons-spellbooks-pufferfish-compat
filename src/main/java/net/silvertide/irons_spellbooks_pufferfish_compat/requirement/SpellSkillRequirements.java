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
import java.util.Map;
import java.util.Optional;

public final class SpellSkillRequirements extends SimpleJsonResourceReloadListener {
    public static final String DATAPACK_DIRECTORY = "spell_skill_requirements";
    public static final SpellSkillRequirements INSTANCE = new SpellSkillRequirements();

    private volatile Map<ResourceLocation, SkillKey> requiredSkillBySpell = Map.of();

    private SpellSkillRequirements() {
        super(new Gson(), DATAPACK_DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> rawEntries, ResourceManager resourceManager, ProfilerFiller profiler) {
        requiredSkillBySpell = parseAll(rawEntries);
        IronsSpellbooksPufferfishCompat.LOGGER.info(
                "Loaded {} spell_skill_requirement entries: {}",
                requiredSkillBySpell.size(), requiredSkillBySpell.keySet());
    }

    public Optional<SkillKey> findForSpell(ResourceLocation spellId) {
        return Optional.ofNullable(requiredSkillBySpell.get(spellId));
    }

    static Map<ResourceLocation, SkillKey> parseAll(Map<ResourceLocation, JsonElement> rawEntries) {
        Map<ResourceLocation, SkillKey> parsed = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : rawEntries.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            SpellSkillRequirement.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> IronsSpellbooksPufferfishCompat.LOGGER.warn(
                            "Skipping malformed spell_skill_requirement {}: {}", fileId, error))
                    .ifPresent(requirement -> {
                        SkillKey previous = parsed.put(requirement.spell(), requirement.requiredSkill());
                        if (previous != null) {
                            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                                    "Duplicate spell_skill_requirement for spell {} - file {} overrode previous entry",
                                    requirement.spell(), fileId);
                        }
                    });
        }
        return Map.copyOf(parsed);
    }
}
