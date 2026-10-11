package net.silvertide.irons_spellbooks_pufferfish_compat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.ClientConfig;
import net.silvertide.irons_spellbooks_pufferfish_compat.innate.InnateSpellGrants;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.CompatPayloads;
import net.silvertide.irons_spellbooks_pufferfish_compat.requirement.SpellSkillRequirements;
import net.silvertide.irons_spellbooks_pufferfish_compat.skills.InnateSyncBootstrap;
import org.slf4j.Logger;

@Mod(IronsSpellbooksPufferfishCompat.MODID)
public class IronsSpellbooksPufferfishCompat {
    public static final String MODID = "irons_spellbooks_pufferfish_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsSpellbooksPufferfishCompat(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        context.getModEventBus().addListener(InnateSyncBootstrap::registerSkillChangeListeners);
        MinecraftForge.EVENT_BUS.addListener(IronsSpellbooksPufferfishCompat::registerReloadListeners);
        CompatPayloads.register();
    }

    private static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(SpellSkillRequirements.INSTANCE);
        event.addListener(InnateSpellGrants.INSTANCE);
    }
}
