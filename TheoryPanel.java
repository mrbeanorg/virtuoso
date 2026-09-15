import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TheoryPanel extends JPanel {

    private static final Color COLOR_MAIN_BG = new Color(20, 24, 29);
    private static final Color COLOR_PANEL_BG = new Color(28, 33, 40, 240);
    private static final Color COLOR_TEXT_HEADER = new Color(240, 245, 250);
    private static final Color COLOR_BORDER = new Color(48, 55, 65);

    public TheoryPanel(String username, Runnable onBack) {
        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_MAIN_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 45));

        JLabel titleLabel = new JLabel("Algorithm Theory & Tutor Reference");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);

        JButton backBtn = LoginPanel.createButton("Back to Workspace", new Color(55, 65, 80), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(160, 38));
        backBtn.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Content Card matching project theme
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
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        String html = "<html><body style='color:#f0f5fa; font-family:Segoe UI; padding:15px; font-size:13px;'>" +
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
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scroll.getViewport().setBackground(COLOR_PANEL_BG);

        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }
}