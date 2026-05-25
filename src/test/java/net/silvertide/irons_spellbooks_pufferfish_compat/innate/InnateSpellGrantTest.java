package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnateSpellGrantTest {
    @Test
    void parsesSpellAndLevel() {
        JsonElement json = JsonParser.parseString("""
                {"spell": "irons_spellbooks:fireball", "level": 3}
                """);

        InnateSpellGrant parsed = InnateSpellGrant.CODEC
                .parse(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow();

        assertEquals(ResourceLocation.parse("irons_spellbooks:fireball"), parsed.spell());
        assertEquals(3, parsed.level());
    }

    @Test
    void defaultsLevelToOne() {
        JsonElement json = JsonParser.parseString("""
                {"spell": "irons_spellbooks:fireball"}
                """);

        InnateSpellGrant parsed = InnateSpellGrant.CODEC
                .parse(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow();

        assertEquals(InnateSpellGrant.DEFAULT_LEVEL, parsed.level());
    }

    @Test
    void rejectsMissingSpellField() {
        JsonElement json = JsonParser.parseString("""
                {"level": 2}
                """);

        assertTrue(InnateSpellGrant.CODEC.parse(JsonOps.INSTANCE, json).result().isEmpty());
    }
}
