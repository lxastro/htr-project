package xlong.classifier.adapter;

import java.util.ArrayList;



import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import xlong.sample.Label;
import xlong.sample.Sample;
import xlong.sample.SparseVector;

public class SparseVectorSampleToWekaInstanceAdapter{
	private int numOfAttributes;
	private Vector<Instance> instanceVec;
	private Map<String, Integer> labelMap;
	private Instances instances;
	
	public SparseVectorSampleToWekaInstanceAdapter(int numOfAtt) {
		numOfAttributes = numOfAtt;
		labelMap = new TreeMap<String, Integer>();
		instanceVec = new Vector<Instance>();
	}
	
	public int label2id(String label) {
		if (labelMap.containsKey(label)) {
			return labelMap.get(label);
		} else {
			return labelMap.size();
		}
	}
	
	private SparseInstance sparseVectorToSparseInstance(SparseVector vec, int classID) {
		int len = vec.getIndexs().length;
		int[] idx = new int[len + 1];
		double[] val = new double[len + 1];
		idx[0] = 0;
		val[0] = classID;
		System.arraycopy(vec.getIndexs(), 0, idx, 1, len);
		System.arraycopy(vec.getValues(), 0, val, 1, len);
		return new SparseInstance(1, val, idx, numOfAttributes + 1);
	}
	
	public void addSample(Sample sample) {
		String label = sample.getLabels().iterator().next().getText();
		if (!labelMap.containsKey(label)) {
			int n = labelMap.size();
			labelMap.put(label, new Integer(n));
		}
		Instance instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), labelMap.get(label));
		instanceVec.add(instance);
	}
	
	public Instance convertSample (Sample sample) {
		String label = sample.getLabels().iterator().next().getText();
		Instance instance;
		if (!labelMap.containsKey(label)) {
			instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), labelMap.size());
		} else {
			instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), labelMap.get(label));
		}
		instance.setDataset(instances);
		return instance;
	}
	
	public void addSample(Sample sample, Label pos) {
		int classID;
		if (sample.containLabel(pos)) {
			classID = 1;
		} else {
			classID = 0;
		}
		labelMap.put(String.valueOf(classID), classID);
		instanceVec.add(sparseVectorToSparseInstance((SparseVector) sample.getProperty(), classID));
	}
	
	public Instance convertSample (Sample sample, Label pos) {
		int classID;
		if (sample.containLabel(pos)) {
			classID = 1;
		} else {
			classID = 0;
		}
		Instance instance;
		if (!labelMap.containsKey(String.valueOf(classID))) {
			instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), labelMap.size());
		} else {	
			instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), classID);
		}
		instance.setDataset(instances);
		return instance;
	}
	
	public Instances getInstances() {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		
		ArrayList<String> attVals = new ArrayList<String>(labelMap.keySet());
		for (Entry<String, Integer> en:labelMap.entrySet()) {
			attVals.set(en.getValue(), en.getKey());
		}
		attVals.add("Lx_others");

		atts.add(new Attribute("class", attVals));
		
		for (int i = 0; i < numOfAttributes; i++) {
			atts.add(new Attribute(String.valueOf(i)));
		}
		instances = new Instances("", atts, 0);
		instances.setClassIndex(0);
		
		for (Instance instance:instanceVec) {
			instance.setDataset(instances);
			instances.add(instance);
		}
		
		return instances;
	}
	
	public int getNumOfClass() {
		return labelMap.size() + 1;
	}
}
