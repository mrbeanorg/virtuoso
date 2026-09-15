import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;
    private AlgorithmVirtuosoPanel virtuosoPanel;
    
    // We replaced vaultPanel with your new ExamPrepPanel
    private ExamPrepPanel examPrepPanel;

    private String loggedInUser = "alan";

    public MainApp() {
        setTitle("Virtuoso - Algorithm Tutor & 3-Mark Q&A Bank");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 740);
        setLocationRelativeTo(null);
        setResizable(true); 

        DatabaseApp.initializeTables();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(new Color(15, 18, 22)); 

        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        dashboardPanel = new DashboardPanel(this);

        mainContainer.add(loginPanel, "LOGIN");
        mainContainer.add(registerPanel, "REGISTER");
        mainContainer.add(dashboardPanel, "DASHBOARD");

        add(mainContainer);
    }

    public void setLoggedInUser(String username, boolean showBanner) {
        this.loggedInUser = username;
        setTitle("Virtuoso - Logged in as: " + loggedInUser);
        dashboardPanel.updateUserGreeting(username, showBanner);
    }

    public String getLoggedInUser() {
        return this.loggedInUser;
    }

    public void showView(String cardName) {
        if (cardName.equals("LOGIN")) {
            loginPanel.resetFields();
        } else if (cardName.equals("REGISTER")) {
            registerPanel.resetFields();
        } else if (cardName.equals("VIRTUOSO")) {
            if (virtuosoPanel != null) {
                mainContainer.remove(virtuosoPanel);
            }
            virtuosoPanel = new AlgorithmVirtuosoPanel(loggedInUser, () -> showView("DASHBOARD"));
            mainContainer.add(virtuosoPanel, "VIRTUOSO");
            
        // Load the new ExamPrepPanel instead of the old Vault
        } else if (cardName.equals("EXAM_PREP")) {
            if (examPrepPanel != null) {
                mainContainer.remove(examPrepPanel);
            }
            examPrepPanel = new ExamPrepPanel(loggedInUser, () -> showView("DASHBOARD"));
            mainContainer.add(examPrepPanel, "EXAM_PREP");
        }
        cardLayout.show(mainContainer, cardName);
    }

    public void prefillLogin(String username) {
        loginPanel.setLoginUsername(username);
    }

    private static void setupGlobalUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, modernFont);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setupGlobalUI();
        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }
}