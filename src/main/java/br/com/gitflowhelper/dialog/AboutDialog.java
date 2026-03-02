package br.com.gitflowhelper.dialog;

import br.com.gitflowhelper.util.PluginInfoUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends DialogWrapper {

    private static final String PLUGIN_NAME = "GitFlow Helper";
    private static final String PLUGIN_VERSION = PluginInfoUtil.getPluginVersion();
    private static final String GITHUB_URL = "https://github.com/walterdvalle/gitflow-helper";
    private static final String MARKETPLACE_URL = "https://plugins.jetbrains.com/plugin/30207-git-flow-helper";
    private static final String KOFI_URL = "https://ko-fi.com/waltervalle";

    public AboutDialog(@Nullable Project project) {
        super(project);
        setTitle("About " + PLUGIN_NAME);
        setResizable(false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(JBUI.Borders.empty(12));

        // Top: icon + title/version
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel iconLabel = new JLabel(loadPluginIcon());
        iconLabel.setBorder(JBUI.Borders.emptyTop(2));
        header.add(iconLabel, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new VerticalLayout(2));
        titlePanel.setOpaque(false);

        JBLabel title = new JBLabel(PLUGIN_NAME);
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() + 4f));
        titlePanel.add(title);

        JBLabel version = new JBLabel("Version " + PLUGIN_VERSION);
        version.setForeground(JBColor.GRAY);
        titlePanel.add(version);

        String ideName = ApplicationNamesInfo.getInstance().getFullProductName();
        String ideVersion = ApplicationInfo.getInstance().getFullVersion();
        JBLabel ideInfo = new JBLabel("Running on " + ideName + " " + ideVersion);
        ideInfo.setForeground(JBColor.GRAY);
        titlePanel.add(ideInfo);

        header.add(titlePanel, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);

        // Center: description
        JBTextArea desc = new JBTextArea();
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setOpaque(false);
        desc.setBorder(JBUI.Borders.emptyTop(10));
        desc.setText(
                "Implements the classic Git Flow workflow inside the IDE.\n\n" +
                "• Init Git Flow (main/develop, prefixes)\n" +
                "• Feature / Release / Hotfix actions\n" +
                "• Local and remote branch navigation\n" +
                "• Command tracking tool window\n\n" +
                "Project: " + GITHUB_URL
        );

        JBScrollPane scroll = new JBScrollPane(desc);
        scroll.setBorder(null);
        root.add(scroll, BorderLayout.CENTER);

        // Bottom: button links
        JPanel links = new JPanel(new GridBagLayout());
        links.setOpaque(false);
        links.setBorder(JBUI.Borders.emptyTop(10));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = JBUI.insetsRight(8);
        c.anchor = GridBagConstraints.WEST;

        // GitHub
        c.gridx = 0;
        JButton githubBtn = new JButton("Open GitHub \uD83D\uDC19");
        githubBtn.addActionListener(e -> BrowserUtil.browse(GITHUB_URL));
        links.add(githubBtn, c);

        // Marketplace
        c.gridx = 2;
        JButton marketplaceBtn = new JButton("Open Marketplace \uD83E\uDDE9");
        marketplaceBtn.addActionListener(e -> BrowserUtil.browse(MARKETPLACE_URL));
        links.add(marketplaceBtn, c);

        // Buy me a Coffee
        c.gridx = 1;
        JButton coffeeBtn = new JButton("Buy me a Coffee ☕");
        coffeeBtn.addActionListener(e -> BrowserUtil.browse(KOFI_URL));
        links.add(coffeeBtn, c);

        root.add(links, BorderLayout.SOUTH);

        // define an amazing size
        root.setPreferredSize(new Dimension(520, 320));
        return root;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    private @Nullable Icon loadPluginIcon() {
        return PluginIcons.GitFlowBig;
    }

}