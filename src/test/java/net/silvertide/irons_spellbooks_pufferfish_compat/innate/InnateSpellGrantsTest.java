package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

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

class InnateSpellGrantsTest {
    @Test
    void parsesEntryAndKeysBySkill() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:anywhere"),
                JsonParser.parseString("""
                        {"category": "example_pack:magic", "skill": "fireball",
                         "spell": "irons_spellbooks:fireball", "level": 2}
                        """));

        Map<SkillKey, InnateSpellGrant> parsed = InnateSpellGrants.parseAll(raw);

        SkillKey expectedKey = new SkillKey(ResourceLocation.parse("example_pack:magic"), "fireball");
        assertEquals(1, parsed.size());
        assertEquals(
                new InnateSpellGrant(ResourceLocation.parse("irons_spellbooks:fireball"), 2),
                parsed.get(expectedKey));
    }

    @Test
    void skipsMalformedEntriesIndividually() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:good"),
                JsonParser.parseString("""
                        {"category": "example_pack:magic", "skill": "fireball",
                         "spell": "irons_spellbooks:fireball"}
                        """));
        raw.put(ResourceLocation.parse("example_pack:bad_category_type"),
                JsonParser.parseString("""
                        {"category": 42, "skill": "fireball",
                         "spell": "irons_spellbooks:fireball"}
                        """));
        raw.put(ResourceLocation.parse("example_pack:missing_skill"),
                JsonParser.parseString("""
                        {"category": "example_pack:magic",
                         "spell": "irons_spellbooks:fireball"}
                        """));

        Map<SkillKey, InnateSpellGrant> parsed = InnateSpellGrants.parseAll(raw);

        SkillKey goodKey = new SkillKey(ResourceLocation.parse("example_pack:magic"), "fireball");
        assertTrue(parsed.containsKey(goodKey));
        assertEquals(1, parsed.size());
    }

    @Test
    void defaultLevelAppliesWhenAbsent() {
        Map<ResourceLocation, JsonElement> raw = new HashMap<>();
        raw.put(ResourceLocation.parse("example_pack:any"),
                JsonParser.parseString("""
                        {"category": "example_pack:magic", "skill": "fireball",
                         "spell": "irons_spellbooks:fireball"}
                        """));

        Map<SkillKey, InnateSpellGrant> parsed = InnateSpellGrants.parseAll(raw);
        InnateSpellGrant only = parsed.values().iterator().next();

        assertEquals(InnateSpellGrant.DEFAULT_LEVEL, only.level());
        assertFalse(parsed.isEmpty());
    }
}
