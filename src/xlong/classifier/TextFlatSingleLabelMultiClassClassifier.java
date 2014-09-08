package xlong.classifier;

import weka.core.Instance;
import xlong.classifier.converter.SparseVectorSampleToWekaInstanceConverter;
import xlong.classifier.converter.TextSampleToWekaInstanceConverter;
import xlong.sample.Composite;
import xlong.sample.Property;
import xlong.sample.Sample;

public class TextFlatSingleLabelMultiClassClassifier extends AbstractClassifier {
	
	private weka.classifiers.Classifier wekaClassifier;
	private TextSampleToWekaInstanceConverter converter;
	
	public TextFlatSingleLabelMultiClassClassifier(int numOfClass) {
		wekaClassifier = new weka.classifiers.bayes.NaiveBayesMultinomialText();
		converter = new TextSampleToWekaInstanceConverter(numOfClass);
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

}
