package org.ltimothy.fclab.gui;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.ltimothy.fclab.dagger.GUIModule.APP_SYSTEM_PLATFORM_NAME;

@Slf4j
public class DefaultGUI extends JFrame {
    private static final String APP_TITLE = "Double Entry Validation Tool";
    private static final Font APP_DEFAULT_FONT = new Font("Serif", Font.PLAIN, 12);

    private static final boolean APP_VISIBILITY = true;
    private static final int APP_DEFAULT_CLOSE_BEHAVIOR = JFrame.EXIT_ON_CLOSE;
    private static final int APP_X_COORDINATE = 100;
    private static final int APP_Y_COORDINATE = 100;
    private static final int APP_WIDTH = 500;
    private static final int APP_HEIGHT = 500;
    private boolean isMac;
    private Container container;

    public DefaultGUI(@Named(APP_SYSTEM_PLATFORM_NAME) final Optional<String> systemPlatform) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                this.isMac = systemPlatform.map(s -> s.toLowerCase().contains("mac")).orElse(false);
                this.container = createDefaultContainer(systemPlatform);
                this.container.add(getDefaultConfigurationPanel(), BorderLayout.NORTH);
                this.container.add(getDefaultInformationPanel(), BorderLayout.SOUTH);
            });
        } catch (final InterruptedException|InvocationTargetException e) {
            log.error("Failed to initialize the default GUI for {}", systemPlatform, e);
        }
    }

    private Container createDefaultContainer(@NonNull final Optional<String> systemPlatform) {
        log.info("Creating default container");
        final Container container = getContentPane();
        container.setLayout(new BorderLayout());

        setTitle(APP_TITLE);
        setBounds(APP_X_COORDINATE, APP_Y_COORDINATE, APP_WIDTH, APP_HEIGHT);
        setDefaultCloseOperation(APP_DEFAULT_CLOSE_BEHAVIOR);
        setVisible(APP_VISIBILITY);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (systemPlatform.isPresent() && systemPlatform.get().toLowerCase().startsWith("mac")) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Double Entry Validation Tool");
            }
        } catch (final Exception e) {
            log.error("Failed to configure native look and feel for {}", systemPlatform, e);
        }

        return container;
    }

    private Panel getDefaultConfigurationPanel() {
        log.info("Creating default configuration panel");
        final Panel configurationPanel = new Panel(new GridLayout(1, 1));
        configurationPanel.setVisible(APP_VISIBILITY);
        configurationPanel.add(createFilePanel());
        return configurationPanel;
    }

    private Panel createFilePanel() {
        log.info("Creating file panel");
        final Panel filePanel = getDefaultGridPanel();
        filePanel.add(createJLabel("Select File"));
        filePanel.add(createJButton("Open File...", action -> {
            log.info("TODO: load a file");
        }));
        return filePanel;
    }

    private Panel getDefaultGridPanel() {
        final Panel defaultGridPanel = new Panel(new GridLayout(1, 2));
        defaultGridPanel.setVisible(APP_VISIBILITY);
        return defaultGridPanel;
    }

    private JButton createJButton(@NonNull final String buttonText, @NonNull ActionListener actionListener) {
        final JButton defaultJButton = new JButton(buttonText);
        defaultJButton.setFont(APP_DEFAULT_FONT);
        defaultJButton.addActionListener(actionListener);
        defaultJButton.setVisible(APP_VISIBILITY);
        return defaultJButton;
    }

    private JLabel createJLabel(@NonNull final String labelText) {
        final JLabel defaultJLabel = new JLabel(labelText);
        defaultJLabel.setFont(APP_DEFAULT_FONT);
        defaultJLabel.setVisible(APP_VISIBILITY);
        return defaultJLabel;
    }

    private Panel getDefaultInformationPanel() {
        log.info("Creating default information panel");
        return new Panel();
    }
}
