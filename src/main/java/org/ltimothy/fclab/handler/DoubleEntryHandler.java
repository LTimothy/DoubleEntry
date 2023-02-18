package org.ltimothy.fclab.handler;

import org.ltimothy.fclab.gui.DefaultGUI;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

public class DoubleEntryHandler {
    private final DefaultGUI defaultGUI;
    @Inject
    public DoubleEntryHandler(final DefaultGUI defaultGUI) {
        this.defaultGUI = defaultGUI;
    }

    public void launchGUI() {
        SwingUtilities.invokeLater(() -> new DoubleEntryHandler(defaultGUI));
    }
}
