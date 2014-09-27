package xlong.classifier;

import java.util.Collection;

import xlong.sample.Composite;
import xlong.sample.Sample;

public interface MultiLabelClassifier {
	public void train(Composite composite) throws Exception;
	public Collection<String> test(Sample sample) throws Exception;
}
