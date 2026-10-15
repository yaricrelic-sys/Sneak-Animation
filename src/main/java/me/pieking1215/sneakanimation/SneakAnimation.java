package me.pieking1215.sneakanimation;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.lang.reflect.Field;

@Mod("sneakanimation")
public class SneakAnimation {
    private static final Logger LOGGER = LogUtils.getLogger();

    Field Camera_eyeHeight;
    Field Camera_eyeHeightOld;

    float start_height;
    float start_previousHeight;

    public SneakAnimation() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
            Config.registerClothConfig();

            try {
                Camera_eyeHeight = Camera.class.getDeclaredField("eyeHeight");
                Camera_eyeHeight.setAccessible(true);

                Camera_eyeHeightOld = Camera.class.getDeclaredField("eyeHeightOld");
                Camera_eyeHeightOld.setAccessible(true);
            } catch (Exception e) {
                LOGGER.error("Failed to find Camera fields", e);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Config.getBoolSafe(Config.GENERAL.enabled, true)) return;
        if (Minecraft.getInstance().isPaused()) return;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Entity entity = camera.getEntity();
        if (entity == null) return;

        if (Config.getBoolSafe(Config.GENERAL.disableAnimation, false)) {
            if (event.phase == TickEvent.Phase.END) {
                float targetHeight = entity.getEyeHeight();
                setHeight(camera, targetHeight);
                setPreviousHeight(camera, targetHeight);
            }
        } else {
            if (event.phase == TickEvent.Phase.START) {
                start_height = getHeight(camera);
                start_previousHeight = getPreviousHeight(camera);
            } else {
                start_previousHeight = start_height;

                double mod = Config.getDoubleSafe(Config.GENERAL.animationSpeed, 1.0);
                if (mod < Config.General.ANIM_MIN) mod = Config.General.ANIM_MIN;
                if (mod > Config.General.ANIM_MAX) mod = Config.General.ANIM_MAX;

                start_height += (entity.getEyeHeight() - start_height) * 0.5f * (float) mod;

                setHeight(camera, start_height);
                setPreviousHeight(camera, start_previousHeight);
            }
        }
    }

    public float getHeight(Camera cam) {
        try {
            return (float) Camera_eyeHeight.get(cam);
        } catch (Exception e) {
            LOGGER.error("getHeight failed", e);
            return 0;
        }
    }

    public float getPreviousHeight(Camera cam) {
        try {
            return (float) Camera_eyeHeightOld.get(cam);
        } catch (Exception e) {
            LOGGER.error("getPreviousHeight failed", e);
            return 0;
        }
    }

    public void setHeight(Camera cam, float val) {
        try {
            Camera_eyeHeight.set(cam, val);
        } catch (Exception e) {
            LOGGER.error("setHeight failed", e);
        }
    }

    public void setPreviousHeight(Camera cam, float val) {
        try {
            Camera_eyeHeightOld.set(cam, val);
        } catch (Exception e) {
            LOGGER.error("setPreviousHeight failed", e);
        }
    }
}
