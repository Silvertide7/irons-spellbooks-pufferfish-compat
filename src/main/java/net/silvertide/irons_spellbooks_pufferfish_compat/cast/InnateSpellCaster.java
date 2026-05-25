package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnatePool;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrant;

import java.util.List;

public final class InnateSpellCaster {
    private static final String INNATE_CASTING_SLOT = "innate";
    private static final CastSource INNATE_CAST_SOURCE_FOR_MANA_AND_COOLDOWN = CastSource.SPELLBOOK;

    private InnateSpellCaster() {}

    public static void castFromClientRequest(ServerPlayer player, int poolIndex) {
        List<InnateSpellGrant> pool = InnatePool.currentPool(player);
        if (poolIndex < 0 || poolIndex >= pool.size()) return;
        cast(player, pool.get(poolIndex));
    }

    public static void cast(ServerPlayer player, InnateSpellGrant grant) {
        AbstractSpell spell = SpellRegistry.getSpell(grant.spell());
        if (spell == null || !grant.spell().equals(spell.getSpellResource())) {
            IronsSpellbooksPufferfishCompat.LOGGER.warn(
                    "Innate grant references unknown spell '{}'", grant.spell());
            return;
        }
        spell.attemptInitiateCast(
                ItemStack.EMPTY,
                grant.level(),
                player.level(),
                player,
                INNATE_CAST_SOURCE_FOR_MANA_AND_COOLDOWN,
                true,
                INNATE_CASTING_SLOT
        );
    }
}
