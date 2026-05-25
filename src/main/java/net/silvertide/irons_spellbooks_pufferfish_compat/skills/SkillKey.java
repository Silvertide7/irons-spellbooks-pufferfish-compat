package net.silvertide.irons_spellbooks_pufferfish_compat.skills;

import net.minecraft.resources.ResourceLocation;

public record SkillKey(ResourceLocation category, String skill) {
    @Override
    public String toString() {
        return category + "/" + skill;
    }
}
