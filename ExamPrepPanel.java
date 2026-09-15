import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

// Import Apache PDFBox classes for reading PDFs
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;

public class ExamPrepPanel extends JPanel {

    private static final Color COLOR_MAIN_BG = new Color(20, 24, 29);
    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_TEXT_HEADER = new Color(240, 245, 250);
    private static final Color COLOR_TEXT_MUTED = new Color(150, 165, 180);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color COLOR_BORDER = new Color(48, 55, 65);

    private String currentUser;
    private JTextPane answerDisplayPane;
    private JLabel currentQuestionTitle;
    
    private DefaultListModel<String> questionListModel;
    private ArrayList<String> answersList;
    private JList<String> questionList;

    public ExamPrepPanel(String activeUser, Runnable onBackToMenu) {
        this.currentUser = activeUser;
        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_MAIN_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        questionListModel = new DefaultListModel<>();
        answersList = new ArrayList<>();
        loadDefaultQuestions();

        add(createHeaderPanel(onBackToMenu), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private void loadDefaultQuestions() {
        String[] defaultQuestions = {
            "Q1: Define a stable sorting algorithm. Give an example.",
            "Q2: Differentiate between internal and external sorting.",
            "Q3: Outline the time complexities of Quick Sort."
        };

        String[] defaultAnswers = {
            "<b>Answer:</b><br><br>A sorting algorithm is considered <b>stable</b> if it preserves the relative order of equal elements in the sorted output.",
            "<b>Answer:</b><br><br><b>Internal Sorting:</b> All data fits in RAM.<br><b>External Sorting:</b> Uses auxiliary storage for massive datasets.",
            "<b>Answer:</b><br><br>Quick Sort Complexities:<br>• Best/Average: O(n log n)<br>• Worst Case: O(n²)"
        };

        for (int i = 0; i < defaultQuestions.length; i++) {
            questionListModel.addElement(defaultQuestions[i]);
            answersList.add(defaultAnswers[i]);
        }
    }

    private JPanel createHeaderPanel(Runnable onBackToMenu) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 45));

        JLabel titleLabel = new JLabel("Academic Prep & PDF Knowledge Extractor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.setOpaque(false);

        JLabel userBadge = new JLabel("👤 " + (currentUser != null ? currentUser.toUpperCase() : "STUDENT"));
        userBadge.setForeground(COLOR_TEXT_HEADER);
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton uploadBtn = createButton("📁 Upload PDF / Notes", new Color(55, 75, 95), Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(170, 38));
        uploadBtn.addActionListener(e -> handleDocumentUpload());

        JButton backBtn = createButton("Dashboard", new Color(55, 65, 80), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(110, 38));
        backBtn.addActionListener(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        rightControls.add(userBadge);
        rightControls.add(uploadBtn);
        rightControls.add(backBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private void handleDocumentUpload() {
        JFileChooser fileChooser = new JFileChooser();
        // Allow user to select Text or PDF documents
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF & Text Documents (*.pdf, *.txt)", "pdf", "txt"));
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName().toLowerCase();

            try {
                int addedCount = 0;
                
                if (fileName.endsWith(".pdf")) {
                    // Extract text from PDF using Apache PDFBox
                    try (PDDocument document = Loader.loadPDF(selectedFile)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        String extractedText = stripper.getText(document);
                        addedCount = parseAndStoreContent(extractedText);
                    }
                } else if (fileName.endsWith(".txt")) {
                    // Read line by line from text file
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    }
                    addedCount = parseAndStoreContent(sb.toString());
                }

                JOptionPane.showMessageDialog(this, "Successfully extracted and imported " + addedCount + " questions from document!", "Import Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading document. Ensure the file contains structured 'Q:' and 'A:' tags.", "Import Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private int parseAndStoreContent(String fullText) {
        int count = 0;
        String[] lines = fullText.split("\\r?\\n");
        String currentQ = null;
        StringBuilder currentA = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Q:")) {
                if (currentQ != null) {
                    questionListModel.addElement(currentQ);
                    answersList.add("<b>Answer:</b><br><br>" + currentA.toString().trim());
                    count++;
                    currentA.setLength(0);
                }
                currentQ = line.substring(2).trim();
            } else if (line.startsWith("A:")) {
                currentA.append(line.substring(2).trim()).append("<br>");
            } else if (currentQ != null && !line.isEmpty()) {
                currentA.append(line).append("<br>");
            }
        }

        // Add final queued question
        if (currentQ != null) {
            questionListModel.addElement(currentQ);
            answersList.add("<b>Answer:</b><br><br>" + currentA.toString().trim());
            count++;
        }
        return count;
    }

    private JPanel createMainContent() {
        JPanel splitContainer = new JPanel(new BorderLayout(25, 0));
        splitContainer.setOpaque(false);

        // --- LEFT SIDEBAR: Question List ---
        JPanel leftPanel = createRoundedCard();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(320, 0));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel listTitle = new JLabel("EXTRACTED KNOWLEDGE BANK");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        listTitle.setForeground(COLOR_TEXT_MUTED);
        listTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        questionList = new JList<>(questionListModel);
        questionList.setOpaque(false);
        questionList.setBackground(new Color(0, 0, 0, 0));
        questionList.setForeground(COLOR_TEXT_HEADER);
        questionList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        questionList.setFixedCellHeight(45);

        questionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(false);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                
                String text = value.toString();
                if (text.length() > 38) text = text.substring(0, 35) + "...";
                label.setText(text);

                JPanel card = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isSelected ? new Color(46, 117, 89) : new Color(35, 42, 52));
                        g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 10, 10);
                        g2.dispose();
                    }
                };
                card.setOpaque(false);
                label.setForeground(Color.WHITE);
                card.add(label, BorderLayout.CENTER);
                return card;
            }
        });

        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = questionList.getSelectedIndex();
                if (idx != -1 && idx < answersList.size()) {
                    currentQuestionTitle.setText(questionListModel.getElementAt(idx));
                    answerDisplayPane.setText("<html><body style='color:#f0f5fa; font-family:Segoe UI; font-size:14px; line-height: 1.6;'>" + answersList.get(idx) + "</body></html>");
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(questionList);
        listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);
        listScroll.setBorder(BorderFactory.createEmptyBorder());

        leftPanel.add(listTitle, BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        // --- RIGHT PANEL: Answer Display ---
        JPanel rightPanel = createRoundedCard();
        rightPanel.setLayout(new BorderLayout(0, 20));
        rightPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        currentQuestionTitle = new JLabel("Select an extracted question...");
        currentQuestionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        currentQuestionTitle.setForeground(COLOR_ACCENT_GREEN);

        answerDisplayPane = new JTextPane();
        answerDisplayPane.setEditable(false);
        answerDisplayPane.setContentType("text/html");
        answerDisplayPane.setOpaque(false);
        answerDisplayPane.setBackground(new Color(0,0,0,0));
        answerDisplayPane.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane answerScroll = new JScrollPane(answerDisplayPane);
        answerScroll.setOpaque(false);
        answerScroll.getViewport().setOpaque(false);
        answerScroll.setBorder(BorderFactory.createEmptyBorder());

        rightPanel.add(currentQuestionTitle, BorderLayout.NORTH);
        rightPanel.add(answerScroll, BorderLayout.CENTER);

        splitContainer.add(leftPanel, BorderLayout.WEST);
        splitContainer.add(rightPanel, BorderLayout.CENTER);

        if (!questionListModel.isEmpty()) {
            questionList.setSelectedIndex(0);
        }

        return splitContainer;
    }

    private JPanel createRoundedCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}