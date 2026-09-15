import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeQuizEngine extends JDialog {

    private static final Color COLOR_MAIN_BG = new Color(20, 24, 29);
    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);

    private DefaultListModel<String> questionListModel;
    private ArrayList<String> answersList;
    
    private String targetWord;
    private JLabel qTitle;
    private JTextPane maskedPane;
    private JTextField answerField;

    public KnowledgeQuizEngine(Window parent, DefaultListModel<String> questions, ArrayList<String> answers) {
        super(parent, "Virtuoso Quiz: Knowledge Recall", Dialog.ModalityType.APPLICATION_MODAL);
        this.questionListModel = questions;
        this.answersList = answers;

        setSize(550, 350);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(COLOR_MAIN_BG);
        setLayout(new BorderLayout());

        buildUI();
        generateNextQuestion();
    }

    private void buildUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 20));
        contentPanel.setBackground(COLOR_PANEL_BG);
        contentPanel.setOpaque(true);
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Title
        qTitle = new JLabel("Loading question...");
        qTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        qTitle.setForeground(COLOR_ACCENT_GREEN);

        // Masked Text Display
        maskedPane = new JTextPane();
        maskedPane.setEditable(false);
        maskedPane.setContentType("text/html");
        maskedPane.setOpaque(false);
        maskedPane.setBackground(new Color(0,0,0,0));

        JScrollPane scrollPane = new JScrollPane(maskedPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Input Area
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        inputPanel.setOpaque(false);

        JLabel promptLabel = new JLabel("Missing Word:");
        promptLabel.setForeground(Color.WHITE);
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        answerField = new JTextField(15);
        answerField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Allow pressing "Enter" to submit
        answerField.addActionListener(e -> checkAnswer()); 

        JButton submitBtn = createButton("Submit", COLOR_ACCENT_GREEN, Color.WHITE);
        submitBtn.addActionListener(e -> checkAnswer());
        
        JButton skipBtn = createButton("Skip", new Color(100, 110, 120), Color.WHITE);
        skipBtn.addActionListener(e -> generateNextQuestion());

        inputPanel.add(promptLabel);
        inputPanel.add(answerField);
        inputPanel.add(submitBtn);
        inputPanel.add(skipBtn);

        contentPanel.add(qTitle, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void generateNextQuestion() {
        answerField.setText("");
        
        // Pick a random question from the bank
        int randomIndex = (int) (Math.random() * answersList.size());
        String questionText = questionListModel.get(randomIndex);
        String rawAnswer = answersList.get(randomIndex);

        // Clean HTML formatting to get raw text
        String plainText = rawAnswer.replaceAll("<[^>]*>", "").replace("Answer:", "").trim();
        
        // Smart word selector: Find a valid key word (longer than 4 letters) to hide
        String[] words = plainText.split("\\s+");
        List<String> validWords = new ArrayList<>();
        for (String w : words) {
            String cleanWord = w.replaceAll("[^a-zA-Z]", "");
            if (cleanWord.length() > 4) validWords.add(cleanWord);
        }

        if (validWords.isEmpty()) {
            // If the text is too short, just recursively try another one
            generateNextQuestion();
            return;
        }

        // Select a random valid word to blank out
        targetWord = validWords.get((int) (Math.random() * validWords.size()));
        
        // Replace the target word with a blank line in the display text
        String maskedText = plainText.replaceAll("(?i)\\b" + targetWord + "\\b", "<b><span style='color:#e74c3c;'>[ _________ ]</span></b>");

        qTitle.setText("<html>" + questionText + "</html>");
        maskedPane.setText("<html><body style='color:#f0f5fa; font-family:Segoe UI; font-size:15px; line-height: 1.6;'>" + maskedText + "</body></html>");
        
        // Request focus back to the text field for fast typing
        SwingUtilities.invokeLater(() -> answerField.requestFocusInWindow());
    }

    private void checkAnswer() {
        String userGuess = answerField.getText().trim();
        
        if (userGuess.isEmpty()) {
            return;
        }

        if (userGuess.equalsIgnoreCase(targetWord)) {
            JOptionPane.showMessageDialog(this, "Correct! Excellent recall.", "Tier 1 Passed", JOptionPane.INFORMATION_MESSAGE);
            generateNextQuestion(); // Automatically load the next challenge
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect. The missing word was: " + targetWord.toUpperCase(), "Keep Studying", JOptionPane.ERROR_MESSAGE);
            answerField.setText("");
            answerField.requestFocusInWindow();
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}