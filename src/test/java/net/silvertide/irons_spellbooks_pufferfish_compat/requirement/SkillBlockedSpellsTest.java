package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillBlockedSpellsTest {
    @Test
    void parsesWellFormedFile() {
        JsonElement json = JsonParser.parseString("""
                {
                  "category": "example_pack:magic",
                  "skill": "pyromancy",
                  "blocks": ["irons_spellbooks:fireball", "irons_spellbooks:fire_arrow"]
                }
                """);

        SkillBlockedSpells parsed = SkillBlockedSpells.CODEC
                .parse(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow();

        assertEquals(new SkillKey(ResourceLocation.parse("example_pack:magic"), "pyromancy"),
                parsed.blockingSkill());
        assertEquals(2, parsed.blockedSpells().size());
        assertEquals(ResourceLocation.parse("irons_spellbooks:fireball"), parsed.blockedSpells().get(0));
        assertEquals(ResourceLocation.parse("irons_spellbooks:fire_arrow"), parsed.blockedSpells().get(1));
    }

    @Test
    void rejectsMissingBlocksField() {
        JsonElement json = JsonParser.parseString("""
                {"category": "example_pack:magic", "skill": "pyromancy"}
                """);

        assertTrue(SkillBlockedSpells.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }

    @Test
    void rejectsMissingSkillField() {
        JsonElement json = JsonParser.parseString("""
                {"category": "example_pack:magic", "blocks": ["irons_spellbooks:fireball"]}
                """);

        assertTrue(SkillBlockedSpells.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }

    @Test
    void emptyBlocksListIsAllowed() {
        JsonElement json = JsonParser.parseString("""
                {"category": "example_pack:magic", "skill": "pyromancy", "blocks": []}
                """);

        SkillBlockedSpells parsed = SkillBlockedSpells.CODEC
                .parse(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow();

        assertTrue(parsed.blockedSpells().isEmpty());
    }
}
