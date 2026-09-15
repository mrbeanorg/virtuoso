import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends StyledBackgroundPanel {

    private JLabel titleLabel;
    private JPanel successBanner;
    private JLabel successMsgLabel;

    public DashboardPanel(MainApp app) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(28, 33, 40, 240));
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        card.setPreferredSize(new Dimension(360, 500));

        successBanner = new JPanel(new BorderLayout(5, 0));
        successBanner.setBackground(new Color(30, 70, 50));
        successBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 117, 89), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        successBanner.setMaximumSize(new Dimension(290, 40));
        successBanner.setAlignmentX(Component.CENTER_ALIGNMENT);
        successBanner.setVisible(false);

        successMsgLabel = new JLabel("✅ Logged in successfully!");
        successMsgLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        successMsgLabel.setForeground(new Color(210, 245, 225));

        JButton closeBannerBtn = new JButton("✕");
        closeBannerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBannerBtn.setForeground(new Color(210, 245, 225));
        closeBannerBtn.setContentAreaFilled(false);
        closeBannerBtn.setBorderPainted(false);
        closeBannerBtn.setFocusPainted(false);
        closeBannerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBannerBtn.addActionListener(e -> successBanner.setVisible(false));

        successBanner.add(successMsgLabel, BorderLayout.CENTER);
        successBanner.add(closeBannerBtn, BorderLayout.EAST);

        titleLabel = new JLabel("MAIN DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(240, 245, 250));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton examPrepBtn = LoginPanel.createButton("1. 3-Mark Q&A Bank", new Color(46, 117, 89), Color.WHITE);
        JButton virtuosoBtn = LoginPanel.createButton("2. Algorithm Virtuoso", new Color(46, 117, 89), Color.WHITE);
        JButton logoutBtn = LoginPanel.createButton("3. Logout", new Color(55, 65, 80), Color.WHITE);
        JButton exitBtn = LoginPanel.createButton("4. Exit Application", new Color(180, 50, 50), Color.WHITE);

        examPrepBtn.addActionListener(e -> { successBanner.setVisible(false); app.showView("EXAM_PREP"); });
        virtuosoBtn.addActionListener(e -> { successBanner.setVisible(false); app.showView("VIRTUOSO"); });
        logoutBtn.addActionListener(e -> { successBanner.setVisible(false); app.showView("LOGIN"); });
        exitBtn.addActionListener(e -> System.exit(0));

        card.add(successBanner);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(40));
        card.add(examPrepBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(virtuosoBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(logoutBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(exitBtn);

        add(card);
    }

    public void updateUserGreeting(String username, boolean showBanner) {
        titleLabel.setText("WELCOME, " + username.toUpperCase());
        if (showBanner) {
            successMsgLabel.setText("✅ Logged in as: " + username);
            successBanner.setVisible(true);
        } else {
            successBanner.setVisible(false);
        }
    }
}