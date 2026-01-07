package br.com.gitflowhelper.toolwindow;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;

public class MyToolWindowPanel extends JPanel {
    private final JTextPane textPane;
    private final HTMLDocument doc;
    private final HTMLEditorKit kit;

    public MyToolWindowPanel() {
        super(new BorderLayout());
        String htmlContent = "<html><body></body></html>";

        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(htmlContent);
        textPane.setEditable(false); // Impede edição pelo usuário

        doc = (HTMLDocument) textPane.getDocument();
        kit = (HTMLEditorKit) textPane.getEditorKit();

        JBScrollPane scrollPane = new JBScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void append(String text) {
        try {
            Element body = doc.getRootElements()[0].getElement(1); // html -> body
            // Insere antes do fechamento do body
            kit.insertHTML(doc, body.getEndOffset() - 1, text, 0, 0,null);
            // Rola para o final automaticamente
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException | IOException e) {
            e.printStackTrace(); // Em produção, use o Logger do IntelliJ
        }
    }
}