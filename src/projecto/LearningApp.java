package projecto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

// GUI for learning: select a CSV file, set the learning parameters, follow progress and save the trained network
 
public class LearningApp extends JFrame {

    private Amostra amostra;       // Data loaded from the CSV
    private String selectedCsv;    // Name of the selected file
    private BN learnedBN;          // The learned Bayesian network

    // Generates random graphs for the algorithm
    // Respects the max number of parents and never creates cycles
    private Graphoo generateRandomGraph(int dim, int maxParents) {
        Graphoo g = new Graphoo(dim);
        Random rand = new Random();
        for (int i = 0; i < dim; i++) {
            int numParents = rand.nextInt(maxParents + 1);
            for (int j = 0; j < numParents; j++) {
                int parent = rand.nextInt(dim);
                // Check: not itself, edge not present, parent limit respected
                if (parent != i && !g.edgeQ(parent, i) && g.parents(i).size() < maxParents) {
                    Graphoo test = new Graphoo(g);
                    test.addEdge(parent, i);
                    // Make sure there are no cycles
                    if (!test.cycleQ(i)) {
                        g.addEdge(parent, i);
                    }
                }
            }
        }
        return g;
    }

    // Constructor: sets up the main window and its components
    public LearningApp() {
        setTitle("Learn App");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Log area
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Control panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        // Buttons and labels
        JButton selectFileButton = new JButton("Select CSV");
        JButton learnButton = new JButton("Learn Bayesian Network");
        learnButton.setEnabled(false); // Enabled only after a file is selected

        JLabel maxParentsLabel = new JLabel("Max parents:");
        JTextField maxParentsField = new JTextField(10);

        JLabel numGraphsLabel = new JLabel("Random graphs:");
        JTextField numRandomGraphsField = new JTextField(10);

        // Component layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(selectFileButton, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(maxParentsLabel, gbc);

        gbc.gridx = 1;
        panel.add(maxParentsField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(numGraphsLabel, gbc);

        gbc.gridx = 1;
        panel.add(numRandomGraphsField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(learnButton, gbc);

        add(panel, BorderLayout.SOUTH);

        // Top panel to save the BN
        JTextField saveField = new JTextField(15);
        JButton saveButton = new JButton("Save BN");
        saveButton.setEnabled(false);

        JPanel savePanel = new JPanel(new FlowLayout());
        savePanel.add(new JLabel("File name:"));
        savePanel.add(saveField);
        savePanel.add(saveButton);
        add(savePanel, BorderLayout.NORTH);

        
        File projectDir = new File(System.getProperty("user.dir"));
        File[] csvFiles = projectDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        ArrayList<String> fileNames = new ArrayList<>();
        if (csvFiles != null) {
            for (File f : csvFiles) fileNames.add(f.getName());
        }

        // Dropdown with the files found
        JComboBox<String> csvComboBox = new JComboBox<>(fileNames.toArray(new String[0]));
        panel.add(new JLabel("Choose CSV:"));
        panel.add(csvComboBox);

        //  File selection button
        selectFileButton.addActionListener(new ActionListener() {
        	
        	@Override
        	
        	public void actionPerformed(ActionEvent e) {
        		selectedCsv = (String) csvComboBox.getSelectedItem();
                if (selectedCsv != null) {
                    logArea.append("Selected file: " + selectedCsv + "\n");
                    learnButton.setEnabled(true);
                }
        	}
        });
        
        // Learn button (runs hill climbing)
        learnButton.addActionListener(new ActionListener() {
        	
            @Override
            
            public void actionPerformed(ActionEvent e) {

            	if (selectedCsv == null) {
                    logArea.append("Please select a CSV file first.\n");
                    return;
                }
                amostra = new Amostra(selectedCsv);
                int maxParents = 0;
                int numRandomGraphs = 0;
                
                // Validate the numeric inputs
                try {
                    maxParents = Integer.parseInt(maxParentsField.getText());
                    numRandomGraphs = Integer.parseInt(numRandomGraphsField.getText());
                } catch (NumberFormatException ex) {
                    logArea.append("Please enter valid numbers for max parents and number of graphs.\n");
                    return;
                }

                logArea.append("Starting Bayesian network learning...\n");
                logArea.append("Max parents: " + maxParents + ", Random graphs: " + numRandomGraphs + "\n");

                //  Initialise the population of graphs
                ArrayList<Graphoo> initialGraphs = new ArrayList<>();
                
                // Add the empty graph
                Graphoo disconnected = new Graphoo(amostra.dim()-1);
                initialGraphs.add(disconnected);
                logArea.append("Added disconnected graph.\n");
                logArea.append("Graph edges: " + disconnected.toStringaux() + "\n");

                // Add random graphs
                for (int i = 0; i < numRandomGraphs-1; i++) {
                    Graphoo randomGraph = generateRandomGraph(amostra.dim()-1 , maxParents);
                    initialGraphs.add(randomGraph);
                    logArea.append("Added random graph " + (i+1) + "\n");
                    logArea.append("Graph edges: " + randomGraph.toStringaux() + "\n");
                }
                
                // Hill Climbing
                boolean changed;
                for(int j=0; j<numRandomGraphs;j++){

            		Graphoo bestGraph = new Graphoo(initialGraphs.get(j));
                	do{		
                        changed = false;
                        // Try to improve the MDL score by adding or removing edges
                		for(int i=0; i<amostra.dim() - 1; i++) {
                			for(int k=0; k<amostra.dim() - 1; k++) {
                				if (i==k) continue;
                				
                				// Try removing the edge
                				if(bestGraph.MDLdelta(amostra, i, k, 0) < 0) {
                					bestGraph.removeEdge(i, k);
                					changed = true;
                					continue;
                				}
                				// Try adding the edge
                				if(bestGraph.MDLdelta(amostra, i, k, 2) < 0) {
                					bestGraph.addEdge(i, k);
                					changed = true;
                				}
                			}
                		}
                	} while(changed); // Repeat until a local optimum is reached
            		initialGraphs.set(j, bestGraph);
                }

                // Pick the best model
                Graphoo bestGraph = initialGraphs.get(0);
                double bestMDL = bestGraph.MDL(amostra);

                for (int j = 1; j < initialGraphs.size(); j++) {
                    double mdl = initialGraphs.get(j).MDL(amostra);
                    if (mdl < bestMDL) {
                        bestGraph = initialGraphs.get(j);
                        bestMDL = mdl;
                    }
                }

                // Build the final BN and enable saving
                learnedBN = new BN(amostra, bestGraph, 0.5);
                logArea.append("Learning finished!\n");
                logArea.append("Best MDL: " + bestMDL + "\n");
                logArea.append("Graph edges: " + bestGraph.toStringaux() + "\n");
                
            	saveButton.setEnabled(true);
            }
        });

        //  Save button
        saveButton.addActionListener(new ActionListener() {
        	
            @Override
            
            public void actionPerformed(ActionEvent e) {
            	String filename = saveField.getText().trim();

                if (filename.isEmpty()) {
                    logArea.append("Please enter a file name.\n");
                    return;
                }

                learnedBN.saveBN(filename);
                logArea.append("BN saved to " + filename + "\n");
            }
        });
    }

    // Start the app on the Swing event thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
        	
            @Override
            
            public void run() {
                new LearningApp().setVisible(true);
            }
        });
    }
}