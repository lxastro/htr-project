package xlong.classifier;

import xlong.sample.Composite;
import xlong.sample.Property;
import xlong.sample.Sample;

public interface Classifier {
	void train(Composite composite) throws Exception;
	double[] getDistribution(Sample sample) throws Exception;
	double[] getDistribution(Property property) throws Exception;
}
