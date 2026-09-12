import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPanel extends StyledBackgroundPanel {
    private DatabaseApp db = new DatabaseApp();
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(MainApp app) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(28, 33, 40, 220));
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        card.setPreferredSize(new Dimension(340, 530));

        JLabel titleLabel = new JLabel("MEMBER LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 230, 240));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = createPlaceholderTextField("Username");
        passwordField = createPlaceholderPasswordField("Password");

        JButton loginBtn = createButton("LOGIN", new Color(46, 117, 89), Color.WHITE);
        JButton registerBtn = createButton("REGISTER", new Color(55, 65, 80), Color.WHITE);
        JButton exitBtn = createButton("EXIT APPLICATION", new Color(180, 50, 50), Color.WHITE);

        loginBtn.addActionListener(e -> handleLogin(app));
        registerBtn.addActionListener(e -> app.showView("REGISTER"));
        exitBtn.addActionListener(e -> System.exit(0));

        JLabel forgotLabel = new JLabel("Forgot Password? Click Here");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(140, 160, 180));
        forgotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JSeparator line = new JSeparator();
        line.setMaximumSize(new Dimension(280, 1));
        line.setForeground(new Color(60, 70, 85));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(forgotLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(line);
        card.add(Box.createVerticalStrut(18));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(exitBtn);

        add(card);
    }

    public void resetFields() {
        usernameField.setText("Username");
        usernameField.setForeground(Color.LIGHT_GRAY);
        passwordField.setText("Password");
        passwordField.setForeground(Color.LIGHT_GRAY);
        passwordField.setEchoChar((char) 0);
    }

    public void setLoginUsername(String username) {
        usernameField.setText(username);
        usernameField.setForeground(Color.WHITE);
        passwordField.setText("Password");
        passwordField.setForeground(Color.LIGHT_GRAY);
        passwordField.setEchoChar((char) 0);
    }

    private void handleLogin(MainApp app) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.equals("Username") || username.isEmpty() || password.equals("Password") || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isUsernameValid(username)) {
            JOptionPane.showMessageDialog(this, "Access Denied: Username does not exist.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (verifyPassword(username, password)) {
            app.setLoggedInUser(username, true);
            app.showView("DASHBOARD");
        } else {
            JOptionPane.showMessageDialog(this, "Access Denied: Incorrect password.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isUsernameValid(String username) {
        String sql = "SELECT COUNT(*) FROM student_login WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyPassword(String username, String password) {
       
        String sql = "SELECT * FROM student_login WHERE username = ? AND password = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JTextField createPlaceholderTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setMaximumSize(new Dimension(280, 40));
        tf.setBackground(new Color(20, 25, 32));
        tf.setForeground(Color.LIGHT_GRAY);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.WHITE);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        return tf;
    }

    public static JPasswordField createPlaceholderPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField(placeholder);
        pf.setMaximumSize(new Dimension(280, 40));
        pf.setBackground(new Color(20, 25, 32));
        pf.setForeground(Color.LIGHT_GRAY);
        pf.setCaretColor(Color.WHITE);
        pf.setEchoChar((char) 0);
        pf.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(pf.getPassword()).equals(placeholder)) {
                    pf.setText("");
                    pf.setEchoChar('•');
                    pf.setForeground(Color.WHITE);
                }
            }
            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) {
                    pf.setText(placeholder);
                    pf.setEchoChar((char) 0);
                    pf.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        return pf;
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(280, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}