package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.PuffishSkillsLookup;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class InnatePool {
    private InnatePool() {}

    public static List<InnateSpellGrant> currentPool(ServerPlayer player) {
        return compose(
                InnateSpellGrants.INSTANCE.grantingSkills(),
                skill -> PuffishSkillsLookup.hasUnlocked(player, skill)
                        ? InnateSpellGrants.INSTANCE.findForSkill(skill)
                        : Optional.empty());
    }

    static List<InnateSpellGrant> compose(
            Set<SkillKey> candidateSkills,
            Function<SkillKey, Optional<InnateSpellGrant>> grantForUnlockedSkill
    ) {
        Map<ResourceLocation, InnateSpellGrant> highestGrantBySpell = new HashMap<>();
        for (SkillKey candidateSkill : candidateSkills) {
            grantForUnlockedSkill.apply(candidateSkill).ifPresent(grant ->
                    highestGrantBySpell.merge(grant.spell(), grant, InnatePool::keepHigherLevel));
        }
        return highestGrantBySpell.values().stream()
                .sorted(Comparator.comparing(grant -> grant.spell().toString()))
                .toList();
    }

    private static InnateSpellGrant keepHigherLevel(InnateSpellGrant existing, InnateSpellGrant incoming) {
        return incoming.level() > existing.level() ? incoming : existing;
    }
}
