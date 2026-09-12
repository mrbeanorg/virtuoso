import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    
    private boolean isQuizModeActive = false;
    private String secretQuizAlgorithm = "";

    // MP3 Playback Control States
    private volatile boolean isPaused = false;
    private volatile boolean isRunning = false;
    private int currentStepPointer = 0;
    private Thread sortThread;
    private final Object pauseLock = new Object();

    private static class PyramidStep {
        int[] arraySnapshot;
        int act1;
        int act2;
        String phaseNote;

        PyramidStep(int[] arr, int a1, int a2, String note) {
            this.arraySnapshot = arr.clone();
            this.act1 = a1;
            this.act2 = a2;
            this.phaseNote = note;
        }
    }

    private final List<PyramidStep> sortingStepsHistory = new ArrayList<>();

    private JTextField inputField;
    private JTextArea logTextArea;
    private JTextPane theoryGuidePane; 
    private JButton startSortBtn;
    private JButton pausePlayBtn;
    private JButton rewindBtn;
    private JButton fastForwardBtn;
    private JButton quizModeBtn;
    private JSlider speedSlider;
    private JSpinner sizeSpinner;
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

        JLabel titleLabel = new JLabel("Advanced Sorting Tutor Workspace");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.setOpaque(false);

        JLabel userBadge = new JLabel("👤 " + currentUser.toUpperCase());
        userBadge.setForeground(COLOR_TEXT_HEADER);
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton historyBtn = LoginPanel.createButton("📜 My History", new Color(55, 75, 95), Color.WHITE);
        historyBtn.setPreferredSize(new Dimension(130, 38));
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
        sidebar.setPreferredSize(new Dimension(240, 0));

        JLabel title = new JLabel("ALGORITHM CATEGORIES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(COLOR_TEXT_MUTED);

        algorithmListModel = new DefaultListModel<>();
        algorithmListModel.addElement("Bubble Sort");
        algorithmListModel.addElement("Selection Sort");
        algorithmListModel.addElement("Insertion Sort");
        algorithmListModel.addElement("Merge Sort");
        algorithmListModel.addElement("Quick Sort");
        algorithmListModel.addElement("Heap Sort");
        algorithmListModel.addElement("Counting Sort");
        algorithmListModel.addElement("Radix Sort");
        algorithmListModel.addElement("Bucket Sort");

        algorithmList = new JList<>(algorithmListModel);
        algorithmList.setSelectedIndex(0);
        algorithmList.setOpaque(false);
        algorithmList.setBackground(new Color(0, 0, 0, 0));
        algorithmList.setForeground(COLOR_TEXT_HEADER);
        algorithmList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        algorithmList.setFixedCellHeight(32);

        algorithmList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(false);
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                JPanel card = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        if (isQuizModeActive) {
                            g2.setColor(new Color(35, 42, 52));
                        } else if (isSelected) {
                            g2.setColor(new Color(46, 117, 89));
                        } else {
                            g2.setColor(new Color(35, 42, 52));
                        }
                        
                        g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 8, 8);
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
            if (!e.getValueIsAdjusting() && !isQuizModeActive) {
                selectedAlgorithm = algorithmList.getSelectedValue();
                updateTheoryGuide(selectedAlgorithm);
                logMessage("Selected: " + selectedAlgorithm);
                selectionToggleContainer.setVisible("Selection Sort".equals(selectedAlgorithm));
            }
        });

        JScrollPane algoScroll = new JScrollPane(algorithmList);
        algoScroll.setOpaque(false);
        algoScroll.getViewport().setOpaque(false);
        algoScroll.setBorder(BorderFactory.createEmptyBorder());
        algoScroll.setPreferredSize(new Dimension(0, 160));

        theoryGuidePane = new JTextPane();
        theoryGuidePane.setEditable(false);
        theoryGuidePane.setContentType("text/html");
        theoryGuidePane.setBackground(new Color(15, 18, 22));
        theoryGuidePane.setForeground(COLOR_TEXT_HEADER);
        theoryGuidePane.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        theoryGuidePane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        JScrollPane theoryScroll = new JScrollPane(theoryGuidePane);
        theoryScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        theoryScroll.getViewport().setBackground(new Color(15, 18, 22));
        
        updateTheoryGuide("Bubble Sort"); 

        sidebar.add(title, BorderLayout.NORTH);
        sidebar.add(algoScroll, BorderLayout.NORTH);
        sidebar.add(theoryScroll, BorderLayout.CENTER);
        return sidebar;
    }

    private void updateTheoryGuide(String algo) {
        String theoryHtml = "";
        switch (algo) {
            case "Bubble Sort":
                theoryHtml = "<b>Bubble Sort (Elementary)</b><br>" +
                             "• <i>Theory:</i> Compares adjacent items and swaps them if disordered.<br>" +
                             "• <i>Animation:</i> Amber hotspot curves show items 'bubbling' to the top.";
                break;
            case "Selection Sort":
                theoryHtml = "<b>Selection Sort (Elementary)</b><br>" +
                             "• <i>Theory:</i> Scans for the absolute minimum/maximum and locks it.<br>" +
                             "• <i>Animation:</i> Blue scanning lines track boundary selections.";
                break;
            case "Insertion Sort":
                theoryHtml = "<b>Insertion Sort (Elementary)</b><br>" +
                             "• <i>Theory:</i> Builds sorted array like arranging cards in hand.<br>" +
                             "• <i>Animation:</i> Purple shift paths trace elements slotting left.";
                break;
            case "Merge Sort":
                theoryHtml = "<b>Merge Sort (Divide & Conquer)</b><br>" +
                             "• <i>Theory:</i> Recursively splits halves and merges back stably.<br>" +
                             "• <i>Animation:</i> Tree branches converge systematically.";
                break;
            case "Quick Sort":
                theoryHtml = "<b>Quick Sort (Divide & Conquer)</b><br>" +
                             "• <i>Theory:</i> Partitions data around a chosen pivot.<br>" +
                             "• <i>Animation:</i> Red cross-over paths separate partitions.";
                break;
            case "Heap Sort":
                theoryHtml = "<b>Heap Sort (Divide & Conquer)</b><br>" +
                             "• <i>Theory:</i> Uses binary heap to extract max elements.<br>" +
                             "• <i>Animation:</i> Extracts root nodes iteratively.";
                break;
            case "Counting Sort":
                theoryHtml = "<b>Counting Sort (Non-Comparison)</b><br>" +
                             "• <i>Theory:</i> Tallies key frequencies into buckets.<br>" +
                             "• <i>Animation:</i> Direct frequency mapping sequence.";
                break;
            case "Radix Sort":
                theoryHtml = "<b>Radix Sort (Non-Comparison)</b><br>" +
                             "• <i>Theory:</i> Sorts individual integer digits sequentially.<br>" +
                             "• <i>Animation:</i> Multi-pass digit distribution.";
                break;
            case "Bucket Sort":
                theoryHtml = "<b>Bucket Sort (Non-Comparison)</b><br>" +
                             "• <i>Theory:</i> Scatters items across partitioned intervals.<br>" +
                             "• <i>Animation:</i> Interval bucket distribution.";
                break;
        }
        theoryGuidePane.setText("<html><body style='color:#d0d5da; font-family:Segoe UI; font-size:11px;'>" + theoryHtml + "</body></html>");
    }

    private void styleRadioButton(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setForeground(COLOR_TEXT_HEADER);
        radio.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        row1.setOpaque(false);

        JLabel inputLabel = new JLabel("Data:");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputLabel.setForeground(COLOR_TEXT_HEADER);

        inputField = new JTextField("45, 12, 89, 23, 7, 67, 34", 18) {
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
        inputField.setPreferredSize(new Dimension(220, 38)); 
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputField.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); 

        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sizeLabel.setForeground(COLOR_TEXT_HEADER);

        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 3, 12, 1));
        sizeSpinner.setPreferredSize(new Dimension(45, 34));

        JButton randomBtn = LoginPanel.createButton("🎲 Rand", new Color(55, 75, 95), Color.WHITE);
        randomBtn.setPreferredSize(new Dimension(75, 34));
        randomBtn.addActionListener(e -> {
            int size = (int) sizeSpinner.getValue();
            Random rand = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append(rand.nextInt(90) + 10).append(i < size - 1 ? ", " : "");
            }
            inputField.setText(sb.toString());
        });

        row1.add(inputLabel);
        row1.add(inputField);
        row1.add(sizeLabel);
        row1.add(sizeSpinner);
        row1.add(randomBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        row2.setOpaque(false);

        startSortBtn = LoginPanel.createButton("▶ Run", new Color(46, 117, 89), Color.WHITE);
        startSortBtn.setPreferredSize(new Dimension(85, 36));
        startSortBtn.addActionListener(e -> prepareAndStartSort());
        
        rewindBtn = LoginPanel.createButton("⏮ 1Step", new Color(60, 70, 85), Color.WHITE);
        rewindBtn.setPreferredSize(new Dimension(80, 36));
        rewindBtn.setEnabled(false);
        rewindBtn.addActionListener(e -> stepBackward());

        pausePlayBtn = LoginPanel.createButton("⏸ Pause", new Color(211, 84, 0), Color.WHITE);
        pausePlayBtn.setPreferredSize(new Dimension(90, 36));
        pausePlayBtn.setEnabled(false);
        pausePlayBtn.addActionListener(e -> togglePausePlay());

        fastForwardBtn = LoginPanel.createButton("⏭ 1Step", new Color(60, 70, 85), Color.WHITE);
        fastForwardBtn.setPreferredSize(new Dimension(80, 36));
        fastForwardBtn.setEnabled(false);
        fastForwardBtn.addActionListener(e -> stepForward());

        quizModeBtn = LoginPanel.createButton("🎮 Quiz", new Color(142, 68, 173), Color.WHITE);
        quizModeBtn.setPreferredSize(new Dimension(80, 36));
        quizModeBtn.addActionListener(e -> startQuizMode());

        selectionToggleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        selectionToggleContainer.setOpaque(false);
        selectionToggleContainer.setVisible(false);

        ascendingRadio = new JRadioButton("Asc", true);
        descendingRadio = new JRadioButton("Desc", false);
        styleRadioButton(ascendingRadio);
        styleRadioButton(descendingRadio);

        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(ascendingRadio);
        orderGroup.add(descendingRadio);

        ascendingRadio.addActionListener(e -> { isSelectionAscending = true; });
        descendingRadio.addActionListener(e -> { isSelectionAscending = false; });
        selectionToggleContainer.add(ascendingRadio);
        selectionToggleContainer.add(descendingRadio);

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        speedLabel.setForeground(COLOR_TEXT_HEADER);

        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 1500, 600);
        speedSlider.setInverted(true); 
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(80, 30));

        row2.add(startSortBtn);
        row2.add(rewindBtn);
        row2.add(pausePlayBtn);
        row2.add(fastForwardBtn);
        row2.add(quizModeBtn);
        row2.add(selectionToggleContainer);
        row2.add(speedLabel);
        row2.add(speedSlider);

        controlPanel.add(row1);
        controlPanel.add(Box.createVerticalStrut(5));
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

        JLabel title = new JLabel("TUTOR LOG & STEPS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT_MUTED);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(COLOR_CANVAS_BG);
        logTextArea.setForeground(COLOR_ACCENT_GREEN);
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
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
            currentStepPointer = 0;
            isPaused = false;
            isRunning = true;

            pushPyramidState(-1, -1, "Start: " + selectedAlgorithm);
            logMessage("Loaded data: " + Arrays.toString(numberArray));
            logMessage("Executing: " + selectedAlgorithm + "...");

            startSortBtn.setEnabled(false);
            quizModeBtn.setEnabled(false);
            pausePlayBtn.setEnabled(true);
            rewindBtn.setEnabled(true);
            fastForwardBtn.setEnabled(true);
            pausePlayBtn.setText("⏸ Pause");

            sortThread = new Thread(this::runVisualSort);
            sortThread.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input! Enter integers separated by commas.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void togglePausePlay() {
        synchronized (pauseLock) {
            isPaused = !isPaused;
            if (isPaused) {
                pausePlayBtn.setText("▶ Resume");
                logMessage("⏸ Simulation Paused.");
            } else {
                pausePlayBtn.setText("⏸ Pause");
                logMessage("▶ Simulation Resumed.");
                pauseLock.notifyAll();
            }
        }
    }

    private void stepBackward() {
        if (!isPaused) togglePausePlay(); 
        if (currentStepPointer > 0) {
            currentStepPointer--;
            renderSpecificStep(currentStepPointer);
            logMessage("⏮ Stepped back to frame " + currentStepPointer);
        }
    }

    private void stepForward() {
        if (!isPaused) togglePausePlay(); 
        if (currentStepPointer < sortingStepsHistory.size() - 1) {
            currentStepPointer++;
            renderSpecificStep(currentStepPointer);
            logMessage("⏭ Stepped forward to frame " + currentStepPointer);
        }
    }

    private void renderSpecificStep(int index) {
        if (index >= 0 && index < sortingStepsHistory.size()) {
            currentStepPointer = index;
            visualizerCanvas.repaint();
        }
    }

    private void startQuizMode() {
        isQuizModeActive = true;
        
        Random rand = new Random();
        int size = (int) sizeSpinner.getValue();
        numberArray = new int[size];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            numberArray[i] = rand.nextInt(90) + 10; 
            sb.append(numberArray[i]).append(i < size - 1 ? ", " : "");
        }
        inputField.setText(sb.toString());

        String[] algos = {"Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Counting Sort", "Radix Sort", "Bucket Sort"};
        secretQuizAlgorithm = algos[rand.nextInt(algos.length)];
        selectedAlgorithm = secretQuizAlgorithm;
        isSelectionAscending = true; 
        
        algorithmList.clearSelection();
        algorithmList.setEnabled(false);
        inputField.setEditable(false);
        sizeSpinner.setEnabled(false);
        startSortBtn.setEnabled(false);
        quizModeBtn.setEnabled(false);
        selectionToggleContainer.setVisible(false);
        
        logTextArea.setText("🎮 QUIZ MODE ACTIVE\n");
        logTextArea.append("Analyze intermediate state...\n");
        logTextArea.append("Algorithm identity is hidden.\n");
        
        sortingStepsHistory.clear();
        currentStepPointer = 0;
        isPaused = false;
        isRunning = true;
        pushPyramidState(-1, -1, "Quiz Start");

        pausePlayBtn.setEnabled(true);
        rewindBtn.setEnabled(true);
        fastForwardBtn.setEnabled(true);

        sortThread = new Thread(this::runVisualSort);
        sortThread.start();
    }

    private void pushPyramidState(int act1, int act2, String note) {
        sortingStepsHistory.add(new PyramidStep(numberArray, act1, act2, note));
        currentStepPointer = sortingStepsHistory.size() - 1;
        visualizerCanvas.updateCanvasDimensions();
        visualizerCanvas.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = canvasScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void runVisualSort() {
        int[] originalArray = numberArray.clone();

        switch (selectedAlgorithm) {
            case "Bubble Sort":
                bubbleSortVisual();
                break;
            case "Selection Sort":
                if (isSelectionAscending) selectionSortAscendingVisual();
                else selectionSortDescendingVisual();
                break;
            case "Insertion Sort":
                insertionSortVisual();
                break;
            case "Merge Sort":
                mergeSortVisual(0, numberArray.length - 1);
                break;
            case "Quick Sort":
                quickSortVisual(0, numberArray.length - 1);
                break;
            case "Heap Sort":
                heapSortVisual();
                break;
            case "Counting Sort":
                countingSortVisual();
                break;
            case "Radix Sort":
                radixSortVisual();
                break;
            case "Bucket Sort":
                bucketSortVisual();
                break;
        }

        pushPyramidState(-1, -1, "Complete");
        isRunning = false;
        
        if (!isQuizModeActive) {
            logMessage("✅ Simulation Complete!");
            saveSortHistoryToDB(currentUser, selectedAlgorithm, Arrays.toString(originalArray), Arrays.toString(numberArray));
        }

        SwingUtilities.invokeLater(() -> {
            startSortBtn.setEnabled(true);
            quizModeBtn.setEnabled(true);
            sizeSpinner.setEnabled(true);
            pausePlayBtn.setEnabled(false);
            rewindBtn.setEnabled(false);
            fastForwardBtn.setEnabled(false);
            pausePlayBtn.setText("⏸ Pause");
            if (isQuizModeActive) {
                showQuizGuessDialog();
            }
        });
    }

    private void checkPauseState() {
        synchronized (pauseLock) {
            while (isPaused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void bubbleSortVisual() {
        int n = numberArray.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                checkPauseState();
                pushPyramidState(j, j + 1, "Bubble: Compare adjacent");
                sleep(600);

                if (numberArray[j] > numberArray[j + 1]) {
                    checkPauseState();
                    if(!isQuizModeActive) logMessage("Bubble Swap: " + numberArray[j] + " ↔ " + numberArray[j + 1]);
                    int temp = numberArray[j];
                    numberArray[j] = numberArray[j + 1];
                    numberArray[j + 1] = temp;

                    pushPyramidState(j, j + 1, "Bubble: Swap elements");
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
                checkPauseState();
                pushPyramidState(minIdx, j, "Selection: Scan for minimum");
                sleep(500);

                if (numberArray[j] < numberArray[minIdx]) {
                    minIdx = j;
                }
            }
            checkPauseState();
            if(!isQuizModeActive) logMessage("Selection Min Swap: " + numberArray[minIdx] + " ↔ " + numberArray[i]);
            int temp = numberArray[minIdx];
            numberArray[minIdx] = numberArray[i];
            numberArray[i] = temp;

            pushPyramidState(i, minIdx, "Selection: Lock minimum");
            sleep(600);
        }
    }

    private void selectionSortDescendingVisual() {
        int n = numberArray.length;
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                checkPauseState();
                pushPyramidState(maxIdx, j, "Selection: Scan for maximum");
                sleep(500);

                if (numberArray[j] > numberArray[maxIdx]) {
                    maxIdx = j;
                }
            }
            checkPauseState();
            if(!isQuizModeActive) logMessage("Selection Max Swap: " + numberArray[maxIdx] + " ↔ " + numberArray[i]);
            int temp = numberArray[maxIdx];
            numberArray[maxIdx] = numberArray[i];
            numberArray[i] = temp;

            pushPyramidState(i, maxIdx, "Selection: Lock maximum");
            sleep(600);
        }
    }

    private void insertionSortVisual() {
        int n = numberArray.length;
        for (int i = 1; i < n; ++i) {
            int j = i;
            while (j > 0 && numberArray[j - 1] > numberArray[j]) {
                checkPauseState();
                if(!isQuizModeActive) logMessage("Insertion Card Shift: " + numberArray[j] + " ↔ " + numberArray[j - 1]);
                int temp = numberArray[j];
                numberArray[j] = numberArray[j - 1];
                numberArray[j - 1] = temp;
                
                pushPyramidState(j - 1, j, "Insertion: Shift card left");
                sleep(600);
                j--;
            }
            pushPyramidState(j, j, "Insertion: Card placed"); 
            sleep(400);
        }
    }

    private void mergeSortVisual(int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortVisual(left, mid);
            mergeSortVisual(mid + 1, right);
            mergeVisual(left, mid, right);
        }
    }

    private void mergeVisual(int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right) {
            checkPauseState();
            pushPyramidState(i, j, "Merge: Compare sublists");
            sleep(500);
            if (numberArray[i] <= numberArray[j]) {
                temp[k++] = numberArray[i++];
            } else {
                temp[k++] = numberArray[j++];
            }
        }
        while (i <= mid) temp[k++] = numberArray[i++];
        while (j <= right) temp[k++] = numberArray[j++];
        
        for (i = left, k = 0; i <= right; i++, k++) {
            checkPauseState();
            numberArray[i] = temp[k];
            pushPyramidState(i, left, "Merge: Combine sublist");
            sleep(400);
        }
        if(!isQuizModeActive) logMessage("Merged subarray " + left + " to " + right);
    }

    private void quickSortVisual(int low, int high) {
        if (low < high) {
            int pi = partitionVisual(low, high);
            quickSortVisual(low, pi - 1);
            quickSortVisual(pi + 1, high);
        }
    }

    private int partitionVisual(int low, int high) {
        int pivot = numberArray[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            checkPauseState();
            pushPyramidState(j, high, "Quick: Check against pivot");
            sleep(400);
            if (numberArray[j] < pivot) {
                i++;
                int temp = numberArray[i];
                numberArray[i] = numberArray[j];
                numberArray[j] = temp;
                pushPyramidState(i, j, "Quick: Partition swap");
                sleep(400);
            }
        }
        int temp = numberArray[i + 1];
        numberArray[i + 1] = numberArray[high];
        numberArray[high] = temp;
        pushPyramidState(i + 1, high, "Quick: Place pivot");
        sleep(500);
        if(!isQuizModeActive) logMessage("Partitioned around pivot " + pivot);
        return i + 1;
    }

    private void heapSortVisual() {
        int n = numberArray.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapifyVisual(n, i);
        for (int i = n - 1; i > 0; i--) {
            checkPauseState();
            int temp = numberArray[0];
            numberArray[0] = numberArray[i];
            numberArray[i] = temp;
            pushPyramidState(0, i, "Heap: Extract root max");
            sleep(600);
            heapifyVisual(i, 0);
        }
    }

    private void heapifyVisual(int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && numberArray[l] > numberArray[largest]) largest = l;
        if (r < n && numberArray[r] > numberArray[largest]) largest = r;
        if (largest != i) {
            checkPauseState();
            int swap = numberArray[i];
            numberArray[i] = numberArray[largest];
            numberArray[largest] = swap;
            pushPyramidState(i, largest, "Heap: Re-heapify node");
            sleep(500);
            heapifyVisual(n, largest);
        }
    }

    private void countingSortVisual() {
        int max = numberArray[0];
        for (int num : numberArray) if (num > max) max = num;
        int[] count = new int[max + 1];
        for (int num : numberArray) count[num]++;
        int idx = 0;
        for (int i = 0; i <= max; i++) {
            while (count[i] > 0) {
                checkPauseState();
                numberArray[idx++] = i;
                pushPyramidState(idx - 1, idx - 1, "Counting: Tally frequency");
                sleep(400);
                count[i]--;
            }
        }
        if(!isQuizModeActive) logMessage("Counting sort complete.");
    }

    private void radixSortVisual() {
        int max = numberArray[0];
        for (int num : numberArray) if (num > max) max = num;
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countSortForRadix(exp);
        }
        if(!isQuizModeActive) logMessage("Radix sort complete.");
    }

    private void countSortForRadix(int exp) {
        int n = numberArray.length;
        int[] output = new int[n];
        int[] count = new int[10];
        Arrays.fill(count, 0);
        for (int i = 0; i < n; i++) count[(numberArray[i] / exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];
        for (int i = n - 1; i >= 0; i--) {
            output[count[(numberArray[i] / exp) % 10] - 1] = numberArray[i];
            count[(numberArray[i] / exp) % 10]--;
        }
        for (int i = 0; i < n; i++) {
            checkPauseState();
            numberArray[i] = output[i];
            pushPyramidState(i, i, "Radix: Digit pass place");
            sleep(400);
        }
    }

    private void bucketSortVisual() {
        int n = numberArray.length;
        if (n <= 0) return;
        List<Integer>[] buckets = new ArrayList[n];
        for (int i = 0; i < n; i++) buckets[i] = new ArrayList<>();
        int max = numberArray[0];
        for (int num : numberArray) if (num > max) max = num;
        max++; 
        for (int num : numberArray) {
            int bi = (n * num) / max;
            buckets[bi].add(num);
        }
        int idx = 0;
        for (int i = 0; i < n; i++) {
            java.util.Collections.sort(buckets[i]);
            for (int num : buckets[i]) {
                checkPauseState();
                numberArray[idx++] = num;
                pushPyramidState(idx - 1, idx - 1, "Bucket: Scatter & gather");
                sleep(400);
            }
        }
        if(!isQuizModeActive) logMessage("Bucket sort complete.");
    }
    
    private void showQuizGuessDialog() {
        JDialog quizDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quiz Mode Challenge", true);
        quizDialog.setSize(440, 380);
        quizDialog.setLocationRelativeTo(this);
        quizDialog.setLayout(new BorderLayout());
        
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🎮 IDENTIFY THE ALGORITHM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel promptLabel = new JLabel("Which sorting mechanism was just demonstrated?");
        promptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        promptLabel.setForeground(COLOR_TEXT_MUTED);
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] options = {"Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Counting Sort", "Radix Sort", "Bucket Sort"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        dropdown.setMaximumSize(new Dimension(280, 40));
        dropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dropdown.setBackground(new Color(20, 25, 32));
        dropdown.setForeground(Color.WHITE);
        dropdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel clueLabel = new JLabel("");
        clueLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        clueLabel.setForeground(new Color(241, 196, 15));
        clueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton clueBtn = LoginPanel.createButton("💡 Reveal Pedagogical Clue", new Color(60, 70, 85), Color.WHITE);
        clueBtn.setPreferredSize(new Dimension(280, 38));
        clueBtn.addActionListener(e -> {
            String hint = "";
            switch (secretQuizAlgorithm) {
                case "Bubble Sort": hint = "Clue: Repeatedly compares adjacent pairs and bubbles maximums."; break;
                case "Selection Sort": hint = "Clue: Scans for the absolute minimum and locks boundary elements."; break;
                case "Insertion Sort": hint = "Clue: Shifts elements left like sorting playing cards in hand."; break;
                case "Merge Sort": hint = "Clue: Recursively divides arrays into sublists and merges them."; break;
                case "Quick Sort": hint = "Clue: Partitions elements around a designated pivot value."; break;
                case "Heap Sort": hint = "Clue: Leverages a binary max-heap data structure."; break;
                case "Counting Sort": hint = "Clue: Tallies frequencies into an auxiliary occurrence array."; break;
                case "Radix Sort": hint = "Clue: Sorts numbers sequentially digit by digit from LSD to MSD."; break;
                case "Bucket Sort": hint = "Clue: Distributes elements into uniform numeric interval buckets."; break;
            }
            clueLabel.setText(hint);
        });

        JButton submitBtn = LoginPanel.createButton("Submit Answer", new Color(46, 117, 89), Color.WHITE);
        submitBtn.setPreferredSize(new Dimension(280, 40));
        
        final String[] userGuess = {null};
        submitBtn.addActionListener(e -> {
            userGuess[0] = (String) dropdown.getSelectedItem();
            quizDialog.dispose();
        });

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(promptLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(dropdown);
        card.add(Box.createVerticalStrut(15));
        card.add(clueBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(clueLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(submitBtn);

        quizDialog.add(card, BorderLayout.CENTER);
        quizDialog.getContentPane().setBackground(COLOR_MAIN_BG);
        quizDialog.setVisible(true);

        if (userGuess[0] != null) {
            boolean correct = userGuess[0].equals(secretQuizAlgorithm);
            showThemedResultDialog(correct, secretQuizAlgorithm, userGuess[0]);
            saveQuizScoreToDB(currentUser, secretQuizAlgorithm, userGuess[0], correct);
        } else {
            logTextArea.append("\n⚠️ Quiz skipped.\n");
        }
        
        isQuizModeActive = false;
        algorithmList.setEnabled(true);
        algorithmList.setSelectedIndex(0);
        inputField.setEditable(true);
        sizeSpinner.setEnabled(true);
    }

    private void showThemedResultDialog(boolean isCorrect, String actual, String guess) {
        JDialog resultDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isCorrect ? "🎉 Correct Answer!" : "❌ Incorrect Answer", true);
        resultDialog.setSize(420, 320);
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setLayout(new BorderLayout());

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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel headerLabel = new JLabel(isCorrect ? "✅ EXCELLENT WORK!" : "💪 KEEP PUSHING FORWARD!");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(isCorrect ? COLOR_ACCENT_GREEN : new Color(231, 76, 60));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String messageHtml;
        if (isCorrect) {
            messageHtml = "<html><div style='text-align:center; color:#f0f5fa; font-family:Segoe UI; width:300px;'>" +
                          "You successfully identified <b>" + actual + "</b>!<br><br>" +
                          "Your logical breakdown and pattern recognition are spot on. Keep mastering those algorithms!</div></html>";
            logTextArea.append("\n✅ Quiz Passed! Guessed: " + guess + "\n");
        } else {
            messageHtml = "<html><div style='text-align:center; color:#f0f5fa; font-family:Segoe UI; width:300px;'>" +
                          "You guessed: <b>" + guess + "</b><br>" +
                          "Actual algorithm: <b>" + actual + "</b><br><br>" +
                          "<i>Don't worry! Every mistake is a stepping stone to mastery. Review the animation steps and try again!</i></div></html>";
            logTextArea.append("\n❌ Quiz Failed. Guessed: " + guess + " | Actual: " + actual + "\n");
        }

        JLabel msgLabel = new JLabel(messageHtml);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okBtn = LoginPanel.createButton("Continue Learning", new Color(46, 117, 89), Color.WHITE);
        okBtn.setPreferredSize(new Dimension(240, 40));
        okBtn.setMaximumSize(new Dimension(240, 40));
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.addActionListener(e -> resultDialog.dispose());

        card.add(headerLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(msgLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(okBtn);

        resultDialog.add(card, BorderLayout.CENTER);
        resultDialog.getContentPane().setBackground(COLOR_MAIN_BG);
        resultDialog.setVisible(true);
    }

    private void sleep(int baseMs) {
        try {
            int currentSpeed = speedSlider.getValue(); 
            int actualSleep = (int) (baseMs * (currentSpeed / 600.0));
            Thread.sleep(actualSleep);
        } catch (InterruptedException ignored) {}
    }

    private void logMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(msg + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void showTheoryDialog() {
        JDialog theoryDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Algorithm Theory & Tutor Reference", true);
        theoryDialog.setSize(750, 600);
        theoryDialog.setLocationRelativeTo(this);
        theoryDialog.setLayout(new BorderLayout());
        theoryDialog.getContentPane().setBackground(COLOR_MAIN_BG);

        String html = "<html><body style='color:#f0f5fa; font-family:Segoe UI; padding:15px; font-size:12px;'>" +
            "<h2 style='color:#2ecc71;'>Sorting Algorithm Categories</h2>" +
            "<p>Sorting algorithms organize data into a specific order by systematically comparing and swapping elements.</p>" +
            "<h3 style='color:#3498db;'>1. Elementary Sorts (Bubble, Selection, Insertion)</h3>" +
            "<p><b>Bubble Sort:</b> Repeatedly swaps adjacent elements if they are in the wrong order ($O(n^2)$ worst case).</p>" +
            "<p><b>Selection Sort:</b> Finds the absolute minimum element and places it at the boundary ($O(n^2)$ guaranteed swaps).</p>" +
            "<p><b>Insertion Sort:</b> Builds the sorted array incrementally like sorting playing cards ($O(n)$ best case).</p>" +
            "<h3 style='color:#3498db;'>2. Efficient Divide-and-Conquer Sorts (Merge, Quick, Heap)</h3>" +
            "<p><b>Merge Sort:</b> Recursively splits arrays and merges sorted sub-arrays ($O(n \\log n)$ time, stable).</p>" +
            "<p><b>Quick Sort:</b> Partitions arrays around a selected pivot element ($O(n \\log n)$ average time).</p>" +
            "<p><b>Heap Sort:</b> Utilizes a binary heap data structure to extract maximum elements in-place ($O(n \\log n)$).</p>" +
            "<h3 style='color:#3498db;'>3. Specialized Non-Comparison Sorts (Counting, Radix, Bucket)</h3>" +
            "<p><b>Counting Sort:</b> Tallies element frequencies within a bounded integer range ($O(n + k)$).</p>" +
            "<p><b>Radix Sort:</b> Sorts data digit by digit using stable sub-sorting passes.</p>" +
            "<p><b>Bucket Sort:</b> Distributes elements into uniform buckets before sorting individually.</p>" +
            "<br><table border='1' cellspacing='0' cellpadding='6' style='border-collapse:collapse; width:100%; border-color:#303741;'>" +
            "<tr style='background-color:#1c2128;'><th align='left'>Algorithm</th><th align='left'>Best Case</th><th align='left'>Worst Case</th><th align='left'>Space</th><th align='left'>Stable?</th></tr>" +
            "<tr><td>Bubble Sort</td><td>$O(n)$</td><td>$O(n^2)$</td><td>$O(1)$</td><td>Yes</td></tr>" +
            "<tr><td>Selection Sort</td><td>$O(n^2)$</td><td>$O(n^2)$</td><td>$O(1)$</td><td>No</td></tr>" +
            "<tr><td>Insertion Sort</td><td>$O(n)$</td><td>$O(n^2)$</td><td>$O(1)$</td><td>Yes</td></tr>" +
            "<tr><td>Merge Sort</td><td>$O(n \\log n)$</td><td>$O(n \\log n)$</td><td>$O(n)$</td><td>Yes</td></tr>" +
            "<tr><td>Quick Sort</td><td>$O(n \\log n)$</td><td>$O(n^2)$</td><td>$O(\\log n)$</td><td>No</td></tr>" +
            "<tr><td>Heap Sort</td><td>$O(n \\log n)$</td><td>$O(n \\log n)$</td><td>$O(1)$</td><td>No</td></tr>" +
            "<tr><td>Counting Sort</td><td>$O(n + k)$</td><td>$O(n + k)$</td><td>$O(k)$</td><td>Yes</td></tr>" +
            "</table>" +
            "</body></html>";

        JLabel content = new JLabel(html);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(COLOR_PANEL_BG);

        theoryDialog.add(scroll, BorderLayout.CENTER);
        
        JButton closeBtn = LoginPanel.createButton("Close", new Color(55, 65, 80), Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.addActionListener(e -> theoryDialog.dispose());
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(COLOR_MAIN_BG);
        bottom.add(closeBtn);
        
        theoryDialog.add(bottom, BorderLayout.SOUTH);
        theoryDialog.setVisible(true);
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
    
    private void saveQuizScoreToDB(String user, String actual, String guess, boolean isCorrect) {
        String sql = "INSERT INTO user_quiz_history (username, actual_algorithm, user_guess, is_correct) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, actual);
            pstmt.setString(3, guess);
            pstmt.setBoolean(4, isCorrect);
            pstmt.executeUpdate();
        } catch (SQLException e) {
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

            int[][] xCenters = new int[totalSteps][];
            int[] yCenters = new int[totalSteps];

            for (int r = 0; r < totalSteps; r++) {
                PyramidStep tier = sortingStepsHistory.get(r);
                int n = tier.arraySnapshot.length;
                boolean isCurrentActiveRow = (r == currentStepPointer);
                
                int bubbleDiameter = isCurrentActiveRow ? 52 : 38;
                int elementGap = isCurrentActiveRow ? 16 : 10;
                int rowWidth = (n * bubbleDiameter) + ((n - 1) * elementGap);
                int startX = (getWidth() - rowWidth) / 2;
                
                xCenters[r] = new int[n];
                yCenters[r] = startY + (r * verticalGap) + (bubbleDiameter / 2);
                
                for (int i = 0; i < n; i++) {
                    xCenters[r][i] = startX + i * (bubbleDiameter + elementGap) + (bubbleDiameter / 2);
                }
            }

            int drawLimit = Math.min(currentStepPointer + 1, totalSteps);
            for (int r = 1; r < drawLimit; r++) {
                int[] prevArr = sortingStepsHistory.get(r - 1).arraySnapshot;
                int[] currArr = sortingStepsHistory.get(r).arraySnapshot;
                int n = prevArr.length;
                
                boolean[] used = new boolean[n];
                int[] map = new int[n];
                Arrays.fill(map, -1);
                
                for (int i = 0; i < n; i++) {
                    if (prevArr[i] == currArr[i]) {
                        map[i] = i;
                        used[i] = true;
                    }
                }
                for (int i = 0; i < n; i++) {
                    if (map[i] == -1) {
                        for (int j = 0; j < n; j++) {
                            if (!used[j] && prevArr[i] == currArr[j]) {
                                map[i] = j;
                                used[j] = true;
                                break;
                            }
                        }
                    }
                }
                for (int i = 0; i < n; i++) {
                    if (map[i] == -1) {
                        for (int j = 0; j < n; j++) {
                            if (!used[j]) {
                                map[i] = j;
                                used[j] = true;
                                break;
                            }
                        }
                        if (map[i] == -1) map[i] = i; 
                    }
                }

                for (int i = 0; i < n; i++) {
                    int target = map[i];
                    int px = xCenters[r - 1][i];
                    int py = yCenters[r - 1];
                    int cx = xCenters[r][target];
                    int cy = yCenters[r];
                    
                    boolean isSwap = (i != target);
                    
                    if (isSwap) {
                        if (selectedAlgorithm.contains("Bubble")) {
                            g2d.setColor(new Color(230, 126, 34, 220)); 
                        } else if (selectedAlgorithm.contains("Selection")) {
                            g2d.setColor(new Color(52, 152, 219, 220)); 
                        } else if (selectedAlgorithm.contains("Insertion")) {
                            g2d.setColor(new Color(155, 89, 182, 220)); 
                        } else if (selectedAlgorithm.contains("Quick")) {
                            g2d.setColor(new Color(231, 76, 60, 220));  
                        } else {
                            g2d.setColor(new Color(241, 196, 15, 200)); 
                        }
                        g2d.setStroke(new BasicStroke(3.0f));
                    } else {
                        g2d.setColor(new Color(48, 55, 65, 100)); 
                        g2d.setStroke(new BasicStroke(1.0f));
                    }
                    
                    Path2D path = new Path2D.Float();
                    path.moveTo(px, py);
                    path.curveTo(px, py + 30, cx, cy - 30, cx, cy);
                    g2d.draw(path);
                }
            }

            for (int r = 0; r < drawLimit; r++) {
                PyramidStep tier = sortingStepsHistory.get(r);
                int[] arr = tier.arraySnapshot;
                int n = arr.length;
                boolean isCurrentActiveRow = (r == currentStepPointer);
                
                int bubbleDiameter = isCurrentActiveRow ? 52 : 38;
                int yTop = startY + r * verticalGap;

                for (int i = 0; i < n; i++) {
                    int x = xCenters[r][i] - (bubbleDiameter / 2);
                    int y = yTop;

                    if (isCurrentActiveRow && (i == tier.act1 || i == tier.act2)) {
                        if (selectedAlgorithm.contains("Quick")) {
                            g2d.setColor(new Color(142, 68, 173)); 
                        } else {
                            g2d.setColor(new Color(231, 76, 60)); 
                        }
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
                
                if (isCurrentActiveRow && tier.phaseNote != null && !tier.phaseNote.isEmpty()) {
                    g2d.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    g2d.setColor(new Color(200, 215, 230));
                    int noteX = getWidth() - 160;
                    g2d.drawString("📌 " + tier.phaseNote, noteX, yTop + (bubbleDiameter / 2) + 4);
                }
            }
        }
    }
}