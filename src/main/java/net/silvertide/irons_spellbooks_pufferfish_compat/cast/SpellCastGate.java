package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class SpellCastGate {
    public static final String SKILL_REQUIRED_TRANSLATION_KEY =
            "message." + IronsSpellbooksPufferfishCompat.MODID + ".skill_required";

    private SpellCastGate() {}

    @SubscribeEvent
    public static void onSpellPreCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ResourceLocation spellId = ResourceLocation.tryParse(event.getSpellId());
        if (spellId == null) return;

        SkillGateEvaluator.blockIfMissingRequiredSkill(
                player, spellId, SKILL_REQUIRED_TRANSLATION_KEY, () -> event.setCanceled(true));
    }
}
