package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnatePoolTest {
    @Test
    void composesGrantsForUnlockedSkillsAndSkipsUngrantedOnes() {
        SkillKey unlockedAndGranted = new SkillKey(
                ResourceLocation.parse("pack:magic"), "fireball");
        SkillKey unlockedWithoutGrant = new SkillKey(
                ResourceLocation.parse("pack:magic"), "lockpicking");

        InnateSpellGrant grant = new InnateSpellGrant(
                ResourceLocation.parse("irons_spellbooks:fireball"), 1);

        Set<SkillKey> unlocked = new LinkedHashSet<>(List.of(unlockedAndGranted, unlockedWithoutGrant));

        List<InnateSpellGrant> pool = InnatePool.compose(
                unlocked,
                key -> Optional.ofNullable(Map.of(unlockedAndGranted, grant).get(key)));

        assertEquals(List.of(grant), pool);
    }

    @Test
    void poolIsSortedByCategoryThenSkillRegardlessOfInputOrder() {
        SkillKey bCategoryFireball = new SkillKey(
                ResourceLocation.parse("pack:b_category"), "fireball");
        SkillKey aCategoryIceball = new SkillKey(
                ResourceLocation.parse("pack:a_category"), "iceball");
        SkillKey bCategoryEarth = new SkillKey(
                ResourceLocation.parse("pack:b_category"), "earth");

        InnateSpellGrant grantA = new InnateSpellGrant(ResourceLocation.parse("test:a"), 1);
        InnateSpellGrant grantB = new InnateSpellGrant(ResourceLocation.parse("test:b"), 1);
        InnateSpellGrant grantC = new InnateSpellGrant(ResourceLocation.parse("test:c"), 1);

        Map<SkillKey, InnateSpellGrant> grants = Map.of(
                bCategoryFireball, grantA,
                aCategoryIceball, grantB,
                bCategoryEarth, grantC);

        Set<SkillKey> unlocked = new LinkedHashSet<>(
                List.of(bCategoryFireball, aCategoryIceball, bCategoryEarth));

        List<InnateSpellGrant> pool = InnatePool.compose(
                unlocked,
                key -> Optional.ofNullable(grants.get(key)));

        assertEquals(List.of(grantB, grantC, grantA), pool);
    }

    @Test
    void emptyUnlockedSetProducesEmptyPool() {
        List<InnateSpellGrant> pool = InnatePool.compose(Set.of(), key -> Optional.empty());
        assertTrue(pool.isEmpty());
    }
}
