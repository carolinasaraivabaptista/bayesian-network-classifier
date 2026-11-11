package projecto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// Learning and validation: contains the hill climbing algorithm and leave-one-out validation
 
public class Validation {
    
    // Generates a random acyclic graph within the parent limit.
    private static Graphoo generateRandomGraph(int dim, int maxParents) {
        Graphoo g = new Graphoo(dim);
        Random rand = new Random();
        for (int i = 0; i < dim; i++) {
            int numParents = rand.nextInt(maxParents + 1);
            for (int j = 0; j < numParents; j++) {
                int parent = rand.nextInt(dim);
                // Check: not itself, edge not present, parent limit not exceeded
                if (parent != i && !g.edgeQ(parent, i) && g.parents(i).size() < maxParents) {
                    Graphoo test = new Graphoo(g);
                    test.addEdge(parent, i);
                    // Make sure no cycles are created
                    if (!test.cycleQ(i)) {
                        g.addEdge(parent, i);
                    }
                }
            }
        }
        return g;
    }

    // Hill climbing learning algorithm with random restarts
    public static BN learnBN(int maxParents, int randGraphs, Amostra trainingSet, double s) {
        
        ArrayList<Graphoo> initialGraphs = new ArrayList<>();
        
        // Empty graph
        Graphoo disconnected = new Graphoo(trainingSet.dim()-1);
        initialGraphs.add(disconnected);

        // Random graphs
        for (int i = 0; i < randGraphs - 1; i++) {
            Graphoo randomGraph = generateRandomGraph(trainingSet.dim()-1 , maxParents);
            initialGraphs.add(randomGraph);
        }
        
        // Hill climbing from each initial graph
        for(int j = 0; j < randGraphs; j++){
            Graphoo bestGraph = new Graphoo(initialGraphs.get(j));
            boolean changed;
            do {
                changed = false;
                for(int i = 0; i < trainingSet.dim() - 1; i++) {
                    for(int k = 0; k < trainingSet.dim() - 1; k++) {
                        if (i == k) continue;
                        
                        // Try removing the edge
                        if(bestGraph.MDLdelta(trainingSet, i, k, 0) < 0) {
                            bestGraph.removeEdge(i, k);
                            changed = true;
                            continue;
                        }
                        // Try adding the edge
                        if(bestGraph.MDLdelta(trainingSet, i, k, 2) < 0) {
                            bestGraph.addEdge(i, k);
                            changed = true;
                        }
                       
                    }
                }
            } while(changed);
            initialGraphs.set(j, bestGraph);
        }

        // Pick the lowest MDL
        Graphoo bestGraph = initialGraphs.get(0);
        double bestMDL = bestGraph.MDL(trainingSet);

        for (int j = 1; j < initialGraphs.size(); j++) {
            double mdl = initialGraphs.get(j).MDL(trainingSet);
            if (mdl < bestMDL) {
                bestGraph = initialGraphs.get(j);
                bestMDL = mdl;
            }
        }

        // Return the trained BN
        return new BN(trainingSet, bestGraph, s);
    }

    // Leave-one-out cross-validation
    public static double leaveOneOut(String csvFile, double s, int maxParents, int randGraphs) {
        Amostra data = new Amostra(csvFile);
        int n = data.length();
        int correct = 0;

        System.out.println("Starting leave-one-out for " + csvFile);

        for (int i = 0; i < n; i++) {
            if (i % 100 == 0) System.out.println("Iteration: " + i + "/" + n);
            
            // Hold out 1 example for testing
            int[] testPatient = data.lista.remove(i);

            // Train on the rest
            BN bn = learnBN(maxParents, randGraphs, data, s);

            // Test
            int[] testVector = Arrays.copyOf(testPatient, testPatient.length - 1);
            int predicted = bn.classify(testVector);

            if (predicted == testPatient[testPatient.length - 1]) {
                correct++;
            }
            
            // Put the example back for the next iteration
            data.lista.add(i, testPatient);
        }

        return (double) correct / n;
    }

    public static void main(String[] args) {
        
        // =================================================================================
        // Test results
        // Parameters: MaxParents = 2 and S = 0.5
        // =================================================================================
        
        // 1. Breast Cancer 
        // System.out.println("BCancer accuracy: " + leaveOneOut("bcancer.csv", 0.5, 2, 10));
        // Result: 82.72 %

        // 2. Thyroid
        System.out.println("Thyroid accuracy: " + leaveOneOut("thyroid.csv", 0.5, 2, 5));
        // Result: 96.90 %

        // 3. Diabetes
        // System.out.println("Diabetes accuracy: " + leaveOneOut("diabetes.csv", 0.5, 2, 10));
        // Result: 78.39 %

        // 4. Hepatitis
        // System.out.println("Hepatitis accuracy: " + leaveOneOut("hepatitis.csv", 0.5, 2, 10));
        // Result: 88.75 %

        // 5. Soy bean
        // System.out.println("Soybean-large accuracy: " + leaveOneOut("soybean-large.csv", 0.5, 2, 5));
        // Result: 92.35 %

        // Note: leave-one-out on the datasets below takes a long time to run

        // 6. Letter 
        // System.out.println("Letter accuracy: " + leaveOneOut("letter.csv", 0.5, 2, 1));
        // Result: not run (too slow)

        // 7. Satimage 
        // System.out.println("Satimage accuracy: " + leaveOneOut("satimage.csv", 0.5, 2, 1));
        // Result: 87.13 %
    }
}