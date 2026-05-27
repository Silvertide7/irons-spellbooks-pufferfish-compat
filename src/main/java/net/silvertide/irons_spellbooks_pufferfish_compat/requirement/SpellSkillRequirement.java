package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.List;

public record SpellSkillRequirement(List<ResourceLocation> spells, List<SkillKey> requiredSkills) {
    public static final Codec<SpellSkillRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("spells").forGetter(SpellSkillRequirement::spells),
            SkillKey.CODEC.listOf().fieldOf("requires").forGetter(SpellSkillRequirement::requiredSkills)
    ).apply(instance, SpellSkillRequirement::new));
}
