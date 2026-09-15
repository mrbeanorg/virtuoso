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

public class SortHistoryPanel extends JPanel {

    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_CANVAS_BG = new Color(15, 18, 22);
    private static final Color COLOR_TEXT_HEADER = new Color(240, 245, 250);
    private static final Color COLOR_TEXT_MUTED = new Color(150, 165, 180);
    private static final Color COLOR_BORDER = new Color(48, 55, 65);

    private String currentUser;
    private DefaultTableModel tableModel;

    public SortHistoryPanel(String username, Runnable onBack) {
        this.currentUser = username;
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Wrapper Card matching project theme
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

        JLabel title = new JLabel("📜 MY SORTING LOGS - " + currentUser.toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_TEXT_HEADER);

        String[] columns = {"Algorithm", "Initial Array", "Sorted Array", "Date/Time"};
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

        // Force dark theme on Table Header and cells
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

        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer();
        paddedRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        paddedRenderer.setBackground(COLOR_CANVAS_BG);
        paddedRenderer.setForeground(COLOR_TEXT_HEADER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }

        loadTableData();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_CANVAS_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);

        JButton clearHistoryBtn = LoginPanel.createButton("🗑️ Delete All My History", new Color(180, 60, 60), Color.WHITE);
        clearHistoryBtn.setPreferredSize(new Dimension(200, 38));
        clearHistoryBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete all your sort history logs?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String deleteSql = "DELETE FROM user_sort_history WHERE username = ?";
                try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
                     PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {

                    pstmt.setString(1, currentUser);
                    int deletedRows = pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Deleted " + deletedRows + " history records.", "Success", JOptionPane.INFORMATION_MESSAGE);
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

        bottomPanel.add(clearHistoryBtn);
        bottomPanel.add(backBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        String sql = "SELECT algorithm_name, input_array, sorted_array, timestamp FROM user_sort_history WHERE username = ? ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getString("algorithm_name"),
                            rs.getString("input_array"),
                            rs.getString("sorted_array"),
                            rs.getTimestamp("timestamp")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}