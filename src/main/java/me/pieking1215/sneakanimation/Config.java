package me.pieking1215.sneakanimation;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Boolean> enabled;
        public static final double ANIM_MIN = 0.1;
        public static final double ANIM_MAX = 2.5;
        public final ForgeConfigSpec.ConfigValue<Double> animationSpeed;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableAnimation;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");

            enabled = builder
                .comment("Enables/Disables the whole Mod [default:true]")
                .define("enableMod", true);

            animationSpeed = builder
                .comment("Modifier for the sneak animation speed [default:1.0]")
                .defineInRange("animationSpeed", 1.0, ANIM_MIN, ANIM_MAX);

            disableAnimation = builder
                .comment("Disables the smooth sneak animation [default:false]")
                .define("disableAnimation", false);

            builder.pop();
        }
    }

    public static void registerClothConfig() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("config.sneakanimation.title"));

                ConfigEntryBuilder eb = builder.entryBuilder();
                ConfigCategory general = builder.getOrCreateCategory(
                    Component.translatable("key.sneakanimation.category.general")
                );

                general.addEntry(eb
                    .startBooleanToggle(
                        Component.translatable("config.sneakanimation.enable"),
                        getBoolSafe(GENERAL.enabled, true)
                    )
                    .setDefaultValue(true)
                    .setSaveConsumer(GENERAL.enabled::set)
                    .build()
                );

                int nTicks = (int) ((General.ANIM_MAX - General.ANIM_MIN) / 0.1) + 1;

                int animV = (int) (((getDoubleSafe(GENERAL.animationSpeed, 1.0) - General.ANIM_MIN)
                    / (General.ANIM_MAX - General.ANIM_MIN)) * nTicks);
                int animDef = (int) (((1.0 - General.ANIM_MIN)
                    / (General.ANIM_MAX - General.ANIM_MIN)) * nTicks);

                general.addEntry(eb
                    .startIntSlider(
                        Component.translatable("config.sneakanimation.animationSpeed"),
                        animV, 0, nTicks
                    )
                    .setDefaultValue(animDef)
                    .setSaveConsumer((i) -> {
                        double thru = i / (double) nTicks;
                        double v = General.ANIM_MIN + (thru * (General.ANIM_MAX - General.ANIM_MIN));
                        v = Math.round(v * 20.0) / 20.0;
                        GENERAL.animationSpeed.set(v);
                    })
                    .setTextGetter((i) -> {
                        double thru = i / (double) nTicks;
                        double v = General.ANIM_MIN + (thru * (General.ANIM_MAX - General.ANIM_MIN));
                        v = Math.round(v * 20.0) / 20.0;
                        int percent = (int) (100 * v);
                        percent = (int) (Math.round(percent / 10.0) * 10);
                        return Component.literal(percent + "%");
                    })
                    .build()
                );

                general.addEntry(eb
                    .startBooleanToggle(
                        Component.translatable("config.sneakanimation.disableAnimation"),
                        getBoolSafe(GENERAL.disableAnimation, false)
                    )
                    .setDefaultValue(false)
                    .setSaveConsumer(GENERAL.disableAnimation::set)
                    .build()
                );

                return builder.setSavingRunnable(SPEC::save).build();
            })
        );
    }

    public static boolean getBoolSafe(ForgeConfigSpec.ConfigValue<Boolean> bool, boolean defaultValue) {
        Object o = bool.get();
        if (!(o instanceof Boolean)) {
            if (o instanceof String) {
                String st = (String) o;
                if (defaultValue) {
                    bool.set(!st.equalsIgnoreCase("false"));
                } else {
                    bool.set(st.equalsIgnoreCase("true"));
                }
            } else {
                bool.set(defaultValue);
            }
        }
        return bool.get();
    }

    public static double getDoubleSafe(ForgeConfigSpec.ConfigValue<Double> bool, double defaultValue) {
        Object o = bool.get();
        if (!(o instanceof Double)) {
            if (o instanceof Float) {
                bool.set(((Float) o).doubleValue());
            } else if (o instanceof String) {
                String st = (String) o;
                try {
                    double d = Double.parseDouble(st);
                    bool.set(d);
                } catch (Exception e) {
                    bool.set(defaultValue);
                }
            } else {
                bool.set(defaultValue);
            }
        }
        return bool.get();
    }
}
