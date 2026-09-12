import javax.swing.*;
import java.awt.*;

public class StyledBackgroundPanel extends JPanel {
    public StyledBackgroundPanel() {
        setLayout(new GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        Color color1 = new Color(20, 24, 30);
        Color color2 = new Color(15, 18, 22);
        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}