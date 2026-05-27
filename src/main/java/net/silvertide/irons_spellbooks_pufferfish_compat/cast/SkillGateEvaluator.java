package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillRequirements;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.PuffishSkillsLookup;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.Set;

public final class SkillGateEvaluator {
    private SkillGateEvaluator() {}

    public static void blockIfMissingRequiredSkill(
            ServerPlayer player,
            ResourceLocation spellId,
            String denialTranslationKey,
            Runnable cancelEvent
    ) {
        Set<SkillKey> requiredSkills = collectRequiredSkills(spellId);
        if (requiredSkills.isEmpty()) return;

        for (SkillKey requiredSkill : requiredSkills) {
            if (!PuffishSkillsLookup.hasUnlocked(player, requiredSkill)) {
                cancelEvent.run();
                player.displayClientMessage(Component.translatable(denialTranslationKey), true);
                return;
            }
        }
    }

    public static Set<SkillKey> collectRequiredSkills(ResourceLocation spellId) {
        return SpellSkillRequirements.INSTANCE.requiredSkillsFor(spellId);
    }
}
