package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class InnateSpellGrants extends SimpleJsonResourceReloadListener {
    public static final String DATAPACK_DIRECTORY = "innate_spells";
    public static final InnateSpellGrants INSTANCE = new InnateSpellGrants();

    private record InnateSpellGrantFile(
            ResourceLocation category, String skill, ResourceLocation spell, int level) {
        static final Codec<InnateSpellGrantFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("category").forGetter(InnateSpellGrantFile::category),
                Codec.STRING.fieldOf("skill").forGetter(InnateSpellGrantFile::skill),
                ResourceLocation.CODEC.fieldOf("spell").forGetter(InnateSpellGrantFile::spell),
                Codec.INT.optionalFieldOf("level", InnateSpellGrant.DEFAULT_LEVEL)
                        .forGetter(InnateSpellGrantFile::level)
        ).apply(instance, InnateSpellGrantFile::new));
    }

    private volatile Map<SkillKey, InnateSpellGrant> grantsBySkill = Map.of();

    private InnateSpellGrants() {
        super(new Gson(), DATAPACK_DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> rawEntries, ResourceManager resourceManager, ProfilerFiller profiler) {
        grantsBySkill = parseAll(rawEntries);
    }

    public Optional<InnateSpellGrant> findForSkill(SkillKey skillKey) {
        return Optional.ofNullable(grantsBySkill.get(skillKey));
    }

    public Set<SkillKey> grantingSkills() {
        return grantsBySkill.keySet();
    }

    static Map<SkillKey, InnateSpellGrant> parseAll(Map<ResourceLocation, JsonElement> rawEntries) {
        Map<SkillKey, InnateSpellGrant> parsed = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : rawEntries.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            InnateSpellGrantFile.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> IronsSpellbooksPufferfishCompat.LOGGER.warn(
                            "Skipping malformed innate_spell file {}: {}", fileId, error))
                    .ifPresent(file -> {
                        SkillKey skillKey = new SkillKey(file.category(), file.skill());
                        InnateSpellGrant grant = new InnateSpellGrant(file.spell(), file.level());
                        InnateSpellGrant displaced = parsed.put(skillKey, grant);
                        if (displaced != null) {
                            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                                    "Duplicate innate_spell grant for {} - file {} overrode previous entry",
                                    skillKey, fileId);
                        }
                    });
        }
        return Map.copyOf(parsed);
    }
}
