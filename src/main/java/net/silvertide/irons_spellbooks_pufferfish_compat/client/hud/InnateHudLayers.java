package net.silvertide.irons_spellbooks_pufferfish_compat.client.hud;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;

@EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID, value = Dist.CLIENT)
public final class InnateHudLayers {
    private static final ResourceLocation SELECTED_LAYER_ID = ResourceLocation.fromNamespaceAndPath(
            IronsSpellbooksPufferfishCompat.MODID, "innate_selected_spell");
    private static final ResourceLocation WHEEL_LAYER_ID = ResourceLocation.fromNamespaceAndPath(
            IronsSpellbooksPufferfishCompat.MODID, "innate_spell_wheel");

    private InnateHudLayers() {}

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(SELECTED_LAYER_ID, InnateSelectedSpellOverlay.INSTANCE);
        event.registerAboveAll(WHEEL_LAYER_ID, InnateSpellWheelOverlay.INSTANCE);
    }
}
