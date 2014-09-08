package xlong.classifier.converter;

import java.util.ArrayList;



import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import xlong.sample.Label;
import xlong.sample.Sample;
import xlong.sample.SparseVector;

public class SparseVectorSampleToWekaInstanceConverter{
	
	private int numOfAttributes;
	private Instances instances;
	
	public SparseVectorSampleToWekaInstanceConverter(int numOfAtt, int numOfClass) {
		numOfAttributes = numOfAtt;
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		
		ArrayList<String> attVals = new ArrayList<String>(numOfClass);
		for (int i = 0; i < numOfClass; i++) {
			attVals.add(String.valueOf(i));
		}
		atts.add(new Attribute("class", attVals));
		
		for (int i = 0; i < numOfAtt; i++) {
			atts.add(new Attribute(String.valueOf(i)));
		}
		instances = new Instances("", atts, 0);
		instances.setClassIndex(0);
	}
	
	public SparseVectorSampleToWekaInstanceConverter(int numOfAtt) {
		this(numOfAtt, 2);
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
		instances.add(convertSample(sample));
	}
	
	public Instance convertSample (Sample sample) {
		int classID = sample.getLabels().iterator().next().getID();
		Instance instance = sparseVectorToSparseInstance((SparseVector) sample.getProperty(), classID);
		instance.setDataset(instances);
		return instance;
	}
	
	public void addSample(Sample sample, Label pos) {
		instances.add(convertSample(sample, pos));
	}
	
	public Instance convertSample (Sample sample, Label pos) {
		int classID;
		if (sample.containLabel(pos)) {
			classID = 1;
		} else {
			classID = 0;
		}
		return sparseVectorToSparseInstance((SparseVector) sample.getProperty(), classID);
	}
	
	public Instances getInstances() {
		return instances;
	}
}
