package org.ltimothy.fclab.dagger;

import dagger.Component;
import org.ltimothy.fclab.handler.DoubleEntryHandler;

import javax.inject.Singleton;

@Singleton
@Component(modules = GUIModule.class)
public interface ServiceComponent {
    DoubleEntryHandler launchGUI();
}
