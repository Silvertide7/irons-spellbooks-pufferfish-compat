package net.silvertide.irons_spellbooks_pufferfish_compat.cast;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillBlocks;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillRequirements;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.PuffishSkillsLookup;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.SkillKey;

import java.util.HashSet;
import java.util.Set;

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

        Set<SkillKey> requiredSkills = collectRequiredSkills(spellId);

        IronsSpellbooksPufferfishCompat.LOGGER.info(
                "[gate] player={} spell={} source={} required={}",
                player.getGameProfile().getName(), spellId, event.getCastSource(),
                requiredSkills.isEmpty() ? "<none>" : requiredSkills);

        if (requiredSkills.isEmpty()) return;

        for (SkillKey requiredSkill : requiredSkills) {
            if (!PuffishSkillsLookup.hasUnlocked(player, requiredSkill)) {
                IronsSpellbooksPufferfishCompat.LOGGER.info(
                        "[gate]   missing {} -> BLOCK", requiredSkill);
                event.setCanceled(true);
                player.displayClientMessage(
                        Component.translatable(SKILL_REQUIRED_TRANSLATION_KEY), true);
                return;
            }
        }
        IronsSpellbooksPufferfishCompat.LOGGER.info("[gate]   all skills satisfied -> allow");
    }

    static Set<SkillKey> collectRequiredSkills(ResourceLocation spellId) {
        Set<SkillKey> requiredSkills = new HashSet<>();
        SpellSkillRequirements.INSTANCE.findForSpell(spellId).ifPresent(requiredSkills::add);
        requiredSkills.addAll(SpellSkillBlocks.INSTANCE.blockersFor(spellId));
        return requiredSkills;
    }
}
