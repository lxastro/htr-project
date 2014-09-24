package xlong.classifier;

import xlong.sample.Composite;
import xlong.sample.Property;
import xlong.sample.Sample;

public interface Classifier {
	public void train(Composite composite) throws Exception;
	public double[] getDistribution(Sample sample) throws Exception;
	public double[] getDistribution(Property property) throws Exception;
	public int getNumOfClass();
	public int label2id(String label);
}
