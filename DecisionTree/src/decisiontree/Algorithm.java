package decisiontree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The ID3 algorithm for creating a decision tree.
 
 * 
 */
public class Algorithm {
	private String[] allAttributes;
	private int targetIndex = -1;
	private Set<String> targetValues = new HashSet<String>();

	
	public DecisionTree start(String targetAttribute) throws IOException {

		// create an empty decision tree
		DecisionTree tree = new DecisionTree();

		// read input file
		InputStream is = this.getClass().getResourceAsStream("/resource/zoo.data");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = reader.readLine();

		allAttributes = line.split(",");

		int[] otherAttributes = new int[allAttributes.length - 1];
		int pos = 0;
		for (int i = 0; i < allAttributes.length; i++) {
			if (allAttributes[i].equals(targetAttribute)) {
				targetIndex = i;
			} else {
				otherAttributes[pos++] = i;
			}
		}

		
		List<String[]> instances = new ArrayList<String[]>();
		while (((line = reader.readLine()) != null)) {
			String[] lineSplit = line.split(",");
			instances.add(lineSplit);
			targetValues.add(lineSplit[targetIndex]);
		}
		reader.close(); 

		// Start the recusive process

		// create the tree
		tree.root = createTree(otherAttributes, instances);
		tree.allAttributes = allAttributes;

		return tree; 
	}

	
	private Node createTree(int[] otherAttributes, List<String[]> instances) {
		if (otherAttributes.length == 0) {
			Map<String, Integer> targetValuesFrequency = computeFrequency(instances, targetIndex);
			int highestCount = 0;
			String highestName = "";
			for (Entry<String, Integer> entry : targetValuesFrequency.entrySet()) {
				if (entry.getValue() > highestCount) {
					highestCount = entry.getValue();
					highestName = entry.getKey();
				}
			}
			ClassNode classNode = new ClassNode();
			classNode.className = highestName;
			return classNode;
		}

		Map<String, Integer> targetValuesFrequency = computeFrequency(instances, targetIndex);

		if (targetValuesFrequency.entrySet().size() == 1) {
			ClassNode classNode = new ClassNode();
			classNode.className = (String) targetValuesFrequency.keySet().toArray()[0];
			return classNode;
		}

		double entropy = 0d;
		for (String value : targetValues) {
			Integer frequencyInt = targetValuesFrequency.get(value);
			if (frequencyInt != null) {
				double frequencyDouble = frequencyInt / (double) instances.size();
				entropy -= frequencyDouble * Math.log(frequencyDouble) / Math.log(2);
			}
		}

		int attributeWithHighestGain = 0;
		double highestGain = -99999;
		for (int attribute : otherAttributes) {
			double gain = computeIncrease(attribute, instances, entropy);
			if (gain >= highestGain) {
				highestGain = gain;
				attributeWithHighestGain = attribute;
			}
		}

		if (highestGain == 0) {
			ClassNode classNode = new ClassNode();
			int topFrequency = 0;
			String className = null;
			for (Entry<String, Integer> entry : targetValuesFrequency.entrySet()) {
				if (entry.getValue() > topFrequency) {
					topFrequency = entry.getValue();
					className = entry.getKey();
				}
			}
			classNode.className = className;
			return classNode;
		}

		DecisionNode decisionNode = new DecisionNode();
		decisionNode.attribute = attributeWithHighestGain;

		int[] newRemainingAttribute = new int[otherAttributes.length - 1];
		int pos = 0;
		for (int i = 0; i < otherAttributes.length; i++) {
			if (otherAttributes[i] != attributeWithHighestGain) {
				newRemainingAttribute[pos++] = otherAttributes[i];
			}
		}

		Map<String, List<String[]>> partitions = new HashMap<String, List<String[]>>();
		for (String[] instance : instances) {
			String value = instance[attributeWithHighestGain];
			List<String[]> listInstances = partitions.get(value);
			if (listInstances == null) {
				listInstances = new ArrayList<String[]>();
				partitions.put(value, listInstances);
			}
			listInstances.add(instance);
		}

		decisionNode.nodes = new Node[partitions.size()];
		decisionNode.attributeValues = new String[partitions.size()];
		int index = 0;
		for (Entry<String, List<String[]>> partition : partitions.entrySet()) {
			decisionNode.attributeValues[index] = partition.getKey();
			decisionNode.nodes[index] = createTree(newRemainingAttribute, partition.getValue());
			index++;
		}
		return decisionNode;
	}

	private double computeIncrease(int attributeIndex, List<String[]> instances, double entropy) {
		Map<String, Integer> valuesFrequency = computeFrequency(instances, attributeIndex);
		double sum = 0;
		for (Entry<String, Integer> entry : valuesFrequency.entrySet()) {
			sum += entry.getValue() / ((double) instances.size())
					* computeEntropy(instances, attributeIndex, entry.getKey());
		}
		return entropy - sum;
	}

	private double computeEntropy(List<String[]> instances, int attributeIF, String valueIF) {

		int instancesCount = 0;
		Map<String, Integer> valuesFrequency = new HashMap<String, Integer>();
		for (String[] instance : instances) {
			if (instance[attributeIF].equals(valueIF)) {
				String targetValue = instance[targetIndex];
				if (valuesFrequency.get(targetValue) == null) {
					valuesFrequency.put(targetValue, 1);
				} else {
					valuesFrequency.put(targetValue, valuesFrequency.get(targetValue) + 1);
				}
				instancesCount++;
			}
		}
		double entropy = 0;
		for (String value : targetValues) {
			Integer count = valuesFrequency.get(value);
			if (count != null) {
				double frequency = count / (double) instancesCount;
				entropy -= frequency * Math.log(frequency) / Math.log(2);
			}
		}
		return entropy;
	}
	private Map<String, Integer> computeFrequency(List<String[]> instances, int indexAttribute) {
		Map<String, Integer> targetValuesFrequency = new HashMap<String, Integer>();
		for (String[] instance : instances) {
			String targetValue = instance[indexAttribute];
			if (targetValuesFrequency.get(targetValue) == null) {
				targetValuesFrequency.put(targetValue, 1);
			} else {
				targetValuesFrequency.put(targetValue, targetValuesFrequency.get(targetValue) + 1);
			}
		}
		return targetValuesFrequency;
	}

	public void print() {
		System.out.println("Target attribute = " + allAttributes[targetIndex]);
		System.out.print("Other attributes = ");
		for (String attribute : allAttributes) {
			if (!attribute.equals(allAttributes[targetIndex])) {
				System.out.print(attribute + " ");
			}
		}
		System.out.println();
	}
}
