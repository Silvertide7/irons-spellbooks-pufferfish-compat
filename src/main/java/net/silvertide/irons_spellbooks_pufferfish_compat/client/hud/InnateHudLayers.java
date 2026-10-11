package net.silvertide.irons_spellbooks_pufferfish_compat.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

@Mod.EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class InnateHudLayers {
    private static final String SELECTED_OVERLAY_ID = "innate_selected_spell";
    private static final String WHEEL_OVERLAY_ID = "innate_spell_wheel";

    private InnateHudLayers() {}

    @SubscribeEvent
    static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(SELECTED_OVERLAY_ID, InnateSelectedSpellOverlay.INSTANCE);
        event.registerAboveAll(WHEEL_OVERLAY_ID, InnateSpellWheelOverlay.INSTANCE);
    }
}
