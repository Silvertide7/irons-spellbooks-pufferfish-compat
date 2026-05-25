package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellSkillRequirementsTest {
    @Test
    void keysParsedEntriesByTheSpellFieldInsideTheJsonNotByFilePath() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:requirements/anywhere"),
                JsonParser.parseString("""
                        {
                          "spell": "irons_spellbooks:fireball",
                          "category": "example_pack:magic",
                          "skill": "pyromancy"
                        }
                        """));

        Map<ResourceLocation, SkillKey> parsed = SpellSkillRequirements.parseAll(raw);

        assertEquals(1, parsed.size());
        assertEquals(
                new SkillKey(ResourceLocation.parse("example_pack:magic"), "pyromancy"),
                parsed.get(ResourceLocation.parse("irons_spellbooks:fireball")));
        assertFalse(parsed.containsKey(ResourceLocation.parse("example_pack:requirements/anywhere")));
    }

    @Test
    void filePathCanBeAnythingAsLongAsSpellFieldIsCorrect() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("some_pack:deeply/nested/name"),
                JsonParser.parseString("""
                        {
                          "spell": "irons_spellbooks:cone_of_cold",
                          "category": "some_pack:magic",
                          "skill": "cryomancy"
                        }
                        """));

        Map<ResourceLocation, SkillKey> parsed = SpellSkillRequirements.parseAll(raw);

        assertTrue(parsed.containsKey(ResourceLocation.parse("irons_spellbooks:cone_of_cold")));
    }

    @Test
    void skipsMalformedEntries() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:good"),
                JsonParser.parseString("""
                        {"spell": "irons_spellbooks:fireball",
                         "category": "example_pack:magic", "skill": "pyromancy"}
                        """));
        raw.put(ResourceLocation.parse("example_pack:bad_no_spell"),
                JsonParser.parseString("""
                        {"category": "example_pack:magic", "skill": "pyromancy"}
                        """));

        Map<ResourceLocation, SkillKey> parsed = SpellSkillRequirements.parseAll(raw);

        assertEquals(1, parsed.size());
        assertTrue(parsed.containsKey(ResourceLocation.parse("irons_spellbooks:fireball")));
    }
}
