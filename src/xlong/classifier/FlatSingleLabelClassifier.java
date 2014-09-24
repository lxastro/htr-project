package xlong.classifier;

import weka.core.Instance;
import xlong.classifier.converter.SparseVectorSampleToWekaInstanceConverter;
import xlong.sample.Composite;
import xlong.sample.Property;
import xlong.sample.Sample;

public class FlatSingleLabelClassifier extends AbstractClassifier {
	
	private weka.classifiers.Classifier wekaClassifier;
	private SparseVectorSampleToWekaInstanceConverter converter;
	
	public FlatSingleLabelClassifier(int numOfAtt) {
		wekaClassifier = new weka.classifiers.bayes.NaiveBayesMultinomial();
		//wekaClassifier = new weka.classifiers.functions.SMO();
		converter = new SparseVectorSampleToWekaInstanceConverter(numOfAtt);
	}
	
	@Override
	public int getNumOfClass() {
		return converter.getNumOfClass();
	}

	@Override
	public void train(Composite composite) throws Exception {
		for (Sample sample:composite.getSamples()) {
			converter.addSample(sample);
		}
		wekaClassifier.buildClassifier(converter.getInstances());
	}

	@Override
	public double[] getDistribution(Sample sample) throws Exception {
		Instance instance = converter.convertSample(sample);
		return wekaClassifier.distributionForInstance(instance);
	}

	@Override
	public double[] getDistribution(Property property) throws Exception {
		Sample sample = new Sample(property);
		return getDistribution(sample);
	}

	@Override
	public int label2id(String label) {
		return converter.label2id(label);
	}

}
