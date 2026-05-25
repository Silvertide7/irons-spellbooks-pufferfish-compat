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
    void distinctSpellsAreAllPresentSortedBySpellId() {
        SkillKey fireSkill = new SkillKey(ResourceLocation.parse("pack:magic"), "fire");
        SkillKey iceSkill = new SkillKey(ResourceLocation.parse("pack:magic"), "ice");
        SkillKey lightningSkill = new SkillKey(ResourceLocation.parse("pack:magic"), "lightning");

        InnateSpellGrant fireGrant = new InnateSpellGrant(ResourceLocation.parse("test:fire"), 1);
        InnateSpellGrant iceGrant = new InnateSpellGrant(ResourceLocation.parse("test:ice"), 1);
        InnateSpellGrant lightningGrant = new InnateSpellGrant(ResourceLocation.parse("test:lightning"), 1);

        Map<SkillKey, InnateSpellGrant> grants = Map.of(
                lightningSkill, lightningGrant,
                fireSkill, fireGrant,
                iceSkill, iceGrant);

        List<InnateSpellGrant> pool = InnatePool.compose(
                new LinkedHashSet<>(List.of(lightningSkill, fireSkill, iceSkill)),
                key -> Optional.ofNullable(grants.get(key)));

        assertEquals(List.of(fireGrant, iceGrant, lightningGrant), pool);
    }

    @Test
    void multipleSkillsGrantingTheSameSpellCollapseToTheHighestLevel() {
        SkillKey firebolt1 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_1");
        SkillKey firebolt2 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_2");
        SkillKey firebolt3 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_3");

        ResourceLocation firebolt = ResourceLocation.parse("irons_spellbooks:firebolt");
        InnateSpellGrant level1 = new InnateSpellGrant(firebolt, 1);
        InnateSpellGrant level2 = new InnateSpellGrant(firebolt, 2);
        InnateSpellGrant level3 = new InnateSpellGrant(firebolt, 3);

        Map<SkillKey, InnateSpellGrant> grants = Map.of(
                firebolt1, level1,
                firebolt2, level2,
                firebolt3, level3);

        List<InnateSpellGrant> pool = InnatePool.compose(
                new LinkedHashSet<>(List.of(firebolt1, firebolt2, firebolt3)),
                key -> Optional.ofNullable(grants.get(key)));

        assertEquals(List.of(level3), pool);
    }

    @Test
    void partialChainGivesTheHighestLevelOwned() {
        SkillKey firebolt1 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_1");
        SkillKey firebolt2 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_2");

        ResourceLocation firebolt = ResourceLocation.parse("irons_spellbooks:firebolt");
        InnateSpellGrant level1 = new InnateSpellGrant(firebolt, 1);
        InnateSpellGrant level2 = new InnateSpellGrant(firebolt, 2);

        Map<SkillKey, InnateSpellGrant> grants = Map.of(
                firebolt1, level1,
                firebolt2, level2);

        List<InnateSpellGrant> ownsOnlyLevel1 = InnatePool.compose(
                new LinkedHashSet<>(List.of(firebolt1)),
                key -> Optional.ofNullable(grants.get(key)));
        assertEquals(List.of(level1), ownsOnlyLevel1);

        List<InnateSpellGrant> ownsBoth = InnatePool.compose(
                new LinkedHashSet<>(List.of(firebolt1, firebolt2)),
                key -> Optional.ofNullable(grants.get(key)));
        assertEquals(List.of(level2), ownsBoth);
    }

    @Test
    void dedupePreservesDistinctSpellsThatAreAlsoUpgraded() {
        SkillKey firebolt1 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_1");
        SkillKey firebolt2 = new SkillKey(ResourceLocation.parse("pack:magic"), "firebolt_2");
        SkillKey frostStep = new SkillKey(ResourceLocation.parse("pack:magic"), "frost_step");

        ResourceLocation firebolt = ResourceLocation.parse("irons_spellbooks:firebolt");
        ResourceLocation frost = ResourceLocation.parse("irons_spellbooks:frost_step");

        InnateSpellGrant fireboltLow = new InnateSpellGrant(firebolt, 1);
        InnateSpellGrant fireboltHigh = new InnateSpellGrant(firebolt, 3);
        InnateSpellGrant frostGrant = new InnateSpellGrant(frost, 1);

        Map<SkillKey, InnateSpellGrant> grants = Map.of(
                firebolt1, fireboltLow,
                firebolt2, fireboltHigh,
                frostStep, frostGrant);

        List<InnateSpellGrant> pool = InnatePool.compose(
                new LinkedHashSet<>(List.of(firebolt1, firebolt2, frostStep)),
                key -> Optional.ofNullable(grants.get(key)));

        assertEquals(List.of(fireboltHigh, frostGrant), pool);
    }

    @Test
    void emptyUnlockedSetProducesEmptyPool() {
        List<InnateSpellGrant> pool = InnatePool.compose(Set.of(), key -> Optional.empty());
        assertTrue(pool.isEmpty());
    }
}
