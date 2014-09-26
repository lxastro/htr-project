package xlong.classifier.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import xlong.sample.Sample;
import xlong.sample.SparseVector;

public class SparseVectorSampleToWekaInstanceAdapter{
	private int numOfAttributes;
	private Map<String, Integer> labelMap;
	private Instances dataSet;
	
	// Just need to save numOfAttributes, pos and labelMap. Create dataSet use initInstances.
	
	/**
	 * @param numOfAtts
	 * @param labels
	 * @param pos null means multiClass, else means binaryClass
	 */
	public SparseVectorSampleToWekaInstanceAdapter(int numOfAtts, Collection<String> labels) {
		// get number of attributes
		numOfAttributes = numOfAtts;
		// get labels
		labelMap = new TreeMap<String, Integer>();
		for (String label:labels){
			if (!labelMap.containsKey(label)) {
				int n = labelMap.size();
				labelMap.put(label, new Integer(n));
			}			
		}
		// get dataSet
		dataSet = initInstances(labelMap);
	}
	
	private Instances initInstances(Map<String, Integer> labelMap) {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> attVals = new ArrayList<String>(labelMap.keySet());
		for (Entry<String, Integer> en:labelMap.entrySet()) {
			attVals.set(en.getValue(), en.getKey());
		}
		attVals.add("LX_NaN");
		atts.add(new Attribute("class", attVals));
		for (int i = 0; i < numOfAttributes; i++) {
			atts.add(new Attribute(String.valueOf(i)));
		}
		Instances instances = new Instances("", atts, 0);
		instances.setClassIndex(0);
		return instances;
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
	
	public Instances getDataSet() {
		return dataSet;
	}
	
	public Instance adaptSample(Sample sample, String label) {
		int classID;
		if (!labelMap.containsKey(label)) {
			classID = labelMap.size();
		} else {
			classID = labelMap.get(label);
		}
		Instance instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), classID);
		instance.setDataset(dataSet);
		return instance;
	}
	
	public Instance adaptSample(Sample sample) {
		return adaptSample(sample, "LX_NaN");
	}
	
	public int getNumOfClasses() {
		return labelMap.size() + 1;
	}
}
