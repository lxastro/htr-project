package xlong.converter;

import java.util.ArrayList;


import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
import xlong.sample.Label;
import xlong.sample.Sample;
import xlong.sample.SparseVector;

public class SampleToWekaInstanceConverter {
	
	private int numOfAttributes;
	private Instances instances;
	
	public SampleToWekaInstanceConverter(int numOfAtt) {
		numOfAttributes = numOfAtt;
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		
		ArrayList<String> attVals = new ArrayList<String>(2);
		attVals.add("false");
		attVals.add("true");
		atts.add(new Attribute("class", attVals));
		
		for (int i = 0; i < numOfAtt; i++) {
			atts.add(new Attribute(String.valueOf(i)));
		}
		instances = new Instances("", atts, 0);
		instances.setClassIndex(0);
	}
	
	private SparseInstance sparseVectorToSparseInstance(SparseVector vec, boolean classPos) {
		int len = vec.getIndexs().length;
		int[] idx = new int[len + 1];
		double[] val = new double[len + 1];
		idx[0] = 0;
		if (classPos) {
			val[0] = 1;
		} else {
			val[0] = 0;
		}
		System.arraycopy(vec.getIndexs(), 0, idx, 1, len);
		System.arraycopy(vec.getValues(), 0, val, 1, len);
		return new SparseInstance(1, val, idx, numOfAttributes + 1);
	}
	
	public void addSample(Sample sample, Label pos) {
		boolean classPos = sample.containLabel(pos);
		instances.add(sparseVectorToSparseInstance((SparseVector) sample.getProperty(), classPos));
	}
	
	public Instances getInstances() {
		return instances;
	}
}
