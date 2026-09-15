import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuizHistoryPanel extends JPanel {

    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_CANVAS_BG = new Color(15, 18, 22);
    private static final Color COLOR_TEXT_HEADER = new Color(240, 245, 250);
    private static final Color COLOR_TEXT_MUTED = new Color(150, 165, 180);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color COLOR_BORDER = new Color(48, 55, 65);

    private String currentUser;
    private DefaultTableModel tableModel;

    public QuizHistoryPanel(String username, Runnable onBack) {
        this.currentUser = username;
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("🎮 MY QUIZ PERFORMANCE - " + currentUser.toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_TEXT_HEADER);

        String[] columns = {"Actual Algorithm", "Your Guess", "Result"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);

        table.setBackground(COLOR_CANVAS_BG);
        table.setForeground(COLOR_TEXT_HEADER);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(35, 42, 52));
        header.setForeground(COLOR_TEXT_HEADER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 40));
        
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setBackground(new Color(35, 42, 52));
                lbl.setForeground(COLOR_TEXT_HEADER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        });

        DefaultTableCellRenderer resultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if ("✅ Correct".equals(value)) {
                    label.setForeground(COLOR_ACCENT_GREEN);
                } else {
                    label.setForeground(new Color(231, 76, 60));
                }
                label.setBackground(isSelected ? new Color(46, 117, 89) : COLOR_CANVAS_BG);
                return label;
            }
        };
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(COLOR_CANVAS_BG);
        centerRenderer.setForeground(COLOR_TEXT_HEADER);

        loadTableData();

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(resultRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_CANVAS_BG);

        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomWrapper.setOpaque(false);
        
        JButton clearHistoryBtn = LoginPanel.createButton("🗑️ Delete All My History", new Color(180, 60, 60), Color.WHITE);
        clearHistoryBtn.setPreferredSize(new Dimension(200, 38));
        clearHistoryBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete all your quiz history logs?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String deleteSql = "DELETE FROM user_quiz_history WHERE username = ?";
                try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
                     PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {

                    pstmt.setString(1, currentUser);
                    int deletedRows = pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Deleted " + deletedRows + " quiz history records.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTableData();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        JButton backBtn = LoginPanel.createButton("Back to Workspace", new Color(55, 65, 80), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(160, 38));
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
        
        bottomWrapper.add(clearHistoryBtn);
        bottomWrapper.add(backBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(bottomWrapper, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        String sql = "SELECT actual_algorithm, user_guess, is_correct FROM user_quiz_history WHERE username = ? ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    boolean isCorrect = rs.getBoolean("is_correct");
                    tableModel.addRow(new Object[]{
                        rs.getString("actual_algorithm"),
                        rs.getString("user_guess"),
                        isCorrect ? "✅ Correct" : "❌ Incorrect"
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}