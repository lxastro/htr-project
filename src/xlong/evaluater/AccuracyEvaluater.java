package xlong.evaluater;

import xlong.classifier.Classifier;
import xlong.sample.Composite;
import xlong.sample.Sample;

public class AccuracyEvaluater extends Evaluater {

	private int correct;
	
	
	public AccuracyEvaluater(Classifier classifier) {
		super(classifier);
	}

	@Override
	public void evaluate(Sample sample) throws Exception {
		String result = classifier.test(sample);
		String trueClass = sample.getLabels().iterator().next().getText();
		if (result.equals(trueClass)) {
			correct += 1;
		}
		total += 1;

	}

	@Override
	public double getAccuracy() {
		return ((double)correct)/total;
	}

	@Override
	public void evaluate(Composite composite) throws Exception {
		for (Sample sample:composite.getSamples()) {
			evaluate(sample);
		}
		for (Composite sub:composite.getComposites()) {
			evaluate(sub);
		}
	}

}
