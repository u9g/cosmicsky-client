package dev.u9g;

import moe.nea.libautoupdate.CurrentVersion;
import moe.nea.libautoupdate.UpdateContext;
import moe.nea.libautoupdate.UpdateSource;
import moe.nea.libautoupdate.UpdateTarget;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaMain implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

    @Override
    public void onInitialize() {
        LOGGER.info("Hello world from SkyPlus!!");
        UpdateContext updateContext = new UpdateContext(
                UpdateSource.gistSource("u9g", "09895d022cf0e94a5af5b5c5dda81523"),
                UpdateTarget.replaceJar(JavaMain.class),
                CurrentVersion.of(1),
                "skyplus"
        );

        updateContext.checkUpdate("upstream").thenAccept((potentialUpdate) -> {
            if (potentialUpdate.isUpdateAvailable()) {
                potentialUpdate.launchUpdate();
            }
        });
        SkyplusClient.INSTANCE.onInitializeClient();
    }
}