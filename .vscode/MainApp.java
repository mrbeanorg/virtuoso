import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;
    private AlgorithmVirtuosoPanel virtuosoPanel;
    private PasswordVaultPanel vaultPanel;

    private String loggedInUser = "alan";

    public MainApp() {
        setTitle("Secure Password Generator & Vault (" + loggedInUser + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 740);
        setLocationRelativeTo(null);
        setResizable(true);

        DatabaseApp.initializePasswordTable();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

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
        setTitle("Secure Password Generator & Vault (" + loggedInUser + ")");
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
        } else if (cardName.equals("VAULT")) {
            if (vaultPanel != null) {
                mainContainer.remove(vaultPanel);
            }
            vaultPanel = new PasswordVaultPanel(loggedInUser, () -> showView("DASHBOARD"));
            mainContainer.add(vaultPanel, "VAULT");
        }
        cardLayout.show(mainContainer, cardName);
    }

    public void prefillLogin(String username) {
        loginPanel.setLoginUsername(username);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }
}