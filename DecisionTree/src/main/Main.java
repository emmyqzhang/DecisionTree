package main;

import java.io.IOException;

import decisiontree.Algorithm;
import decisiontree.DecisionTree;

public class Main {

	public static void main(String[] arg) throws IOException {
		// Read input file and run algorithm to create a decision tree
		Algorithm algorithm = new Algorithm();
		
		DecisionTree tree = algorithm.start("name");
		algorithm.print();

		// print the decision tree:
		tree.print();
	}
}
