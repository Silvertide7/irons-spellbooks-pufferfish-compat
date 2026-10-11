package net.silvertide.irons_spellbooks_pufferfish_compat.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<InnateHudDisplay> HUD_DISPLAY;
    public static final ForgeConfigSpec.ConfigValue<Integer> SELECTED_X_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> SELECTED_Y_OFFSET;

    public static final ForgeConfigSpec.ConfigValue<Boolean> WHEEL_CONSISTENT_SIZE;
    public static final ForgeConfigSpec.ConfigValue<Double> WHEEL_SCALE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("SelectedInnateSpell");
        builder.comment("How the selected-innate-spell badge appears. Always = persistent; Contextual = fades in on change/cast then fades out; Never = hidden.");
        HUD_DISPLAY = builder.defineEnum("display", InnateHudDisplay.Always);
        builder.comment("Pixel offset from the default position (just left of the offhand slot, aligned with the hotbar).");
        SELECTED_X_OFFSET = builder.define("xOffset", 0);
        SELECTED_Y_OFFSET = builder.define("yOffset", 0);
        builder.pop();

        builder.push("InnateWheel");
        builder.comment("If true, the spell wheel size ignores the GUI scale option.");
        WHEEL_CONSISTENT_SIZE = builder.define("ignoreGuiScale", false);
        builder.comment("If ignoreGuiScale is enabled, apply this multiplier to the wheel's size.");
        WHEEL_SCALE = builder.define("ignoreGuiScaleSizeMultiplier", 1.0);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {}
}
