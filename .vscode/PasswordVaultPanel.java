import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.RoundRectangle2D;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class PasswordVaultPanel extends JPanel {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private String loggedInUser;
    private JTextField appNameField;
    private JTextField accountUsernameField;
    private JLabel generatedPasswordDisplay;
    private JSpinner lengthSpinner;
    private JCheckBox useUpperCB, useDigitsCB, useSymbolsCB;
    private StrengthBarPanel strengthMeter;
    private JLabel strengthTextLabel;

    private DefaultTableModel vaultTableModel;
    private JTable vaultTable;
    private Set<Integer> revealedRows = new HashSet<>();

    public PasswordVaultPanel(String activeUser, Runnable onBackToMenu) {
        this.loggedInUser = activeUser;

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(20, 24, 29));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        Color cardBg = new Color(28, 33, 40);
        Color textColor = new Color(240, 245, 250);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel headerTitle = new JLabel("Secure Password Vault - " + loggedInUser.toUpperCase());
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerTitle.setForeground(textColor);

        JButton dashboardBtn = LoginPanel.createButton("Dashboard", new Color(55, 65, 80), Color.WHITE);
        dashboardBtn.setPreferredSize(new Dimension(130, 38));
        dashboardBtn.addActionListener(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        headerPanel.add(headerTitle, BorderLayout.WEST);
        headerPanel.add(dashboardBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        splitContainer.setOpaque(false);

        JPanel leftPanel = createRoundedCard(cardBg);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel leftTitle = new JLabel("Generator & Account Details");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        leftTitle.setForeground(textColor);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        appNameField = LoginPanel.createPlaceholderTextField("E.g. Google, GitHub");
        accountUsernameField = LoginPanel.createPlaceholderTextField("E.g. alan.wilson@gmail.com");

        useUpperCB = createStyledCheckBox("Uppercase (A-Z)", true);
        useDigitsCB = createStyledCheckBox("Digits (0-9)", true);
        useSymbolsCB = createStyledCheckBox("Symbols (!@#$)", true);

        ChangeListener optionChangeListener = (ChangeEvent e) -> generatePassword();
        useUpperCB.addChangeListener(optionChangeListener);
        useDigitsCB.addChangeListener(optionChangeListener);
        useSymbolsCB.addChangeListener(optionChangeListener);

        lengthSpinner = new JSpinner(new SpinnerNumberModel(14, 6, 64, 1));
        lengthSpinner.setMaximumSize(new Dimension(80, 32));
        lengthSpinner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lengthSpinner.setBorder(BorderFactory.createEmptyBorder());

        JPanel lengthRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        lengthRow.setOpaque(false);
        lengthRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lengthRow.add(createLabel("Length:"));
        lengthRow.add(lengthSpinner);

        strengthMeter = new StrengthBarPanel();
        strengthTextLabel = new JLabel("STRONG");
        strengthTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        strengthTextLabel.setForeground(new Color(46, 204, 113));

        JPanel strengthTextRow = new JPanel(new BorderLayout());
        strengthTextRow.setOpaque(false);
        strengthTextRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthTextRow.setMaximumSize(new Dimension(420, 20));
        strengthTextRow.add(createLabel("Password Strength:"), BorderLayout.WEST);
        strengthTextRow.add(strengthTextLabel, BorderLayout.EAST);

        JButton generateBtn = LoginPanel.createButton("⚡ Generate Password", new Color(39, 138, 90), Color.WHITE);
        generateBtn.addActionListener(e -> generatePassword());

        generatedPasswordDisplay = new JLabel("R(C$8EJsRX;EBK", SwingConstants.CENTER);
        generatedPasswordDisplay.setFont(new Font("Consolas", Font.BOLD, 18));
        generatedPasswordDisplay.setForeground(new Color(240, 245, 250));
        generatedPasswordDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        generatedPasswordDisplay.setPreferredSize(new Dimension(300, 40));

        JButton saveBtn = LoginPanel.createButton("💾 Save Password to Vault", new Color(60, 100, 180), Color.WHITE);
        saveBtn.addActionListener(e -> saveGeneratedPasswordToDB());

        leftPanel.add(leftTitle);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(createLabel("Target App/Service Name:"));
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(appNameField);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(createLabel("Account Username:"));
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(accountUsernameField);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(createLabel("Password Options:"));
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(useUpperCB);
        leftPanel.add(useDigitsCB);
        leftPanel.add(useSymbolsCB);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(lengthRow);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(strengthTextRow);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(strengthMeter);
        leftPanel.add(Box.createVerticalStrut(25));
        leftPanel.add(generateBtn);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(generatedPasswordDisplay);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(saveBtn);

        JPanel rightPanel = createRoundedCard(cardBg);
        rightPanel.setLayout(new BorderLayout(0, 15));
        rightPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel rightTitle = new JLabel("Saved Account Vault");
        rightTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rightTitle.setForeground(textColor);

        String[] columns = {"ID", "App/Service", "Account Username", "Password", "Action"};
        vaultTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        vaultTable = new JTable(vaultTableModel);
        vaultTable.setBackground(new Color(20, 25, 32));
        vaultTable.setForeground(textColor);
        vaultTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vaultTable.setRowHeight(40); 
        vaultTable.setShowGrid(false); 
        vaultTable.setIntercellSpacing(new Dimension(0, 0));
        vaultTable.setSelectionBackground(new Color(46, 117, 89));
        vaultTable.setSelectionForeground(Color.WHITE);

        vaultTable.getTableHeader().setBackground(new Color(35, 42, 52));
        vaultTable.getTableHeader().setForeground(textColor);
        vaultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        vaultTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        vaultTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        vaultTable.getColumnModel().getColumn(0).setMinWidth(0);
        vaultTable.getColumnModel().getColumn(0).setMaxWidth(0);
        vaultTable.getColumnModel().getColumn(0).setWidth(0);

        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer();
        paddedRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        vaultTable.getColumnModel().getColumn(1).setCellRenderer(paddedRenderer);
        vaultTable.getColumnModel().getColumn(2).setCellRenderer(paddedRenderer);

        vaultTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (revealedRows.contains(row)) {
                    l.setText(value != null ? value.toString() : "");
                } else {
                    l.setText("••••••••••");
                }
                l.setFont(new Font("Consolas", Font.PLAIN, 14));
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                return l;
            }
        });

        vaultTable.getColumnModel().getColumn(4).setCellRenderer(new EyeButtonRenderer());
        vaultTable.getColumnModel().getColumn(4).setCellEditor(new EyeButtonEditor(new JCheckBox()));
        vaultTable.getColumnModel().getColumn(4).setMaxWidth(50);

        JScrollPane tableScroll = new JScrollPane(vaultTable);
        tableScroll.getViewport().setBackground(new Color(20, 25, 32));
        tableScroll.setBorder(BorderFactory.createEmptyBorder()); 

        JButton copyBtn = LoginPanel.createButton("📋 Copy Selected Password", new Color(55, 75, 95), Color.WHITE);
        copyBtn.addActionListener(e -> copySelectedPassword());

        JButton deleteSelectedBtn = LoginPanel.createButton("🗑️ Delete Selected", new Color(185, 80, 80), Color.WHITE);
        deleteSelectedBtn.addActionListener(e -> deleteSelectedPasswordFromDB());

        JButton deleteAllBtn = LoginPanel.createButton("⚠️ Clear Vault", new Color(120, 30, 30), Color.WHITE);
        deleteAllBtn.addActionListener(e -> deleteAllUserPasswordsFromDB());

        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.add(copyBtn);
        actionButtonsPanel.add(deleteSelectedBtn);
        actionButtonsPanel.add(deleteAllBtn);

        rightPanel.add(rightTitle, BorderLayout.NORTH);
        rightPanel.add(tableScroll, BorderLayout.CENTER);
        rightPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        splitContainer.add(leftPanel);
        splitContainer.add(rightPanel);

        add(splitContainer, BorderLayout.CENTER);

        generatePassword();
        loadVaultFromDB();
    }

    private JPanel createRoundedCard(Color bg) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(150, 165, 180));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setForeground(new Color(200, 215, 230));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setFocusPainted(false);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    private void generatePassword() {
        int length = (int) lengthSpinner.getValue();
        StringBuilder charPool = new StringBuilder(LOWER);

        if (useUpperCB.isSelected()) charPool.append(UPPER);
        if (useDigitsCB.isSelected()) charPool.append(DIGITS);
        if (useSymbolsCB.isSelected()) charPool.append(SYMBOLS);

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(charPool.length());
            password.append(charPool.charAt(idx));
        }

        String result = password.toString();
        generatedPasswordDisplay.setText(result);

        int score = calculateStrengthScore(result);
        strengthMeter.setStrengthLevel(score);

        if (score <= 1) {
            strengthTextLabel.setText("VERY WEAK");
            strengthTextLabel.setForeground(new Color(231, 76, 60));
        } else if (score == 2) {
            strengthTextLabel.setText("WEAK");
            strengthTextLabel.setForeground(new Color(230, 126, 34));
        } else if (score == 3) {
            strengthTextLabel.setText("MEDIUM");
            strengthTextLabel.setForeground(new Color(241, 196, 15));
        } else {
            strengthTextLabel.setText("STRONG");
            strengthTextLabel.setForeground(new Color(46, 204, 113));
        }
    }

    private int calculateStrengthScore(String pwd) {
        int score = 0;
        if (pwd.length() >= 8) score++;
        if (pwd.length() >= 12) score++;
        if (pwd.matches(".*[A-Z].*") && pwd.matches(".*[a-z].*")) score++;
        if (pwd.matches(".*[0-9].*") && pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score++;
        return Math.min(score, 4);
    }

    private void saveGeneratedPasswordToDB() {
        String appName = appNameField.getText().trim();
        String accUser = accountUsernameField.getText().trim();
        String pwd = generatedPasswordDisplay.getText().trim();

        if (appName.isEmpty() || accUser.isEmpty() || appName.equals("E.g. Google, GitHub") || accUser.equals("E.g. alan.wilson@gmail.com")) {
            JOptionPane.showMessageDialog(this, "Please enter both App Name and Account Username!", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pwd.isEmpty() || pwd.startsWith("Generated password")) {
            JOptionPane.showMessageDialog(this, "Generate a password first!", "Missing Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String checkSql = "SELECT id FROM user_saved_passwords WHERE logged_in_username = ? AND LOWER(app_name) = LOWER(?) AND LOWER(account_username) = LOWER(?)";

        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, loggedInUser);
            checkStmt.setString(2, appName);
            checkStmt.setString(3, accUser);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int existingId = rs.getInt("id");

                    int option = JOptionPane.showConfirmDialog(
                            this,
                            "An entry for '" + appName + "' with username '" + accUser + "' already exists.\nDo you want to update it with the new password?",
                            "Account Entry Already Exists",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        String updateSql = "UPDATE user_saved_passwords SET generated_password = ?, created_at = CURRENT_TIMESTAMP WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, pwd);
                            updateStmt.setInt(2, existingId);

                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                JOptionPane.showMessageDialog(this, "Existing password updated successfully for " + appName + "!", "Updated", JOptionPane.INFORMATION_MESSAGE);
                                loadVaultFromDB();
                            }
                        }
                    }
                    return;
                }
            }

            String insertSql = "INSERT INTO user_saved_passwords (logged_in_username, app_name, account_username, generated_password) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, loggedInUser);
                insertStmt.setString(2, appName);
                insertStmt.setString(3, accUser);
                insertStmt.setString(4, pwd);

                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Password successfully saved for " + appName + "!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                    loadVaultFromDB();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error while saving/updating password.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadVaultFromDB() {
        vaultTableModel.setRowCount(0);
        revealedRows.clear();
        String sql = "SELECT id, app_name, account_username, generated_password FROM user_saved_passwords WHERE logged_in_username = ? ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loggedInUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vaultTableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("app_name"),
                            rs.getString("account_username"),
                            rs.getString("generated_password"),
                            "👁"
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void copySelectedPassword() {
        int row = vaultTable.getSelectedRow();
        if (row != -1) {
            String pass = vaultTableModel.getValueAt(row, 3).toString();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(pass), null);
            JOptionPane.showMessageDialog(this, "Password copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an account row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedPasswordFromDB() {
        int row = vaultTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a password record to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int recordId = (int) vaultTableModel.getValueAt(row, 0);
        String appName = vaultTableModel.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete saved entry for '" + appName + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM user_saved_passwords WHERE id = ? AND logged_in_username = ?";
            try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, recordId);
                pstmt.setString(2, loggedInUser);

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Password deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadVaultFromDB();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error while deleting record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAllUserPasswordsFromDB() {
        if (vaultTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Vault is empty. Nothing to delete.", "Vault Empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "⚠️ Are you sure you want to delete ALL saved passwords for user '" + loggedInUser + "'?",
                "Confirm Delete All",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM user_saved_passwords WHERE logged_in_username = ?";
            try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, loggedInUser);

                int deletedCount = pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Deleted " + deletedCount + " vault record(s).", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVaultFromDB();

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error while clearing vault.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class StrengthBarPanel extends JPanel {
        private int level = 4;

        public StrengthBarPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, 10));
            setMaximumSize(new Dimension(420, 10));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        public void setStrengthLevel(int level) {
            this.level = Math.max(1, Math.min(level, 4));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int gap = 8;
            int barWidth = (w - (3 * gap)) / 4;

            Color[] colors = {
                    new Color(231, 76, 60),
                    new Color(230, 126, 34),
                    new Color(241, 196, 15),
                    new Color(46, 204, 113)
            };

            for (int i = 0; i < 4; i++) {
                int x = i * (barWidth + gap);
                if (i < level) {
                    g2.setColor(colors[level - 1]);
                } else {
                    g2.setColor(new Color(45, 52, 62));
                }
                g2.fill(new RoundRectangle2D.Float(x, 0, barWidth, h, 6, 6));
            }
            g2.dispose();
        }
    }

    private class EyeButtonRenderer extends JButton implements TableCellRenderer {
        public EyeButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setBackground(new Color(20, 25, 32));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "👁");
            if(isSelected) {
                setBackground(new Color(46, 117, 89));
            } else {
                setBackground(new Color(20, 25, 32));
            }
            return this;
        }
    }

    private class EyeButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int activeRow;

        public EyeButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setBackground(new Color(46, 117, 89));
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            activeRow = row;
            label = (value == null) ? "👁" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                if (revealedRows.contains(activeRow)) {
                    revealedRows.remove(activeRow);
                } else {
                    revealedRows.add(activeRow);
                }
                vaultTable.repaint();
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}