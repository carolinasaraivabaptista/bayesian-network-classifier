package projecto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;

// GUI for classification: load a saved Bayesian network, enter a patient's data and get the predicted class

public class ClassifyingApp extends JFrame {

    private int[] patient;          // Patient data vector to classify
    private JTextField patientField;// Text field to enter the data
    private BN bn;                  // The loaded Bayesian network

    // Constructor: sets up the window and its components
    public ClassifyingApp() {
        setTitle("Classifying App");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Area for status messages and results
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Classify button (disabled until valid data is entered)
        JButton classifyButton = new JButton("Classify");
        classifyButton.setEnabled(false);
        panel.add(classifyButton, BorderLayout.NORTH);

        // Data input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        patientField = new JTextField();
        JButton readVectorButton = new JButton("Read Patient Data");

        // Label showing an input example based on the loaded BN
        JLabel exampleLabel = new JLabel("Example:");

        inputPanel.add(exampleLabel, BorderLayout.NORTH);
        inputPanel.add(patientField, BorderLayout.CENTER);
        inputPanel.add(readVectorButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.CENTER);

        add(panel, BorderLayout.SOUTH);

        // Top panel
        JTextField loadField = new JTextField(15);
        JButton loadButton = new JButton("Load BN");

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("BN file:"));
        topPanel.add(loadField);
        topPanel.add(loadButton);

        add(topPanel, BorderLayout.NORTH);

        // Load BN button
        loadButton.addActionListener(new ActionListener() {
        	
        	@Override
        	
            public void actionPerformed(ActionEvent e) {
                String filename = loadField.getText().trim();

                if (filename.isEmpty()) {
                    logArea.append("Enter a BN file name.\n");
                    return;
                }

                // Read the BN object from the file
                try (ObjectInputStream in =
                         new ObjectInputStream(new FileInputStream(filename))) {

                    bn = (BN) in.readObject();
                    logArea.append("Loaded BN from " + filename + "\n");
                    classifyButton.setEnabled(true);
                    
                    // Show an example of the expected input (domain upper bounds)
                    int[] examplePatient = new int[bn.classe];
                    for (int i = 0; i < bn.classe; i++) {
            		    examplePatient[i] = bn.domains[i] - 1;
            		}

                    exampleLabel.setText("Example: " + Arrays.toString(examplePatient));

                } catch (IOException | ClassNotFoundException ex) {
                    logArea.append("Failed to load BN.\n");
                }
            }
        });

        //  Read patient data button
        readVectorButton.addActionListener(new ActionListener() {
        	
        	@Override
        	
            public void actionPerformed(ActionEvent e) {
                String text = patientField.getText();
                String[] parts = text.split(","); // Split the string on commas

                int[] tempPatient = new int[parts.length];
                boolean valid = true;

                if (bn == null) {
                    logArea.append("No BN loaded.\n");
                    return;
                }

                // Check the number of attributes matches the BN
                if (parts.length == bn.classe) {
                    for (int i = 0; i < parts.length; i++) {
                        try {
                            int value = Integer.parseInt(parts[i].trim());
                            // Check the value is within the variable's domain (0 to domain-1)
                            if (value < 0 || value > bn.domains[i] - 1) {
                                logArea.append("Value at position " + (i + 1) + " is out of bounds (0-" + (bn.domains[i] - 1) + ").\n");
                                valid = false;
                                break;
                            }
                            tempPatient[i] = value;
                        } catch (NumberFormatException ex) {
                            logArea.append("Invalid number at position " + (i + 1) + ".\n");
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        patient = tempPatient;
                        logArea.append("Patient data: " + Arrays.toString(patient) + "\n");
                    } else {
                        logArea.append("Patient data invalid.\n");
                    }
                } else {
                    logArea.append("Vector length must be " + (bn.classe) + "\n");
                }
            }
        });

        //  Classify button
        classifyButton.addActionListener(new ActionListener() {
        	
        	@Override
        	
            public void actionPerformed(ActionEvent e) {
                if (patient == null) {
                    logArea.append("No patient data entered\n");
                    return;
                }
                // Call the BN classification method
                int predictedClass = bn.classify(patient);
                logArea.append("Predicted class: " + predictedClass + "\n");
            }
        });
    }

    // Entry point: start the app
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClassifyingApp().setVisible(true);
            }
        });
    }
}