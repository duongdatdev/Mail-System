package client.ui;

import client.MailClient;
import model.Mail;

import javax.swing.*;
import java.awt.*;

/**
 * Swing UI for the Java Mail Client.
 * Allows sending and receiving mail through GUI buttons.
 * Now includes digital signature verification display.
 */
public class MailClientUI extends JFrame {

    private final MailClient client;
    private final JTextArea logArea;
    private String userAddress = "user@localhost";

    public MailClientUI() {
        super("Java Mail Client (Swing Edition)");

    // Ask user for their email address to use as the From field and mailbox for POP3
    String address = JOptionPane.showInputDialog(this, "Enter your email address (used as mailbox):", "user@localhost");
    if (address != null && !address.trim().isEmpty()) userAddress = address.trim();
    // Connect to localhost server (client knows the user's address for POP3)
    client = new MailClient("127.0.0.1", 2525, 1110, userAddress);
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);

        // Buttons
        JButton sendButton = new JButton("📤 Send Mail");
        JButton receiveButton = new JButton("📥 Receive Mail");
        JButton clearButton = new JButton("🧹 Clear Log");

        sendButton.addActionListener(e -> openSendDialog());
        receiveButton.addActionListener(e -> openReceiveDialog());
        clearButton.addActionListener(e -> logArea.setText(""));

        // Layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(receiveButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Window setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /** Open dialog to send a new mail. */
    private void openSendDialog() {
    JTextField fromField = new JTextField();
    fromField.setText(userAddress);
        JTextField toField = new JTextField();
        JTextField subjectField = new JTextField();
        JTextArea bodyArea = new JTextArea(5, 30);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("From:"));
        panel.add(fromField);
        panel.add(new JLabel("To:"));
        panel.add(toField);
        panel.add(new JLabel("Subject:"));
        panel.add(subjectField);
        panel.add(new JLabel("Body:"));
        panel.add(new JScrollPane(bodyArea));

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Compose Mail", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                Mail mail = new Mail(
                        fromField.getText().trim(),
                        toField.getText().trim(),
                        subjectField.getText().trim(),
                        bodyArea.getText().trim()
                );
                client.sendMail(mail);
                logArea.append("✅ Sent mail: " + mail + "\n");
            } catch (Exception ex) {
                logArea.append("❌ Error sending mail: " + ex.getMessage() + "\n");
            }
        }
    }

    /** Open dialog to list and read received mails. */
    private void openReceiveDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter message ID to read (0 to just list):", "Receive Mail",
                JOptionPane.PLAIN_MESSAGE);

        if (input == null || input.isEmpty()) return;

        try {
            int id = Integer.parseInt(input.trim());
            String out = client.listAndRead(id);
            
            // Parse the output to extract signature information
            String signatureStatus = extractSignatureStatus(out);
            
            logArea.append(out);
            if (!signatureStatus.isEmpty()) {
                logArea.append("� " + signatureStatus + "\n");
            }
            logArea.append("�📥 Listed/Read message id " + id + "\n");
        } catch (Exception ex) {
            logArea.append("❌ Error reading mail: " + ex.getMessage() + "\n");
        }
    }

    /**
     * Extract and display signature verification status from email response.
     * Looks for X-Signature-Status header in the email.
     */
    private String extractSignatureStatus(String emailResponse) {
        if (emailResponse.contains("X-Signature-Status:")) {
            String[] lines = emailResponse.split("\n");
            for (String line : lines) {
                if (line.contains("X-Signature-Status:")) {
                    String status = line.substring(line.indexOf("X-Signature-Status:") + "X-Signature-Status:".length()).trim();
                    if (status.contains("VALID")) {
                        return "✅ Digital Signature VALID - Email authenticated";
                    } else if (status.contains("INVALID")) {
                        return "⚠️ Digital Signature INVALID - Email may be tampered!";
                    }
                }
            }
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MailClientUI::new);
    }
}
