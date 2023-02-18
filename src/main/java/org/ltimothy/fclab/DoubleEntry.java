package org.ltimothy.fclab;

import org.ltimothy.fclab.dagger.DaggerServiceComponent;
import org.ltimothy.fclab.dagger.GUIModule;
import org.ltimothy.fclab.dagger.ServiceComponent;

public class DoubleEntry {
    public static void main(final String[] args) {
        final ServiceComponent serviceComponent = DaggerServiceComponent.builder().gUIModule(new GUIModule()).build();
        serviceComponent.launchGUI();
    }
}
