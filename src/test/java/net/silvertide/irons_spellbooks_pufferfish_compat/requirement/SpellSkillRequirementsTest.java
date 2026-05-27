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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellSkillRequirementsTest {
    private static final SkillKey PYROMANCY =
            new SkillKey(ResourceLocation.parse("example_pack:magic"), "pyromancy");
    private static final SkillKey FIRE_MASTERY =
            new SkillKey(ResourceLocation.parse("example_pack:magic"), "fire_mastery");

    @Test
    void fansOneRuleOutToEverySpellItLists() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:anywhere"),
                JsonParser.parseString("""
                        {
                          "spells": ["irons_spellbooks:fire_arrow", "irons_spellbooks:fire_breath"],
                          "requires": [{"category": "example_pack:magic", "skill": "pyromancy"}]
                        }
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillRequirements.parseAll(raw);

        assertEquals(Set.of(PYROMANCY), parsed.get(ResourceLocation.parse("irons_spellbooks:fire_arrow")));
        assertEquals(Set.of(PYROMANCY), parsed.get(ResourceLocation.parse("irons_spellbooks:fire_breath")));
        assertFalse(parsed.containsKey(ResourceLocation.parse("example_pack:anywhere")));
    }

    @Test
    void unionsRequiredSkillsForASpellAcrossMultipleRules() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:rule_a"),
                JsonParser.parseString("""
                        {"spells": ["irons_spellbooks:fireball"],
                         "requires": [{"category": "example_pack:magic", "skill": "pyromancy"}]}
                        """));
        raw.put(ResourceLocation.parse("example_pack:rule_b"),
                JsonParser.parseString("""
                        {"spells": ["irons_spellbooks:fireball"],
                         "requires": [{"category": "example_pack:magic", "skill": "fire_mastery"}]}
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillRequirements.parseAll(raw);

        assertEquals(Set.of(PYROMANCY, FIRE_MASTERY),
                parsed.get(ResourceLocation.parse("irons_spellbooks:fireball")));
    }

    @Test
    void multipleRequiredSkillsInOneRuleAllApply() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:multi"),
                JsonParser.parseString("""
                        {
                          "spells": ["irons_spellbooks:fireball"],
                          "requires": [
                            {"category": "example_pack:magic", "skill": "pyromancy"},
                            {"category": "example_pack:magic", "skill": "fire_mastery"}
                          ]
                        }
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillRequirements.parseAll(raw);

        assertEquals(Set.of(PYROMANCY, FIRE_MASTERY),
                parsed.get(ResourceLocation.parse("irons_spellbooks:fireball")));
    }

    @Test
    void filePathIsIgnoredSpellsComeFromTheJsonBody() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("some_pack:deeply/nested/name"),
                JsonParser.parseString("""
                        {"spells": ["irons_spellbooks:cone_of_cold"],
                         "requires": [{"category": "some_pack:magic", "skill": "cryomancy"}]}
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillRequirements.parseAll(raw);

        assertTrue(parsed.containsKey(ResourceLocation.parse("irons_spellbooks:cone_of_cold")));
        assertFalse(parsed.containsKey(ResourceLocation.parse("some_pack:deeply/nested/name")));
    }

    @Test
    void skipsMalformedEntries() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:good"),
                JsonParser.parseString("""
                        {"spells": ["irons_spellbooks:fireball"],
                         "requires": [{"category": "example_pack:magic", "skill": "pyromancy"}]}
                        """));
        raw.put(ResourceLocation.parse("example_pack:bad_no_spells"),
                JsonParser.parseString("""
                        {"requires": [{"category": "example_pack:magic", "skill": "pyromancy"}]}
                        """));

        Map<ResourceLocation, Set<SkillKey>> parsed = SpellSkillRequirements.parseAll(raw);

        assertEquals(1, parsed.size());
        assertTrue(parsed.containsKey(ResourceLocation.parse("irons_spellbooks:fireball")));
    }
}
