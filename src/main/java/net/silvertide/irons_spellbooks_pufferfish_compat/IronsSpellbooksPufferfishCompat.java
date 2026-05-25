package net.silvertide.irons_spellbooks_pufferfish_compat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.ClientConfig;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrants;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillBlocks;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillRequirements;
import org.slf4j.Logger;

@Mod(IronsSpellbooksPufferfishCompat.MODID)
public class IronsSpellbooksPufferfishCompat {
    public static final String MODID = "irons_spellbooks_pufferfish_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsSpellbooksPufferfishCompat(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(IronsSpellbooksPufferfishCompat::registerReloadListeners);
    }

    private static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(SpellSkillRequirements.INSTANCE);
        event.addListener(SpellSkillBlocks.INSTANCE);
        event.addListener(InnateSpellGrants.INSTANCE);
    }
}
