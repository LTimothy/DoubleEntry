package org.ltimothy.fclab.dagger;

import dagger.Module;
import dagger.Provides;
import lombok.extern.slf4j.Slf4j;
import org.ltimothy.fclab.gui.DefaultGUI;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JFrame;
import java.util.Optional;

@Slf4j
@Module
public class GUIModule extends JFrame {
    public static final String APP_SYSTEM_PLATFORM_NAME = "SYSTEM_PLATFORM";

    @Provides
    @Singleton
    @Named(APP_SYSTEM_PLATFORM_NAME)
    public Optional<String> providesSystemPlatform() {
        final Optional<String> systemPlatform = Optional.ofNullable(System.getProperty("os.name"));
        log.info("System Platform: {}", systemPlatform);
        return systemPlatform;
    }

    @Provides
    @Singleton
    public DefaultGUI providesDefaultGUI(@Named(APP_SYSTEM_PLATFORM_NAME) Optional<String> systemPlatform) {
        return new DefaultGUI(systemPlatform);
    }
}
