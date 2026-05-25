package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellSkillBlocksTest {
    @Test
    void invertsFileIntoSpellToBlockersMap() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:apprentice_mage/pyromancy"),
                JsonParser.parseString("""
                        {
                          "category": "example_pack:apprentice_mage",
                          "skill": "pyromancy",
                          "blocks": ["irons_spellbooks:fireball", "irons_spellbooks:fire_arrow"]
                        }
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillBlocks.parseAll(raw);

        SkillKey expectedBlocker = new SkillKey(
                ResourceLocation.parse("example_pack:apprentice_mage"), "pyromancy");
        assertEquals(2, parsed.size());
        assertEquals(Set.of(expectedBlocker),
                parsed.get(ResourceLocation.parse("irons_spellbooks:fireball")));
        assertEquals(Set.of(expectedBlocker),
                parsed.get(ResourceLocation.parse("irons_spellbooks:fire_arrow")));
    }

    @Test
    void unionizesBlockersWhenMultipleSkillsBlockTheSameSpell() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:pyro"),
                JsonParser.parseString("""
                        {"category": "x:y", "skill": "pyromancy",
                         "blocks": ["irons_spellbooks:fireball"]}
                        """));
        raw.put(ResourceLocation.parse("example_pack:apprentice"),
                JsonParser.parseString("""
                        {"category": "x:y", "skill": "apprentice",
                         "blocks": ["irons_spellbooks:fireball"]}
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillBlocks.parseAll(raw);

        Set<SkillKey> blockers = parsed.get(ResourceLocation.parse("irons_spellbooks:fireball"));
        assertEquals(2, blockers.size());
        assertTrue(blockers.contains(new SkillKey(ResourceLocation.parse("x:y"), "pyromancy")));
        assertTrue(blockers.contains(new SkillKey(ResourceLocation.parse("x:y"), "apprentice")));
    }

    @Test
    void skipsMalformedEntriesAndKeepsValidOnes() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:good"),
                JsonParser.parseString("""
                        {"category": "x:y", "skill": "good_skill",
                         "blocks": ["irons_spellbooks:fireball"]}
                        """));
        raw.put(ResourceLocation.parse("example_pack:missing_blocks"),
                JsonParser.parseString("""
                        {"category": "x:y", "skill": "bad_skill"}
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillBlocks.parseAll(raw);

        assertEquals(1, parsed.size());
        assertTrue(parsed.containsKey(ResourceLocation.parse("irons_spellbooks:fireball")));
    }
}
