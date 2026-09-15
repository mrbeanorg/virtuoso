import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

// Import Apache PDFBox classes for reading PDFs
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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
    private ArrayList<Integer> dbIds; // Tracks the MySQL Primary Keys
    private JList<String> questionList;

    public ExamPrepPanel(String activeUser, Runnable onBackToMenu) {
        this.currentUser = activeUser;
        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_MAIN_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        questionListModel = new DefaultListModel<>();
        answersList = new ArrayList<>();
        dbIds = new ArrayList<>();
        
        loadQuestionsFromDB(); // Automatically loads user's saved data

        add(createHeaderPanel(onBackToMenu), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private void loadQuestionsFromDB() {
        String sql = "SELECT id, question, answer FROM user_knowledge_bank WHERE username = ? ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    dbIds.add(rs.getInt("id"));
                    questionListModel.addElement(rs.getString("question"));
                    answersList.add(rs.getString("answer"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load knowledge bank. The panel will remain blank until new data is saved.");
        }
    }

    private void saveQuestionToDB(String question, String answer, String sourceFile) {
        String sql = "INSERT INTO user_knowledge_bank (username, question, answer, source_file) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, currentUser);
            pstmt.setString(2, question);
            pstmt.setString(3, answer);
            pstmt.setString(4, sourceFile);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    dbIds.add(rs.getInt(1)); // Add the new auto-generated ID to our tracking list
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dbIds.add(-1); // Fallback so indexes stay aligned if saving fails
        }
    }

    private void deleteQuestionFromDB(int id) {
        if (id == -1) return;
        String sql = "DELETE FROM user_knowledge_bank WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DatabaseApp.DB_URL, DatabaseApp.DB_USER, DatabaseApp.DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

        JButton uploadBtn = createButton("📁 Upload Notes", new Color(55, 75, 95), Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(140, 38));
        uploadBtn.addActionListener(e -> handleDocumentUpload());

        // --- NEW QUIZ BUTTON ---
        JButton quizBtn = createButton("🧠 Start Quiz", new Color(155, 89, 182), Color.WHITE);
        quizBtn.setPreferredSize(new Dimension(130, 38));
        quizBtn.addActionListener(e -> {
            if (questionListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your knowledge bank is empty! Upload some notes first.", "Cannot Start Quiz", JOptionPane.WARNING_MESSAGE);
            } else {
                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                // Launch our new decoupled KnowledgeQuizEngine class
                new KnowledgeQuizEngine(parentWindow, questionListModel, answersList).setVisible(true);
            }
        });

        JButton backBtn = createButton("Dashboard", new Color(55, 65, 80), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(110, 38));
        backBtn.addActionListener(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        rightControls.add(userBadge);
        rightControls.add(uploadBtn);
        rightControls.add(quizBtn); // Add the quiz button to the UI
        rightControls.add(backBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private void handleDocumentUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF & Text Documents (*.pdf, *.txt)", "pdf", "txt"));
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName().toLowerCase();

            try {
                int addedCount = 0;
                String extractedText = "";
                
                if (fileName.endsWith(".pdf")) {
                    try (PDDocument document = Loader.loadPDF(selectedFile)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        extractedText = stripper.getText(document);
                    }
                } else if (fileName.endsWith(".txt")) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    }
                    extractedText = sb.toString();
                }

                addedCount = parseAndStoreContent(extractedText, selectedFile.getName());

                JOptionPane.showMessageDialog(this, "Successfully extracted and permanently saved " + addedCount + " items!", "Import Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Automatically select the newly imported item
                if (questionListModel.getSize() > 0) {
                    questionList.setSelectedIndex(questionListModel.getSize() - 1);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading document. Make sure the file isn't corrupted.", "Import Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private int parseAndStoreContent(String fullText, String fileName) {
        int count = 0;
        String[] lines = fullText.split("\\r?\\n");
        String currentQ = null;
        StringBuilder currentA = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            if (line.matches("(?i)^(q|question|q\\d+)[^\\w\\n]*\\s*(.*)")) {
                if (currentQ != null) {
                    String formattedAnswer = "<b>Answer:</b><br><br>" + (currentA.length() > 0 ? currentA.toString().trim() : "<i>No answer text found.</i>");
                    saveQuestionToDB(currentQ, formattedAnswer, fileName);
                    questionListModel.addElement(currentQ);
                    answersList.add(formattedAnswer);
                    count++;
                    currentA.setLength(0);
                }
                currentQ = line.replaceFirst("(?i)^(q|question|q\\d+)[^\\w\\n]*\\s*", "").trim();
                if (currentQ.isEmpty()) currentQ = "Extracted Question";
            } 
            else if (line.matches("(?i)^(a|answer|ans|a\\d+)[^\\w\\n]*\\s*(.*)")) {
                String ansText = line.replaceFirst("(?i)^(a|answer|ans|a\\d+)[^\\w\\n]*\\s*", "").trim();
                currentA.append(ansText).append("<br>");
            } 
            else if (currentQ != null) {
                currentA.append(line).append("<br>");
            }
        }

        if (currentQ != null) {
            String formattedAnswer = "<b>Answer:</b><br><br>" + (currentA.length() > 0 ? currentA.toString().trim() : "<i>No answer text found.</i>");
            saveQuestionToDB(currentQ, formattedAnswer, fileName);
            questionListModel.addElement(currentQ);
            answersList.add(formattedAnswer);
            count++;
        }

        if (count == 0 && !fullText.trim().isEmpty()) {
            String qTitle = "📄 Study Notes: " + fileName;
            String formattedText = "<b>Document Contents:</b><br><br>" + fullText.replace("\n", "<br>");
            
            saveQuestionToDB(qTitle, formattedText, fileName);
            questionListModel.addElement(qTitle);
            answersList.add(formattedText);
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
        leftPanel.setPreferredSize(new Dimension(340, 0));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel listTitle = new JLabel("MY KNOWLEDGE BANK");
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
                if (text.length() > 40) text = text.substring(0, 37) + "...";
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
                    answerDisplayPane.setText("<html><body style='color:#f0f5fa; font-family:Segoe UI; font-size:14px; line-height: 1.6; padding-right:15px;'>" + answersList.get(idx) + "</body></html>");
                    answerDisplayPane.setCaretPosition(0); 
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

        JPanel rightHeaderPanel = new JPanel(new BorderLayout());
        rightHeaderPanel.setOpaque(false);

        currentQuestionTitle = new JLabel("Select a question to review...");
        currentQuestionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        currentQuestionTitle.setForeground(COLOR_ACCENT_GREEN);

        JButton deleteBtn = createButton("🗑️ Delete Selected", new Color(180, 60, 60), Color.WHITE);
        deleteBtn.setPreferredSize(new Dimension(160, 32));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> {
            int selectedIndex = questionList.getSelectedIndex();
            if (selectedIndex != -1) {
                // Delete from MySQL Database
                int dbId = dbIds.get(selectedIndex);
                deleteQuestionFromDB(dbId);
                
                // Remove from UI Lists
                questionListModel.remove(selectedIndex);
                answersList.remove(selectedIndex);
                dbIds.remove(selectedIndex);
                
                if (questionListModel.isEmpty()) {
                    currentQuestionTitle.setText("Knowledge Bank Empty");
                    answerDisplayPane.setText("");
                } else {
                    int nextIndex = (selectedIndex >= questionListModel.getSize()) ? questionListModel.getSize() - 1 : selectedIndex;
                    questionList.setSelectedIndex(nextIndex);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        rightHeaderPanel.add(currentQuestionTitle, BorderLayout.CENTER);
        rightHeaderPanel.add(deleteBtn, BorderLayout.EAST);

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
        answerScroll.getVerticalScrollBar().setUnitIncrement(16);

        rightPanel.add(rightHeaderPanel, BorderLayout.NORTH);
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