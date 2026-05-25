package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellSkillRequirementTest {
    @Test
    void parsesAllThreeFields() {
        JsonElement json = JsonParser.parseString("""
                {
                  "spell": "irons_spellbooks:fireball",
                  "category": "example_pack:magic",
                  "skill": "pyromancy"
                }
                """);

        SpellSkillRequirement parsed = SpellSkillRequirement.CODEC
                .parse(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow();

        assertEquals(ResourceLocation.parse("irons_spellbooks:fireball"), parsed.spell());
        assertEquals(new SkillKey(ResourceLocation.parse("example_pack:magic"), "pyromancy"),
                parsed.requiredSkill());
    }

    @Test
    void roundTripsThroughCodec() {
        SpellSkillRequirement original = new SpellSkillRequirement(
                ResourceLocation.parse("irons_spellbooks:fireball"),
                new SkillKey(ResourceLocation.parse("my_pack:elemental"), "ignite"));

        JsonElement encoded = SpellSkillRequirement.CODEC
                .encodeStart(JsonOps.INSTANCE, original)
                .result()
                .orElseThrow();
        SpellSkillRequirement decoded = SpellSkillRequirement.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        assertEquals(original, decoded);
    }

    @Test
    void rejectsMissingSpellField() {
        JsonElement json = JsonParser.parseString("""
                {"category": "example_pack:magic", "skill": "pyromancy"}
                """);

        assertTrue(SpellSkillRequirement.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }

    @Test
    void rejectsMissingSkillField() {
        JsonElement json = JsonParser.parseString("""
                {"spell": "irons_spellbooks:fireball", "category": "example_pack:magic"}
                """);

        assertTrue(SpellSkillRequirement.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }
}
