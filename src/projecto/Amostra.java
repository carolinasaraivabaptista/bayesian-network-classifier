package projecto;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

// The dataset: loads the CSV, keeps the samples in memory and provides counts and variable domains
 
public class Amostra {
	
	// Stores all samples. Each int[] is one row of the dataset
	ArrayList<int[]> lista;
	// Maximum value seen for each variable
	int[] maximos = null;

	public Amostra() {
		this.lista = new ArrayList<int[]>();
	}

	// Helper: converts a CSV text line into an int array
	static int[] convert (String line) {
		String cvsSplitBy = ",";
		String[] strings = line.split(cvsSplitBy);
		int[] stringToIntVec = new int[strings.length];
		for (int i = 0; i < strings.length; i++)
			stringToIntVec[i] = Integer.parseInt(strings[i]);
		return stringToIntVec;
	}
	
	// Constructor: loads and parses the CSV file line by line
	public Amostra(String csvFile) {
		this.lista = new ArrayList<int []>();
		this.maximos = null;
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {	
				add(convert(line));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int dim() {
		return maximos.length; // Number of variables (columns)
	}
	
	// Adds a new sample and updates the maximum values (domains)
	public void add (int[] v){
		if (maximos == null) {
			maximos = new int[v.length];
			System.arraycopy(v, 0, maximos, 0, v.length);
		} else {
			for (int i = 0; i < maximos.length; i++)
				if (maximos[i] < v[i]) maximos[i] = v[i];
		}
		lista.add(v);
	}
	
	public int length() {
		return lista.size(); // Number of examples (rows)
	}
	
	public int[] element (int i){
		return lista.get(i);
	}
	
	// Checks whether sample j matches the given variable values
	private boolean equalQ(int[] var, int[] val, int j){
		boolean r = true;
		if (var != null) {
			int v[] = lista.get(j);
			for (int i = 0; i < var.length && r; i++)
				if (v[var[i]] != val[i]) r = false;
		}
		return r;
	}
	
	// Computes the size of the state space for a set of variables
	public int domain (int[] var){
		if (var == null)
			return 0;
		else {
			int r = 1;
			for (int i = 0; i < var.length; i++)
				r *= (maximos[var[i]] + 1);
			return r;
		}
	}
	
	// domain() for a LinkedList (used by the graph computations)
	public int domain (LinkedList<Integer> var){
		if (var == null)
			return 0;
		else {
			int r = 1;
			for (int i = 0; i < var.size(); i++)
				r *= (maximos[var.get(i)] + 1);
			return r;
		}
	}
	
	// Cardinality of a single variable
	public int domain (int v){
		return maximos[v] + 1;
	}

	// The methods below count how often a combination of values occurs in the dataset
	
	public int count(int var, int val){
		int r = 0;
		int vars[] = {var};
		int vals[] = {val};
		for (int j = 0; j < lista.size(); j++)
			if (equalQ(vars, vals, j)) r += 1;
		return r;
	}
	
	public int count(int var1, int var2, int val1, int val2){
		int r = 0;
		int vars[] = {var1, var2};
		int vals[] = {val1, val2};
		for (int j = 0; j < lista.size(); j++)
			if (equalQ(vars, vals, j)) r += 1;
		return r;
	}
	
	public int count(int var1, int var2, int var3, int val1, int val2, int val3){
		int r = 0;
		int vars[] = {var1, var2, var3};
		int vals[] = {val1, val2, val3};
		for (int j = 0; j < lista.size(); j++)
			if (equalQ(vars, vals, j)) r += 1;
		return r;
	}
	
	public int count(int var1, int var2, int var3, int var4, int val1, int val2, int val3, int val4){
		int r = 0;
		int vars[] = {var1, var2, var3, var4};
		int vals[] = {val1, val2, val3, val4};
		for (int j = 0; j < lista.size(); j++)
			if (equalQ(vars, vals, j)) r += 1;
		return r;
	}
	
	public String toString() {
		String s = "\n[\n";
		if (lista.size() > 0) s += Arrays.toString(lista.get(0));
		for (int i = 1; i < lista.size(); i++)
			s += "\n" + Arrays.toString(lista.get(i));
		s += "\n]";
		int[] mydomain = new int[maximos.length];
		for (int i = 0; i < maximos.length; i++)
			mydomain[i] = maximos[i] + 1;
			
		return "Domains \n" + Arrays.toString(mydomain) + "\n" + "Sample " + s;
	}
	
	// Used only to test this class

	public static void main(String[] args) {
		Amostra amostra = new Amostra("bcancer.csv");
		
		int vars[][] = new int [5][];
		int vals[][] = new int [5][];
		
		int[] vazio = {};
		
		vars[0] = new int[] {0,3,2,10};
		vals[0] = new int[] {1,3,2,1};
		
		vars[1] = new int[] {2,3,5,10};
		vals[1] = new int[] {1,2,1,1};
		
		vars[2] = new int[] {0,6,7,9};
		vals[2] = new int[] {0,0,1,0};
		
		vars[3] = new int[] {3,4,5,9};
		vals[3] = new int[] {3,2,0,1};
		
		vars[4] = new int[] {6,2,3,10};
		vals[4] = new int[] {1,0,2,0};
		
		for(int i = 0; i < vars.length; i++) {
			System.out.println(amostra.domain(vazio));
		}
		
		
	}

}