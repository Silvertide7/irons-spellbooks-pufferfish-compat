package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class InnatePool {
    private InnatePool() {}

    public static List<InnateSpellGrant> currentPool(ServerPlayer player) {
        Set<SkillKey> unlockedSkills = new HashSet<>();
        SkillsAPI.streamCategories().forEach(category -> {
            var categoryId = category.getId();
            category.streamUnlockedSkills(player)
                    .forEach(skill -> unlockedSkills.add(new SkillKey(categoryId, skill.getId())));
        });
        return compose(unlockedSkills, InnateSpellGrants.INSTANCE::findForSkill);
    }

    static List<InnateSpellGrant> compose(
            Set<SkillKey> unlockedSkills,
            Function<SkillKey, Optional<InnateSpellGrant>> grants
    ) {
        Map<ResourceLocation, InnateSpellGrant> highestGrantBySpell = new HashMap<>();
        for (SkillKey unlockedSkill : unlockedSkills) {
            grants.apply(unlockedSkill).ifPresent(grant ->
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
