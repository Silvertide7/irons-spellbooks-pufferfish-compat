package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillBlocks;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillRequirements;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.PuffishSkillsLookup;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

public final class SkillGateEvaluator {
    private SkillGateEvaluator() {}

    public static boolean blockIfMissingRequiredSkill(
            ServerPlayer player,
            ResourceLocation spellId,
            String denialTranslationKey,
            Runnable cancelEvent
    ) {
        Set<SkillKey> requiredSkills = collectRequiredSkills(spellId);
        if (requiredSkills.isEmpty()) return false;

        for (SkillKey requiredSkill : requiredSkills) {
            if (!PuffishSkillsLookup.hasUnlocked(player, requiredSkill)) {
                cancelEvent.run();
                player.displayClientMessage(Component.translatable(denialTranslationKey), true);
                return true;
            }
        }
        return false;
    }

    public static Set<SkillKey> collectRequiredSkills(ResourceLocation spellId) {
        Set<SkillKey> requiredSkills = new HashSet<>();
        SpellSkillRequirements.INSTANCE.findForSpell(spellId).ifPresent(requiredSkills::add);
        requiredSkills.addAll(SpellSkillBlocks.INSTANCE.blockersFor(spellId));
        return requiredSkills;
    }
}
