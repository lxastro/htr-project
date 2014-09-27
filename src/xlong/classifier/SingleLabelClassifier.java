package xlong.classifier;

import xlong.sample.Composite;
import xlong.sample.Sample;

public interface SingleLabelClassifier {
	public void train(Composite composite) throws Exception;
	public String test(Sample sample) throws Exception;
}
