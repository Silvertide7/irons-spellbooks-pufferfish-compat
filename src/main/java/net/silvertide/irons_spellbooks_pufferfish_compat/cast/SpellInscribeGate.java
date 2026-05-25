package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import io.redspace.ironsspellbooks.api.events.InscribeSpellEvent;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.PuffishSkillsLookup;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.Set;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID)
public final class SpellInscribeGate {
    public static final String SKILL_REQUIRED_TRANSLATION_KEY =
            "message." + IronsSpellbooksPufferfishCompat.MODID + ".skill_required_inscribe";

    private SpellInscribeGate() {}

    @SubscribeEvent
    public static void onInscribeSpell(InscribeSpellEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AbstractSpell spell = event.getSpellData().getSpell();
        ResourceLocation spellId = ResourceLocation.tryParse(spell.getSpellId());
        if (spellId == null) return;

        Set<SkillKey> requiredSkills = SpellCastGate.collectRequiredSkills(spellId);

        IronsSpellbooksPufferfishCompat.LOGGER.info(
                "[inscribe-gate] player={} spell={} required={}",
                player.getGameProfile().getName(), spellId,
                requiredSkills.isEmpty() ? "<none>" : requiredSkills);

        if (requiredSkills.isEmpty()) return;

        for (SkillKey requiredSkill : requiredSkills) {
            if (!PuffishSkillsLookup.hasUnlocked(player, requiredSkill)) {
                IronsSpellbooksPufferfishCompat.LOGGER.info(
                        "[inscribe-gate]   missing {} -> BLOCK", requiredSkill);
                event.setCanceled(true);
                player.displayClientMessage(
                        Component.translatable(SKILL_REQUIRED_TRANSLATION_KEY), true);
                return;
            }
        }
        IronsSpellbooksPufferfishCompat.LOGGER.info("[inscribe-gate]   all skills satisfied -> allow");
    }
}
