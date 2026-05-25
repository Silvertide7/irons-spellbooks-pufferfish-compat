package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

public record SpellSkillRequirement(ResourceLocation spell, SkillKey requiredSkill) {
    public static final Codec<SpellSkillRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(SpellSkillRequirement::spell),
            ResourceLocation.CODEC.fieldOf("category").forGetter(r -> r.requiredSkill().category()),
            Codec.STRING.fieldOf("skill").forGetter(r -> r.requiredSkill().skill())
    ).apply(instance, (spell, category, skill) ->
            new SpellSkillRequirement(spell, new SkillKey(category, skill))));
}
