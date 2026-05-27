package net.silvertide.irons_spellbooks_pufferfish_compat.requirement;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellSkillRequirementTest {
    @Test
    void parsesSpellsAndRequiredSkills() {
        JsonElement json = JsonParser.parseString("""
                {
                  "spells": ["irons_spellbooks:fireball", "irons_spellbooks:fire_arrow"],
                  "requires": [
                    {"category": "example_pack:magic", "skill": "pyromancy"},
                    {"category": "example_pack:magic", "skill": "fire_mastery"}
                  ]
                }
                """);

        SpellSkillRequirement parsed = SpellSkillRequirement.CODEC
                .parse(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow();

        assertEquals(List.of(
                        ResourceLocation.parse("irons_spellbooks:fireball"),
                        ResourceLocation.parse("irons_spellbooks:fire_arrow")),
                parsed.spells());
        assertEquals(List.of(
                        new SkillKey(ResourceLocation.parse("example_pack:magic"), "pyromancy"),
                        new SkillKey(ResourceLocation.parse("example_pack:magic"), "fire_mastery")),
                parsed.requiredSkills());
    }

    @Test
    void roundTripsThroughCodec() {
        SpellSkillRequirement original = new SpellSkillRequirement(
                List.of(ResourceLocation.parse("irons_spellbooks:fireball")),
                List.of(new SkillKey(ResourceLocation.parse("my_pack:elemental"), "ignite")));

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
    void rejectsMissingSpellsField() {
        JsonElement json = JsonParser.parseString("""
                {"requires": [{"category": "example_pack:magic", "skill": "pyromancy"}]}
                """);

        assertTrue(SpellSkillRequirement.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }

    @Test
    void rejectsMissingRequiresField() {
        JsonElement json = JsonParser.parseString("""
                {"spells": ["irons_spellbooks:fireball"]}
                """);

        assertTrue(SpellSkillRequirement.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }
}
