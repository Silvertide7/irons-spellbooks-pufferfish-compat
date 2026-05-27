package net.silvertide.irons_spellbooks_pufferfish_compat.skills;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record SkillKey(ResourceLocation category, String skill) {
    public static final Codec<SkillKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("category").forGetter(SkillKey::category),
            Codec.STRING.fieldOf("skill").forGetter(SkillKey::skill)
    ).apply(instance, SkillKey::new));

    @Override
    public String toString() {
        return category + "/" + skill;
    }
}
