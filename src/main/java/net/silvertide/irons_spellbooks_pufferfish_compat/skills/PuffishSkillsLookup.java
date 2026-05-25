package net.silvertide.irons_spellbooks_pufferfish_compat.skills;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

import java.util.Optional;

public final class PuffishSkillsLookup {
    private PuffishSkillsLookup() {}

    public static boolean hasUnlocked(ServerPlayer player, SkillKey skillKey) {
        Optional<Category> category = SkillsAPI.getCategory(skillKey.category());
        if (category.isEmpty()) {
            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                    "Skill mapping references unknown Pufferfish category '{}'", skillKey.category());
            return false;
        }
        Optional<Skill> skill = category.get().getSkill(skillKey.skill());
        if (skill.isEmpty()) {
            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                    "Skill mapping references unknown Pufferfish skill '{}' in category '{}'",
                    skillKey.skill(), skillKey.category());
            return false;
        }
        return skill.get().getState(player) == Skill.State.UNLOCKED;
    }
}
