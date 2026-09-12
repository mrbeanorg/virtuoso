import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlgorithmVirtuosoPanel extends JPanel {

    private static final Color COLOR_MAIN_BG = new Color(20, 24, 29);
    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_CANVAS_BG = new Color(15, 18, 22);
    private static final Color COLOR_TEXT_HEADER = new Color(240, 245, 250);
    private static final Color COLOR_TEXT_MUTED = new Color(150, 165, 180);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color COLOR_BORDER = new Color(48, 55, 65);

    private String currentUser = "alan";
    private String selectedAlgorithm = "Bubble Sort";
    private boolean isSelectionAscending = true;
    private int[] numberArray = new int[]{};

    private static class PyramidStep {
        int[] arraySnapshot;
        int act1;
        int act2;

        PyramidStep(int[] arr, int a1, int a2) {
            this.arraySnapshot = arr.clone();
            this.act1 = a1;
            this.act2 = a2;
        }
    }

    private final List<PyramidStep> sortingStepsHistory = new ArrayList<>();

    private JTextField inputField;
    private JTextArea logTextArea;
    private JButton startSortBtn;
    private BubbleVisualizerPanel visualizerCanvas;
    private JScrollPane canvasScrollPane;
    private DefaultListModel<String> algorithmListModel;
    private JList<String> algorithmList;

    private JPanel selectionToggleContainer;
    private JRadioButton ascendingRadio;
    private JRadioButton descendingRadio;

    public AlgorithmVirtuosoPanel(String loggedInUser, Runnable onBackToMenu) {
        if (loggedInUser != null && !loggedInUser.trim().isEmpty()) {
            this.currentUser = loggedInUser;
        }

        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_MAIN_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(onBackToMenu), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);

        contentPanel.add(createLeftSidebar(), BorderLayout.WEST);
        contentPanel.add(createCenterVisualizer(), BorderLayout.CENTER);
        contentPanel.add(createRightLogPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(Runnable onBackToMenu) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 45));

        JLabel titleLabel = new JLabel("Algorithm Virtuoso Workspace");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.setOpaque(false);

        JLabel userBadge = new JLabel("👤 " + currentUser.toUpperCase());
        userBadge.setForeground(COLOR_TEXT_HEADER);
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton historyBtn = LoginPanel.createButton("📜 My Sort History", new Color(55, 75, 95), Color.WHITE);
        historyBtn.setPreferredSize(new Dimension(150, 38));
        historyBtn.addActionListener(e -> showHistoryDialog());

        JButton backBtn = LoginPanel.createButton("Dashboard", new Color(55, 65, 80), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(110, 38));
        backBtn.addActionListener(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        rightControls.add(userBadge);
        rightControls.add(historyBtn);
        rightControls.add(backBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private JPanel createRoundedCard() {
        JPanel panel = new JPanel() {
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
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createLeftSidebar() {
        JPanel sidebar = createRoundedCard();
        sidebar.setLayout(new BorderLayout(10, 15));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel title = new JLabel("SELECT ALGORITHM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT_MUTED);

        algorithmListModel = new DefaultListModel<>();
        algorithmListModel.addElement("Bubble Sort");
        algorithmListModel.addElement("Selection Sort");
        algorithmListModel.addElement("Insertion Sort");

        algorithmList = new JList<>(algorithmListModel);
        algorithmList.setSelectedIndex(0);
        algorithmList.setOpaque(false);
        algorithmList.setBackground(new Color(0, 0, 0, 0));
        algorithmList.setForeground(COLOR_TEXT_HEADER);
        algorithmList.setFont(new Font("Segoe UI", Font.BOLD, 14));
        algorithmList.setFixedCellHeight(48);

        algorithmList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(false);
                label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));

                JPanel card = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (isSelected) {
                            g2.setColor(new Color(46, 117, 89));
                        } else {
                            g2.setColor(new Color(35, 42, 52));
                        }
                        g2.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 12, 12);
                        g2.dispose();
                    }
                };
                card.setOpaque(false);
                label.setForeground(Color.WHITE);
                card.add(label, BorderLayout.CENTER);
                return card;
            }
        });

        algorithmList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedAlgorithm = algorithmList.getSelectedValue();
                logMessage("Switched to: " + selectedAlgorithm);
                selectionToggleContainer.setVisible("Selection Sort".equals(selectedAlgorithm));
            }
        });

        sidebar.add(title, BorderLayout.NORTH);
        sidebar.add(algorithmList, BorderLayout.CENTER);
        return sidebar;
    }

    private void styleRadioButton(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setForeground(COLOR_TEXT_HEADER);
        radio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radio.setFocusPainted(false);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel createCenterVisualizer() {
        JPanel centerPanel = createRoundedCard();
        centerPanel.setLayout(new BorderLayout(15, 15));
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        row1.setOpaque(false);

        JLabel inputLabel = new JLabel("Enter Numbers:");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputLabel.setForeground(COLOR_TEXT_HEADER);

        inputField = new JTextField("45, 12, 89, 23, 7, 67, 34", 26) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 25, 32));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? new Color(46, 117, 89) : COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        inputField.setOpaque(false);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        row1.add(inputLabel);
        row1.add(inputField);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        row2.setOpaque(false);

        startSortBtn = LoginPanel.createButton("▷ Start Visual Sort", new Color(46, 117, 89), Color.WHITE);
        startSortBtn.setPreferredSize(new Dimension(180, 40));
        startSortBtn.addActionListener(e -> prepareAndStartSort());

        selectionToggleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectionToggleContainer.setOpaque(false);
        selectionToggleContainer.setVisible(false);

        ascendingRadio = new JRadioButton("Ascending", true);
        descendingRadio = new JRadioButton("Descending", false);

        styleRadioButton(ascendingRadio);
        styleRadioButton(descendingRadio);

        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(ascendingRadio);
        orderGroup.add(descendingRadio);

        ascendingRadio.addActionListener(e -> {
            isSelectionAscending = true;
            logMessage("Selection Sort Order: ASCENDING");
        });

        descendingRadio.addActionListener(e -> {
            isSelectionAscending = false;
            logMessage("Selection Sort Order: DESCENDING");
        });

        selectionToggleContainer.add(ascendingRadio);
        selectionToggleContainer.add(descendingRadio);

        row2.add(startSortBtn);
        row2.add(selectionToggleContainer);

        controlPanel.add(row1);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(row2);

        visualizerCanvas = new BubbleVisualizerPanel();

        canvasScrollPane = new JScrollPane(visualizerCanvas);
        canvasScrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        canvasScrollPane.getViewport().setBackground(COLOR_CANVAS_BG);
        canvasScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(controlPanel, BorderLayout.NORTH);
        centerPanel.add(canvasScrollPane, BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createRightLogPanel() {
        JPanel rightPanel = createRoundedCard();
        rightPanel.setLayout(new BorderLayout(10, 15));
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        JLabel title = new JLabel("EXECUTION LOG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT_MUTED);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(COLOR_CANVAS_BG);
        logTextArea.setForeground(COLOR_ACCENT_GREEN);
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logTextArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_CANVAS_BG);

        rightPanel.add(title, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        return rightPanel;
    }

    private void prepareAndStartSort() {
        String rawInput = inputField.getText().trim();

        if (rawInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter numbers to sort.", "Input Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String[] tokens = rawInput.split("[,\\s]+");
            numberArray = new int[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                numberArray[i] = Integer.parseInt(tokens[i].trim());
            }

            logTextArea.setText("");
            sortingStepsHistory.clear();

            pushPyramidState(-1, -1);
            logMessage("Loaded numbers: " + Arrays.toString(numberArray));
            logMessage("Running " + selectedAlgorithm + ("Selection Sort".equals(selectedAlgorithm) ? (isSelectionAscending ? " [Ascending]" : " [Descending]") : "") + "...");

            startSortBtn.setEnabled(false);
            new Thread(this::runVisualSort).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input! Enter integers separated by commas or spaces.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pushPyramidState(int act1, int act2) {
        sortingStepsHistory.add(new PyramidStep(numberArray, act1, act2));
        visualizerCanvas.updateCanvasDimensions();
        visualizerCanvas.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = canvasScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void runVisualSort() {
        int[] originalArray = numberArray.clone();

        if ("Bubble Sort".equals(selectedAlgorithm)) {
            bubbleSortVisual();
        } else if ("Selection Sort".equals(selectedAlgorithm)) {
            if (isSelectionAscending) {
                selectionSortAscendingVisual();
            } else {
                selectionSortDescendingVisual();
            }
        } else if ("Insertion Sort".equals(selectedAlgorithm)) {
            insertionSortVisual();
        }

        pushPyramidState(-1, -1);
        logMessage("✅ Sorting Complete!");

        String algoLabel = selectedAlgorithm + ("Selection Sort".equals(selectedAlgorithm) ? (isSelectionAscending ? " (Ascending)" : " (Descending)") : "");
        saveSortHistoryToDB(currentUser, algoLabel, Arrays.toString(originalArray), Arrays.toString(numberArray));

        SwingUtilities.invokeLater(() -> startSortBtn.setEnabled(true));
    }

    private void bubbleSortVisual() {
        int n = numberArray.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                pushPyramidState(j, j + 1);
                sleep(600);

                if (numberArray[j] > numberArray[j + 1]) {
                    logMessage("Swap: " + numberArray[j] + " ↔ " + numberArray[j + 1]);
                    int temp = numberArray[j];
                    numberArray[j] = numberArray[j + 1];
                    numberArray[j + 1] = temp;

                    pushPyramidState(j, j + 1);
                    sleep(600);
                }
            }
        }
    }

    private void selectionSortAscendingVisual() {
        int n = numberArray.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                pushPyramidState(minIdx, j);
                sleep(500);

                if (numberArray[j] < numberArray[minIdx]) {
                    minIdx = j;
                }
            }
            logMessage("Swap min: " + numberArray[minIdx] + " ↔ " + numberArray[i]);
            int temp = numberArray[minIdx];
            numberArray[minIdx] = numberArray[i];
            numberArray[i] = temp;

            pushPyramidState(i, minIdx);
            sleep(600);
        }
    }

    private void selectionSortDescendingVisual() {
        int n = numberArray.length;
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                pushPyramidState(maxIdx, j);
                sleep(500);

                if (numberArray[j] > numberArray[maxIdx]) {
                    maxIdx = j;
                }
            }
            logMessage("Swap max: " + numberArray[maxIdx] + " ↔ " + numberArray[i]);
            int temp = numberArray[maxIdx];
            numberArray[maxIdx] = numberArray[i];
            numberArray[i] = temp;

            pushPyramidState(i, maxIdx);
            sleep(600);
        }
    }

    private void insertionSortVisual() {
        int n = numberArray.length;
        for (int i = 1; i < n; ++i) {
            int key = numberArray[i];
            int j = i - 1;

            while (j >= 0 && numberArray[j] > key) {
                logMessage("Shift: " + numberArray[j] + " to pos " + (j + 1));
                numberArray[j + 1] = numberArray[j];
                pushPyramidState(j, j + 1);
                sleep(600);
                j = j - 1;
            }
            numberArray[j + 1] = key;
            pushPyramidState(j + 1, i);
            sleep(600);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    private void logMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(msg + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void saveSortHistoryToDB(String user, String algo, String inputStr, String sortedStr) {
        String sql = "INSERT INTO user_sort_history (username, algorithm_name, input_array, sorted_array) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, algo);
            pstmt.setString(3, inputStr);
            pstmt.setString(4, sortedStr);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logMessage("💾 Saved to DB for user: " + user);
            }
        } catch (SQLException e) {
            logMessage("❌ Error saving to DB!");
            e.printStackTrace();
        }
    }

    private void showHistoryDialog() {
        JDialog historyDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sorting History - " + currentUser, true);
        historyDialog.setSize(750, 500);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());
        historyDialog.getContentPane().setBackground(COLOR_MAIN_BG);

        String[] columns = {"Algorithm", "Initial Array", "Sorted Array", "Date/Time"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        table.setBackground(COLOR_PANEL_BG);
        table.setForeground(COLOR_TEXT_HEADER);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(46, 117, 89));
        table.setSelectionForeground(Color.WHITE);

        table.getTableHeader().setBackground(new Color(35, 42, 52));
        table.getTableHeader().setForeground(COLOR_TEXT_HEADER);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer();
        paddedRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }

        Runnable loadTableData = () -> {
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
        };

        loadTableData.run();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_PANEL_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(COLOR_MAIN_BG);

        JButton clearHistoryBtn = LoginPanel.createButton("🗑️ Delete All My History", new Color(180, 60, 60), Color.WHITE);
        clearHistoryBtn.setPreferredSize(new Dimension(200, 38));
        clearHistoryBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    historyDialog,
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

                    JOptionPane.showMessageDialog(historyDialog, "Deleted " + deletedRows + " history records.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTableData.run();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(historyDialog, "Error deleting history records.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bottomPanel.add(clearHistoryBtn);

        historyDialog.add(scrollPane, BorderLayout.CENTER);
        historyDialog.add(bottomPanel, BorderLayout.SOUTH);
        historyDialog.setVisible(true);
    }

    private class BubbleVisualizerPanel extends JPanel {
        public BubbleVisualizerPanel() {
            setBackground(COLOR_CANVAS_BG);
        }

        public void updateCanvasDimensions() {
            int totalSteps = sortingStepsHistory.size();
            int verticalGap = 75; 
            int requiredHeight = Math.max(420, (totalSteps + 1) * verticalGap + 50);

            setPreferredSize(new Dimension(600, requiredHeight));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (sortingStepsHistory.isEmpty()) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int totalSteps = sortingStepsHistory.size();
            int startY = 40;
            int verticalGap = 75;

            for (int rowIndex = 0; rowIndex < totalSteps; rowIndex++) {
                PyramidStep tier = sortingStepsHistory.get(rowIndex);
                int[] arr = tier.arraySnapshot;

                boolean isCurrentActiveRow = (rowIndex == totalSteps - 1);

                int bubbleDiameter = isCurrentActiveRow ? 52 : 38;
                int elementGap = isCurrentActiveRow ? 16 : 10;

                int rowWidth = (arr.length * bubbleDiameter) + ((arr.length - 1) * elementGap);
                int startX = (getWidth() - rowWidth) / 2;

                for (int i = 0; i < arr.length; i++) {
                    int x = startX + i * (bubbleDiameter + elementGap);
                    int y = startY;

                    if (isCurrentActiveRow && (i == tier.act1 || i == tier.act2)) {
                        g2d.setColor(new Color(231, 76, 60)); 
                    } else if (isCurrentActiveRow) {
                        g2d.setColor(new Color(46, 117, 89)); 
                    } else {
                        g2d.setColor(new Color(35, 42, 52)); 
                    }

                    g2d.fillOval(x, y, bubbleDiameter, bubbleDiameter);

                    g2d.setColor(isCurrentActiveRow ? Color.WHITE : COLOR_BORDER);
                    g2d.setStroke(new BasicStroke(isCurrentActiveRow ? 2 : 1));
                    g2d.drawOval(x, y, bubbleDiameter, bubbleDiameter);

                    String numText = String.valueOf(arr[i]);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, isCurrentActiveRow ? 16 : 12));
                    g2d.setColor(isCurrentActiveRow ? Color.WHITE : COLOR_TEXT_MUTED);

                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (bubbleDiameter - fm.stringWidth(numText)) / 2;
                    int textY = y + (bubbleDiameter + fm.getAscent() - fm.getDescent()) / 2;

                    g2d.drawString(numText, textX, textY);
                }
                startY += verticalGap;
            }
        }
    }
}