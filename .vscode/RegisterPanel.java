import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterPanel extends StyledBackgroundPanel {
    private DatabaseApp db = new DatabaseApp();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;

    public RegisterPanel(MainApp app) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(28, 33, 40, 220));
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        card.setPreferredSize(new Dimension(340, 480));

        JLabel titleLabel = new JLabel("CREATE ACCOUNT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 230, 240));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = LoginPanel.createPlaceholderTextField("Username");
        passwordField = LoginPanel.createPlaceholderPasswordField("Password");
        emailField = LoginPanel.createPlaceholderTextField("Email");

        JButton submitBtn = LoginPanel.createButton("REGISTER", new Color(46, 117, 89), Color.WHITE);
        JButton backBtn = LoginPanel.createButton("BACK TO LOGIN", new Color(55, 65, 80), Color.WHITE);

        submitBtn.addActionListener(e -> handleRegistration(app));
        backBtn.addActionListener(e -> app.showView("LOGIN"));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(15));
        card.add(emailField);
        card.add(Box.createVerticalStrut(25));
        card.add(submitBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(backBtn);

        add(card);
    }

    public void resetFields() {
        usernameField.setText("Username");
        usernameField.setForeground(Color.LIGHT_GRAY);
        passwordField.setText("Password");
        passwordField.setForeground(Color.LIGHT_GRAY);
        passwordField.setEchoChar((char) 0);
        emailField.setText("Email");
        emailField.setForeground(Color.LIGHT_GRAY);
    }

    private void handleRegistration(MainApp app) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();

        if (username.isEmpty() || username.equals("Username") ||
            password.isEmpty() || password.equals("Password") ||
            email.isEmpty() || email.equals("Email")) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (doesUserExist(username)) {
            JOptionPane.showMessageDialog(this, "Username '" + username + "' is already taken!", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO student_login (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Account successfully created for " + username + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetFields();
                app.prefillLogin(username);
                app.showView("LOGIN");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error during registration.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean doesUserExist(String username) {
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
}