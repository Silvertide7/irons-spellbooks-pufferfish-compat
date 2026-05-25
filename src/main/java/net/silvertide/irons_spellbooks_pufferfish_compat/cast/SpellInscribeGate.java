package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import io.redspace.ironsspellbooks.api.events.InscribeSpellEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class SpellInscribeGate {
    public static final String SKILL_REQUIRED_TRANSLATION_KEY =
            "message." + IronsSpellbooksPufferfishCompat.MODID + ".skill_required_inscribe";

    private SpellInscribeGate() {}

    @SubscribeEvent
    public static void onInscribeSpell(InscribeSpellEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SkillGateEvaluator.blockIfMissingRequiredSkill(
                player,
                event.getSpellData().getSpell().getSpellResource(),
                SKILL_REQUIRED_TRANSLATION_KEY,
                () -> event.setCanceled(true));
    }
}
