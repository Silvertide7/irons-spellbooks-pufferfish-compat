package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

import java.util.Optional;

public final class InnateSpells {
    private InnateSpells() {}

    public static Optional<AbstractSpell> resolve(ResourceLocation spellId) {
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null || !spellId.equals(spell.getSpellResource())) {
            return Optional.empty();
        }
        return Optional.of(spell);
    }

    public static int castableLevel(AbstractSpell spell, int requestedLevel) {
        int clampedLevel = Mth.clamp(requestedLevel, spell.getMinLevel(), spell.getMaxLevel());
        if (clampedLevel != requestedLevel) {
            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                    "Innate grant for '{}' requests level {} outside the spell's range [{}, {}]; clamping to {}",
                    spell.getSpellResource(), requestedLevel,
                    spell.getMinLevel(), spell.getMaxLevel(), clampedLevel);
        }
        return clampedLevel;
    }
}
