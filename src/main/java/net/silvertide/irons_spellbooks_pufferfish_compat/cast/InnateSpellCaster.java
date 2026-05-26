package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnatePool;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpells;

import java.util.Optional;

public final class InnateSpellCaster {
    private static final String INNATE_CASTING_SLOT = "innate";
    private static final CastSource INNATE_CAST_SOURCE = CastSource.SPELLBOOK;

    private InnateSpellCaster() {}

    public static void castFromClientRequest(ServerPlayer player, ResourceLocation requestedSpellId) {
        Optional<InnateSpellGrant> authoritativeGrant = InnatePool.currentPool(player).stream()
                .filter(grant -> grant.spell().equals(requestedSpellId))
                .findFirst();
        if (authoritativeGrant.isEmpty()) return;
        cast(player, authoritativeGrant.get());
    }

    public static void cast(ServerPlayer player, InnateSpellGrant grant) {
        Optional<AbstractSpell> resolved = InnateSpells.resolve(grant.spell());
        if (resolved.isEmpty()) {
            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                    "Innate grant references unknown spell '{}'", grant.spell());
            return;
        }
        AbstractSpell spell = resolved.get();
        spell.attemptInitiateCast(
                ItemStack.EMPTY,
                InnateSpells.castableLevel(spell, grant.level()),
                player.serverLevel(),
                player,
                INNATE_CAST_SOURCE,
                true,
                INNATE_CASTING_SLOT
        );
    }
}
