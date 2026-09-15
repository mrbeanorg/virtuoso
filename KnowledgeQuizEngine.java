import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KnowledgeQuizEngine extends JDialog {

    private static final Color COLOR_MAIN_BG = new Color(20, 24, 29);
    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color COLOR_OPTION_BG = new Color(45, 55, 65);

    private DefaultListModel<String> questionListModel;
    private ArrayList<String> answersList;
    
    private String targetWord;
    private JLabel qTitle;
    private JTextPane maskedPane;
    
    private JButton[] optionButtons;

    public KnowledgeQuizEngine(Window parent, DefaultListModel<String> questions, ArrayList<String> answers) {
        super(parent, "Virtuoso MCQ Quiz Mode", Dialog.ModalityType.APPLICATION_MODAL);
        this.questionListModel = questions;
        this.answersList = answers;

        setSize(600, 420);
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

        // MCQ Options Grid (2x2)
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        optionsPanel.setOpaque(false);
        
        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = createOptionButton("");
            optionsPanel.add(optionButtons[i]);
        }

        // Bottom Controls
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton skipBtn = new JButton("Skip Question ⏭");
        skipBtn.setForeground(Color.LIGHT_GRAY);
        skipBtn.setContentAreaFilled(false);
        skipBtn.setBorderPainted(false);
        skipBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> generateNextQuestion());
        
        bottomPanel.add(optionsPanel, BorderLayout.CENTER);
        bottomPanel.add(skipBtn, BorderLayout.SOUTH);

        contentPanel.add(qTitle, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void generateNextQuestion() {
        // Pick a random question from the bank
        int randomIndex = (int) (Math.random() * answersList.size());
        String questionText = questionListModel.get(randomIndex);
        String rawAnswer = answersList.get(randomIndex);

        String plainText = rawAnswer.replaceAll("<[^>]*>", "").replace("Answer:", "").trim();
        
        // Find valid target word
        String[] words = plainText.split("\\s+");
        List<String> validWords = new ArrayList<>();
        for (String w : words) {
            String cleanWord = w.replaceAll("[^a-zA-Z]", "");
            if (cleanWord.length() > 4) validWords.add(cleanWord.toLowerCase());
        }

        if (validWords.isEmpty()) {
            generateNextQuestion(); // Retry if answer is too short
            return;
        }

        targetWord = validWords.get((int) (Math.random() * validWords.size()));
        String maskedText = plainText.replaceAll("(?i)\\b" + targetWord + "\\b", "<b><span style='color:#e74c3c;'>[ _________ ]</span></b>");

        qTitle.setText("<html>" + questionText + "</html>");
        maskedPane.setText("<html><body style='color:#f0f5fa; font-family:Segoe UI; font-size:15px; line-height: 1.6;'>" + maskedText + "</body></html>");
        
        // Generate Distractors (Wrong Options) by scanning other notes
        List<String> mcqOptions = new ArrayList<>();
        mcqOptions.add(targetWord);

        List<String> pool = new ArrayList<>();
        for (String ans : answersList) {
            String cleanAns = ans.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
            for (String w : cleanAns.split("\\s+")) {
                if (w.length() > 4 && !w.equals(targetWord) && !pool.contains(w)) {
                    pool.add(w);
                }
            }
        }
        
        Collections.shuffle(pool);
        for (int i = 0; i < Math.min(3, pool.size()); i++) {
            mcqOptions.add(pool.get(i));
        }

        // Fallback if knowledge bank is too small
        String[] fallbacks = {"algorithm", "variable", "database", "network", "function", "compile", "memory", "syntax"};
        int fbIndex = 0;
        while (mcqOptions.size() < 4) {
            if (!mcqOptions.contains(fallbacks[fbIndex])) mcqOptions.add(fallbacks[fbIndex]);
            fbIndex++;
        }

        Collections.shuffle(mcqOptions);

        // Assign to buttons
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(mcqOptions.get(i));
            // Reset colors
            optionButtons[i].setBackground(COLOR_OPTION_BG);
        }
    }

    private void checkAnswer(JButton clickedBtn) {
        String userGuess = clickedBtn.getText();
        
        if (userGuess.equalsIgnoreCase(targetWord)) {
            clickedBtn.setBackground(COLOR_ACCENT_GREEN); // Flash Green
            JOptionPane.showMessageDialog(this, "Correct! Great job.", "MCQ Passed", JOptionPane.INFORMATION_MESSAGE);
            generateNextQuestion();
        } else {
            clickedBtn.setBackground(new Color(192, 57, 43)); // Flash Red
            JOptionPane.showMessageDialog(this, "Incorrect. The correct answer was: " + targetWord.toUpperCase(), "Keep Studying", JOptionPane.ERROR_MESSAGE);
            generateNextQuestion(); // Move on to next question
        }
    }

    private JButton createOptionButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(COLOR_OPTION_BG);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> checkAnswer(btn));
        return btn;
    }
}