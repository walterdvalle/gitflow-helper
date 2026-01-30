package br.com.gitflowhelper.popup;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;
import java.awt.*;

class RoundedBorder implements Border {
    private final int radius;

    RoundedBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(
            java.awt.Component c, Graphics g, int x, int y, int width, int height) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setColor(new JBColor(
                new Color(0xB0B0B0), // light
                new Color(0x777777)  // dark
        ));
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    @Override public java.awt.Insets getBorderInsets(java.awt.Component c) {
        return JBUI.insets(6);
    }

    @Override public boolean isBorderOpaque() {
        return false;
    }
}
