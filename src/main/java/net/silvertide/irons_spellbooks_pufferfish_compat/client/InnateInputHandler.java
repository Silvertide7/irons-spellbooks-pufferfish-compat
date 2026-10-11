package net.silvertide.irons_spellbooks_pufferfish_compat.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.silvertide.irons_spellbooks_pufferfish_compat.IronsSpellbooksPufferfishCompat;
import net.silvertide.irons_spellbooks_pufferfish_compat.client.hud.InnateSelectedSpellOverlay;
import net.silvertide.irons_spellbooks_pufferfish_compat.client.hud.InnateSpellWheelOverlay;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.ClientConfig;
import net.silvertide.irons_spellbooks_pufferfish_compat.config.InnateHudDisplay;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.CastInnatePayload;
import net.silvertide.irons_spellbooks_pufferfish_compat.network.CompatPayloads;

@Mod.EventBusSubscriber(modid = IronsSpellbooksPufferfishCompat.MODID, value = Dist.CLIENT)
public final class InnateInputHandler {
    private static final int CONTEXTUAL_REVEAL_TICKS_ON_SCROLL_HOLD = 40;
    private static boolean wasWheelKeyDown = false;

    private InnateInputHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        while (InnateKeybinds.CAST_INNATE.consumeClick()) {
            ClientInnateState.selectedGrant().ifPresent(grant -> {
                CompatPayloads.sendToServer(new CastInnatePayload(grant.spell()));
                InnateSelectedSpellOverlay.reveal();
            });
        }

        while (InnateKeybinds.OPEN_WHEEL.consumeClick()) {
            if (!wasWheelKeyDown) {
                InnateSpellWheelOverlay.INSTANCE.open();
                wasWheelKeyDown = true;
            }
        }
        handleWheelKeyRelease();

        if (InnateKeybinds.TOGGLE_WHEEL.consumeClick()) {
            if (InnateSpellWheelOverlay.INSTANCE.isActive()) {
                InnateSpellWheelOverlay.INSTANCE.close();
            } else {
                InnateSpellWheelOverlay.INSTANCE.open();
            }
            drainRemainingToggleWheelClicks();
        }

        if (InnateKeybinds.SCROLL_CYCLE_MODIFIER.isDown()
                && ClientConfig.HUD_DISPLAY.get() == InnateHudDisplay.Contextual) {
            InnateSelectedSpellOverlay.revealForTicks(CONTEXTUAL_REVEAL_TICKS_ON_SCROLL_HOLD);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().player == null) return;
        if (!InnateKeybinds.SCROLL_CYCLE_MODIFIER.isDown()) return;
        if (ClientInnateState.pool().isEmpty()) return;

        double scrollDelta = event.getScrollDelta();
        if (scrollDelta > 0) {
            ClientInnateState.cyclePrevious();
        } else if (scrollDelta < 0) {
            ClientInnateState.cycleNext();
        } else {
            return;
        }
        InnateSelectedSpellOverlay.reveal();
        event.setCanceled(true);
    }

    private static void handleWheelKeyRelease() {
        boolean isDown = InnateKeybinds.OPEN_WHEEL.isDown();
        boolean wasReleased = wasWheelKeyDown && !isDown;
        if (wasReleased && InnateSpellWheelOverlay.INSTANCE.isActive()) {
            InnateSpellWheelOverlay.INSTANCE.close();
        }
        wasWheelKeyDown = isDown;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void drainRemainingToggleWheelClicks() {
        while (InnateKeybinds.TOGGLE_WHEEL.consumeClick()) ;
    }
}
