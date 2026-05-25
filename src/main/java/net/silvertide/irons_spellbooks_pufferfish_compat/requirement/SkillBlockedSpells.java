package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.List;

public record SkillBlockedSpells(SkillKey blockingSkill, List<ResourceLocation> blockedSpells) {
    public static final Codec<SkillBlockedSpells> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("category").forGetter(s -> s.blockingSkill().category()),
            Codec.STRING.fieldOf("skill").forGetter(s -> s.blockingSkill().skill()),
            ResourceLocation.CODEC.listOf().fieldOf("blocks").forGetter(SkillBlockedSpells::blockedSpells)
    ).apply(instance, (category, skill, blocks) -> new SkillBlockedSpells(new SkillKey(category, skill), blocks)));
}
