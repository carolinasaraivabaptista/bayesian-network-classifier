package projecto;
import java.util.LinkedList;
import java.io.*;

// A Bayesian network classifier

public class BN implements Serializable {
    private static final long serialVersionUID = 1L;
    
    Graphoo g;          // The graph structure
    double S;           // Smoothing factor
    double[][] theta;   // The probabilities
    int[] domains;      // Domain of each variable
    int classe;         // Index of the class variable
    double[] prob_c;    // Class prior P(C)
    
    // Takes the structure (graph) and the data (sample) and learns the parameters (theta)
    public BN(Amostra a, Graphoo g, double s) {
        this.g = g;
        S = s;
        this.classe = a.dim() - 1;
        
        // Initialise domains
        domains = new int[a.maximos.length];
        for (int i = 0; i < a.maximos.length; i++) {
            domains[i] = a.maximos[i] + 1;
        }
        
        // Compute P(C)
        prob_c = new double[domains[classe]];
        for (int i = 0; i < domains[classe]; i++) {
            prob_c[i] = (double)a.count(classe, i) / a.length();
        }

        
        learnTheta(a);
    }
    
    // Computes the probability tables (theta) from the sample counts
    private void learnTheta(Amostra a) {
        int n = a.dim() - 1;
        theta = new double[n][];

        for (int i = 0; i < n; i++) {
            LinkedList<Integer> pars = g.parents(i);
            int di = a.domain(i);
            int dc = a.domain(n);

            // Case: 0 parents
            if (pars.size() == 0) {
                int size = di * dc;
                theta[i] = new double[size];

                for (int xi = 0; xi < di; xi++) {
                    for (int c = 0; c < dc; c++) {
                        int num = a.count(i, n, xi, c);
                        int den = a.count(n, c);
                        // Laplace smoothing
                        double value = (num + S) / (den + S * di); 
                        int idx = xi + di * c;
                        theta[i][idx] = value;
                    }
                }
            }   
            // Case: 1 parent
            else if (pars.size() == 1) {
                int pai = pars.get(0);
                int dpi = a.domain(pai);
                int size = di * dpi * dc;
                theta[i] = new double[size];

                for (int xi = 0; xi < di; xi++) {
                    for (int pi = 0; pi < dpi; pi++) {
                        for (int c = 0; c < dc; c++) {
                            int num = a.count(i, pai, n, xi, pi, c);
                            int den = a.count(pai, n, pi, c);
                            double value = (num + S) / (den + S * di);

                            int idx = xi + di * pi + di * dpi * c;
                            theta[i][idx] = value;
                        }
                    }
                }
            }
            //  Case: 2 parents
            else if (pars.size() == 2) {
                int pai1 = pars.getFirst();
                int pai2 = pars.getLast();
                int dp1i = a.domain(pai1);
                int dp2i = a.domain(pai2);
                int size = di * dp1i * dp2i * dc;
                theta[i] = new double[size];

                for (int xi = 0; xi < di; xi++) {
                    for (int p1i = 0; p1i < dp1i; p1i++) {
                        for (int p2i = 0; p2i < dp2i; p2i++) {
                            for (int c = 0; c < dc; c++) {
                                int num = a.count(i, pai1, pai2, n, xi, p1i, p2i, c);
                                int den = a.count(pai1, pai2, n, p1i, p2i, c);
                                double value = (num + S) / (den + S * di);
                                
                                int idx = xi + di * p1i + di * dp1i * p2i + di * dp1i * dp2i * c;
                                theta[i][idx] = value;
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveBN(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "BN [g=" + g + ", S=" + S + "]";
    }
    
    // Computes the joint probability P(X1, ..., Xn, C)
    public double prob(int[] v) {
        double p = prob_c[v[classe]]; // Start with P(C)
        
        for (int i = 0; i < classe; i++) {
            LinkedList<Integer> pais = g.parents(i);
            int idx = 0;
            int di = domains[i];
            
            if(pais.size() == 0) {
                idx = v[i] + di * v[classe];
            }
            else if(pais.size() == 1) {
                int pai = pais.get(0);
                int dpi = domains[pai];
                idx = v[i] + di * v[pai] + di * dpi * v[classe];
            }
            else if(pais.size() == 2) {
                int pai1 = pais.getFirst();
                int pai2 = pais.getLast();
                int dp1i = domains[pai1];
                int dp2i = domains[pai2];
                idx = v[i] + di * v[pai1] + di * dp1i * v[pai2] + di * dp1i * dp2i * v[classe];
            }
            p = p * theta[i][idx];
        }
        return p;
    }

    // Classifies a sample (argmax P(C|data))
    public int classify(int[] m) {
        int[] na = new int[m.length + 1];
        System.arraycopy(m, 0, na, 0, m.length);
        
        double pmax = -1.0;
        int mc = -1;
        
        // Try every class and keep the most likely one
        for(int c = 0; c < domains[classe]; c++) {
            na[na.length - 1] = c;
            double p = prob(na);
            if(p > pmax) {
                pmax = p;
                mc = c;
            }
        }
        return mc;
    }
}