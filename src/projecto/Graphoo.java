package projecto;

import java.util.LinkedList;
import java.io.Serializable;

// Structure of the Bayesian network: the directed graph (nodes and edges) and its scores
 
public class Graphoo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    int dim; // Number of nodes (variables)
    LinkedList<Integer>[] la; // Adjacency list: la[i] holds the parents of node i
    
    // Base constructor: creates an empty graph with 'dim' nodes
    public Graphoo(int dim) {
        super();
        this.dim = dim;
        this.la = new LinkedList[dim];
        for (int i = 0; i < la.length; i++) {
            this.la[i] = new LinkedList<Integer>();
        }
    }
    
    // Copy constructor: clones the graph (used to simulate moves in hill climbing)
    public Graphoo(Graphoo g) {
        this.dim = g.dim;
        this.la = new LinkedList[dim];
        for (int i = 0; i < dim; i++) {
            this.la[i] = new LinkedList<>(g.la[i]);
        }
    }

    // String formatting helper
    static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
          ? "" 
          : (s.substring(0, s.length() - 1));
    }

    // Builds the text representation of the edges (parent,child)
    String toStringaux() {
        String s = "";
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if(edgeQ(i,j)) {
                    s=s+"("+i+","+j+")"+",";
                }
            }
        }
        return "{" + removeLastChar(s) +"}";
    }
    
    @Override
    public String toString() {
        return "Graph [dim=" + dim + ", V=" + toStringaux() + "]";
    }
    
    // Adds a directed edge from o to d
    public void addEdge(int o, int d) {
        if (edgeQ(d, o) == false)
            this.la[d].add(o);
    }

    // Removes the edge from o to d
    public void removeEdge(int o, int d) {
        this.la[d].remove((Integer)o);
    }
    
    // Reverses the direction of the edge
    public void invertEdge(int o, int d) {
        if (edgeQ(o, d) == true) {
            this.removeEdge(o, d);
            this.addEdge(d, o);
        }
    }
    
    // Checks whether there is an edge from o to d
    public boolean edgeQ(int o, int d) {
        return la[d].contains(o);
    }
    
    // Returns the parents of node d
    public LinkedList<Integer> parents (int d){
        return new LinkedList<Integer>(la[d]);
    }
    
    // Checks whether there is a path from o to d (to detect cycles)
    // Algorithm: breadth-first search
    public boolean pathQ(int o, int d){
        boolean[] v = new boolean[dim];
        boolean found = false;
        LinkedList<Integer> toProcess = new LinkedList<Integer>();
        toProcess.add(d);
        while(!toProcess.isEmpty() && !found) {
            int n = toProcess.remove();
            if (n == o) found = true;
            if(!v[n] && !found) {
                v[n] = true;
                toProcess.addAll(parents(n));
            }
        }
        return found;
    }
    
    // Checks whether there is a cycle reachable from node d
    public boolean cycleQ(int d){
        boolean found = false;
        boolean[] v = new boolean[dim];
        LinkedList<Integer> toProcess = new LinkedList<Integer>();
        toProcess.add(d);
        while(!toProcess.isEmpty() && !found) {
            int n = toProcess.remove();
            if(!v[n]) {
                v[n] = true;
                toProcess.addAll(parents(n));
            }
            else
                found = true;
        }
        return found;
    }
    
    // Base-2 logarithm
    public double log2(double v) {
        return Math.log(v) / Math.log(2);
    }
    

    // Optimised probability computation
  
    double I(int i, Amostra a) {
        int classe = a.dim() - 1;
        LinkedList<Integer> pars = parents(i);
        double m = a.length();
        double s = 0.0;
        
        int domI = a.domain(i);
        int domC = a.domain(classe);

        // Case: 0 parents
        // No contribution (depends only on the class)
   
        if (pars.size() == 0) {            
        }
        
        // Case: 1 parent
        // Computes the information gain of adding 1 parent besides the class.
        else if (pars.size() == 1) {
            int pai = pars.get(0);
            int domPai = a.domain(pai);
            
            // Count tables filled in a single pass over the data
            int[][][] N = new int[domI][domPai][domC];
            int[][] N_ic = new int[domI][domC];
            int[][] N_pc = new int[domPai][domC];
            int[] N_c = new int[domC];

            // Single pass (O(N) instead of O(N * domains))
            for (int k = 0; k < a.length(); k++) {
                int[] row = a.element(k);
                int valI = row[i];
                int valP = row[pai];
                int valC = row[classe];
                
                N[valI][valP][valC]++;
                N_ic[valI][valC]++;
                N_pc[valP][valC]++;
                N_c[valC]++;
            }

            // Weighted sum
            for (int di = 0; di < domI; di++) {
                for (int pi = 0; pi < domPai; pi++) {
                    for (int c = 0; c < domC; c++) {
                        int N_ipc = N[di][pi][c];
                        if (N_ipc > 0) {
                            double num = (double) N_ipc * N_c[c];
                            double den = (double) N_ic[di][c] * N_pc[pi][c];
                            s += N_ipc * log2(num / den);
                        }
                    }
                }
            }
        }
        
        // Case: 2 parents
        // Same as the previous case
        else if (pars.size() == 2) {
            int pai1 = pars.get(0);
            int pai2 = pars.get(1);
            int domP1 = a.domain(pai1);
            int domP2 = a.domain(pai2);
            
            int[][][][] N = new int[domI][domP1][domP2][domC];
            int[][][] N_p1p2c = new int[domP1][domP2][domC];
            int[][] N_ic = new int[domI][domC];
            int[] N_c = new int[domC];

            // Single pass
            for (int k = 0; k < a.length(); k++) {
                int[] row = a.element(k);
                int valI = row[i];
                int valP1 = row[pai1];
                int valP2 = row[pai2];
                int valC = row[classe];

                N[valI][valP1][valP2][valC]++;
                N_p1p2c[valP1][valP2][valC]++;
                N_ic[valI][valC]++;
                N_c[valC]++;
            }
            
            for (int di = 0; di < domI; di++) {
                for (int p1 = 0; p1 < domP1; p1++) {
                    for (int p2 = 0; p2 < domP2; p2++) {
                        for (int c = 0; c < domC; c++) {
                            int N_total = N[di][p1][p2][c];
                            if(N_total > 0) {
                                double num = (double) N_total * N_c[c];
                                double den = (double) N_ic[di][c] * N_p1p2c[p1][p2][c];
                                s += N_total * log2(num / den);
                            }
                        }
                    }
                }
            }
        }
        
        return s/m;
    }
  

    // Computes the total log-likelihood of the network
    public double LL(Amostra a) {
        int classe = a.dim()-1; 
        double r = 0.0;
        double m = a.length();
        for (int i = 0; i < classe; i++)
            r += I(i, a);
        return r * m;
    }
    
    // Computes the MDL score
   
    public double MDL(Amostra a) {
        double theta = 0;
        int classe = a.dim()-1;
        
        // Computes the number of free parameters
        for (int i = 0; i < a.dim()-1; i++) {
            theta = theta + (a.domain(i)-1)*a.domain(parents(i))*a.domain(classe);
        }
        theta = theta + a.domain(classe) - 1;
        
        return theta * log2(a.length()) / 2 - LL(a);
    }
    
    // Computes the score change (delta) for a given operation
    // Lets hill climbing evaluate a move without rescoring the whole network.
    public double MDLdelta(Amostra a, int o, int d, int op) {
        int m = a.length();
        int classe = a.dim() - 1;
        double MDLdelta = 0.0;
        
        // Add edge
        if (op == 2 && !edgeQ(o, d)) {
            double oldI = I(d, a);
            double oldtheta = a.domain(parents(d));
            
            addEdge(o, d);
            // Check constraints: at most 2 parents and acyclic
            if(parents(d).size() <= 2 && !cycleQ(d)) {
                double newI = I(d, a);
                double newtheta = a.domain(parents(d));
                
                double deltaI = newI - oldI;
                double deltatheta = newtheta - oldtheta;
                
                MDLdelta = (log2(m)/2) * (a.domain(d) - 1) * a.domain(classe) * (deltatheta) - m * deltaI;
            } else {
                MDLdelta = 1; // Positive delta means worse (MDL is minimised)
            }
            removeEdge(o, d); // Undo the simulated move
        }
        // Remove edge
        else if (op == 0 && edgeQ(o, d)) {      
            double oldI = I(d, a);
            double oldtheta = a.domain(parents(d));
            
            removeEdge(o, d);
            double newI = I(d, a);
            double newtheta = a.domain(parents(d));
            
            double deltaI = newI - oldI;
            double deltatheta = newtheta - oldtheta;
            
            MDLdelta = (log2(m)/2) * (a.domain(d) - 1) * a.domain(classe) * (deltatheta) - m * deltaI;
                
            addEdge(o, d); // Undo
        }
        // Reverse edge
        else if (op == 1 && edgeQ(o, d)) {
            // Reversing changes the scores of both the source and the target
            double oldI = I(d, a) + I(o, a);
            double oldtheta1 = a.domain(parents(d));
            double oldtheta2 = a.domain(parents(o));
            
            invertEdge(o, d);
            if(parents(o).size() <= 2 && !cycleQ(o)) {
                int dc = a.domain(classe);
                
                double newI = I(d, a) + I(o, a);
                double newtheta1 = a.domain(parents(d));
                double newtheta2 = a.domain(parents(o));
                
                double deltaI = newI - oldI;
                double deltatheta1 = newtheta1 - oldtheta1;
                double deltatheta2 = newtheta2 - oldtheta2;
                
                MDLdelta = (log2(m)/2) * ((a.domain(d) - 1) * dc * (deltatheta1) + (a.domain(o) - 1) * dc * (deltatheta2)) - m * deltaI;
            } else {
                MDLdelta = 1;
            }
            invertEdge(d, o); // Undo
        }
        return MDLdelta;
    }
    
    // Score with the sign flipped
    public double MDLs(Amostra a) {
        return -MDL(a);
    }

    public static void main(String[] args) {
        // Quick test of the class
        Amostra a = new Amostra("bcancer.csv");
        Graphoo g = new Graphoo(a.dim()-1);
        g.addEdge(0, 1);
        System.out.println(g);
        System.out.println("MDL= "+ g.MDL(a));
    }
}