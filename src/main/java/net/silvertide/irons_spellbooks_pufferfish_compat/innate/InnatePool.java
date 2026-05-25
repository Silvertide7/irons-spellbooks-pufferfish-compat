package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class InnatePool {
    private static final Comparator<SkillKey> ORDERING =
            Comparator.comparing((SkillKey key) -> key.category().toString())
                    .thenComparing(SkillKey::skill);

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
        return unlockedSkills.stream()
                .sorted(ORDERING)
                .map(grants)
                .flatMap(Optional::stream)
                .toList();
    }
}
