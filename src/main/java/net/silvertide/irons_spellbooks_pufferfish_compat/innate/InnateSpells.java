package net.silvertide.irons_spellbooks_pufferfish_compat.innate;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;

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
}
