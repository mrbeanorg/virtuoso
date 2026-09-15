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
        
        // Enable high-quality rendering for a smooth gradient
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        Color color1 = new Color(20, 24, 30);
        Color color2 = new Color(10, 12, 15); // Darkened for better contrast depth
        
        // Top-to-bottom gradient
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}