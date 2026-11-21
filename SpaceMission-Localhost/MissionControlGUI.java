import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
// Event Handling: Creates an interactive mission control GUI.
public class MissionControlGUI extends JFrame
        implements ActionListener {
    // GUI components: Accept mission details from the user.
    private JTextField idField = new JTextField(15);
    private JTextField fuelField = new JTextField(15);
    private JComboBox<String> destinationBox =
            new JComboBox<>(new String[]{"Mars", "Earth Orbit"});
    private JButton registerButton =
            new JButton("Register Mission");

    private JButton launchButton =
            new JButton("Launch Mission");

    private JButton databaseButton =
            new JButton("Save to Database");

    private JLabel statusLabel =
            new JLabel("Welcome to Mission Control!");

    // Stores the current mission.
    private SpaceMission mission;

    // Shares the mission registry with the main application.
    private MissionRegistry registry;

    // Constructor: Creates and configures the GUI window.
    public MissionControlGUI(MissionRegistry registry) {

        this.registry = registry;

        setTitle("Space Mission Management System");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(6, 2, 10, 10));

        add(new JLabel("Mission ID:"));
        add(idField);

        add(new JLabel("Available Fuel:"));
        add(fuelField);

        add(new JLabel("Destination:"));
        add(destinationBox);

        add(registerButton);
        add(launchButton);

        add(databaseButton);
        add(statusLabel);

        // Event handling: Registers button click listeners.
        registerButton.addActionListener(this);
        launchButton.addActionListener(this);
        databaseButton.addActionListener(this);
    }

    // Event Handling: Handles button click events.
    @Override
    public void actionPerformed(ActionEvent e) {

        // Registration event.
        if (e.getSource() == registerButton) {

            try {

                String id = idField.getText().trim();
                double fuel = Double.parseDouble(
                        fuelField.getText());

                if (id.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this, "Enter Mission ID!");
                    return;
                }

                String destination =
                        (String) destinationBox.getSelectedItem();

                if (destination.equals("Mars")) {
                    mission = new MarsMission(id, fuel);
                } else {
                    mission = new SatelliteMission(id, fuel);
                }

                // Collections: Adds the mission to the registry.
                if (!registry.addMission(mission)) {
                    mission = null;
                    statusLabel.setText("Duplicate Mission ID!");
                    return;
                }

                // Logging: Records mission registration.
                MissionLogger.logInfo(
                        "Mission " + id + " registered.");

                // File Handling: Saves the mission report.
                MissionFileManager.saveMissionReport(mission);

                statusLabel.setText("Mission registered!");

                JOptionPane.showMessageDialog(
                        this, "Mission registered successfully!");

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this, "Enter valid fuel!");

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        this, ex.getMessage());

            } catch (java.io.IOException ex) {

                JOptionPane.showMessageDialog(
                        this, "File Error: " + ex.getMessage());
            }
        }

        // Threading: Launches the mission in a background thread.
        else if (e.getSource() == launchButton) {

            if (mission == null) {
                JOptionPane.showMessageDialog(
                        this, "Register a mission first!");
                return;
            }

            SpaceMission selectedMission = mission;

            new Thread(new Runnable() {

                public void run() {

                    try {

                        MissionLogger.logInfo(
                                "Mission " +
                                selectedMission.getMissionId()
                                + " launch started.");

                        selectedMission.launch();

                        MissionLogger.logInfo(
                                "Mission " +
                                selectedMission.getMissionId()
                                + " launched successfully.");

                        SwingUtilities.invokeLater(() ->
                                statusLabel.setText(
                                "Mission launched successfully!"));

                    } catch (InsufficientFuelException ex) {

                        MissionLogger.logWarning(
                                ex.getMessage());

                        SwingUtilities.invokeLater(() ->
                                statusLabel.setText(
                                "Launch failed: Insufficient fuel"));
                    }
                }

            }).start();
        }

        // JDBC: Saves the registered mission into MySQL.
        else if (e.getSource() == databaseButton) {

            if (mission == null) {
                JOptionPane.showMessageDialog(
                        this, "Register a mission first!");
                return;
            }

            try {

                String status =
                        mission.getFuel() >=
                        mission.calculateFuelRequired()
                        ? "READY" : "INSUFFICIENT FUEL";

                MissionDatabase.insertMission(
                        mission, status);

                MissionLogger.logInfo(
                        "Mission saved to database.");

                statusLabel.setText("Database saved!");

                JOptionPane.showMessageDialog(
                        this, "Mission saved to database!");

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this, "Database Error: " + ex.getMessage());
            }
        }
    }
}
