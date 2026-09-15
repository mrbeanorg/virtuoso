import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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

    // 3-Mark Question Database
    private final String[] questions = {
        "Q1: Define a stable sorting algorithm. Give an example.",
        "Q2: Differentiate between internal and external sorting.",
        "Q3: Outline the time complexities of Quick Sort.",
        "Q4: Explain the basic principle of Radix Sort.",
        "Q5: Why is Insertion Sort efficient for small datasets?",
        "Q6: What is the primary advantage of Merge Sort?",
        "Q7: Explain the concept of a 'Pivot' in sorting."
    };

    private final String[] answers = {
        "<b>Answer:</b><br><br>A sorting algorithm is considered <b>stable</b> if it preserves the relative order of equal elements in the sorted output. For example, if two distinct items possess the same sorting key, the one that appeared first in the original unsorted list will consistently appear first in the sorted list.<br><br><b>Examples:</b> Merge Sort and Bubble Sort are stable. Quick Sort and Heap Sort are inherently unstable.",
        "<b>Answer:</b><br><br><b>Internal Sorting:</b> Refers to algorithms where all the data to be sorted fits entirely within the computer's main memory (RAM) during the sorting process. <i>Examples: Bubble Sort, Quick Sort.</i><br><br><b>External Sorting:</b> Required when the dataset is too massive to fit into RAM, necessitating the use of external auxiliary storage (like hard drives) in chunks. <i>Example: External Merge Sort.</i>",
        "<b>Answer:</b><br><br>Quick Sort operates on a Divide and Conquer principle. Its time complexities are:<br><br>• <b>Best Case: O(n log n)</b> - Occurs when the chosen pivot naturally divides the array into two perfectly equal halves.<br>• <b>Average Case: O(n log n)</b><br>• <b>Worst Case: O(n²)</b> - Occurs when the array is already completely sorted (or reverse sorted) and the smallest or largest element is consistently chosen as the pivot, leading to highly unbalanced partitions.",
        "<b>Answer:</b><br><br><b>Radix Sort</b> is a specialized, non-comparison-based sorting algorithm. Instead of comparing elements against each other, it sorts elements digit by digit, typically starting from the least significant digit (LSD) and moving to the most significant digit (MSD).<br><br>It utilizes a stable intermediate algorithm (like Counting Sort) for each individual digit pass, ultimately achieving a linear time complexity of <b>O(d(n+k))</b>.",
        "<b>Answer:</b><br><br><b>Insertion Sort</b> has a very low constant algorithmic overhead and operates in <b>O(n) best-case time complexity</b> when the array is already partially or mostly sorted. <br><br>Its highly adaptive nature makes it drastically faster than O(n log n) algorithms for very small datasets. Because of this efficiency, it is frequently used as the base-case fallback in advanced hybrid algorithms like Timsort (used in Python and Java).",
        "<b>Answer:</b><br><br>The primary advantage of <b>Merge Sort</b> is its guaranteed, highly predictable performance. Regardless of the initial arrangement of the data (whether already sorted, reverse sorted, or completely randomized), Merge Sort will always run in <b>O(n log n)</b> time.<br><br>Additionally, it is a stable sort, making it ideal for sorting linked lists where contiguous memory allocation is not a constraint.",
        "<b>Answer:</b><br><br>In the context of Quick Sort, a <b>Pivot</b> is a designated element selected from the array that acts as a structural reference point for partitioning.<br><br>During a sorting pass, all elements smaller than the pivot are shifted to its left, and all elements greater than the pivot are shifted to its right. The recursive efficiency of Quick Sort heavily relies on how close the pivot is to the actual median of the dataset."
    };

    public ExamPrepPanel(String activeUser, Runnable onBackToMenu) {
        this.currentUser = activeUser;
        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_MAIN_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(onBackToMenu), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(Runnable onBackToMenu) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 45));

        JLabel titleLabel = new JLabel("Academic Prep - 3 Mark Questions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.setOpaque(false);

        JLabel userBadge = new JLabel("👤 " + (currentUser != null ? currentUser.toUpperCase() : "STUDENT"));
        userBadge.setForeground(COLOR_TEXT_HEADER);
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton backBtn = createButton("Dashboard", new Color(55, 65, 80), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(110, 38));
        backBtn.addActionListener(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        rightControls.add(userBadge);
        rightControls.add(backBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel splitContainer = new JPanel(new BorderLayout(25, 0));
        splitContainer.setOpaque(false);

        // --- LEFT SIDEBAR: Question List ---
        JPanel leftPanel = createRoundedCard();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(320, 0));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel listTitle = new JLabel("UNIVERSITY QA BANK");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        listTitle.setForeground(COLOR_TEXT_MUTED);
        listTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        JList<String> questionList = new JList<>(questions);
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
                if (idx != -1) {
                    currentQuestionTitle.setText(questions[idx]);
                    answerDisplayPane.setText("<html><body style='color:#f0f5fa; font-family:Segoe UI; font-size:14px; line-height: 1.6;'>" + answers[idx] + "</body></html>");
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

        currentQuestionTitle = new JLabel("Select a question from the bank...");
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

        // Select the first question by default
        questionList.setSelectedIndex(0);

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