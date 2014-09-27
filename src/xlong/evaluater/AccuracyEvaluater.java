package xlong.evaluater;

import xlong.classifier.Classifier;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.util.MyWriter;

public class AccuracyEvaluater extends Evaluater {

	private int correct;
	
	public AccuracyEvaluater(Classifier classifier) {
		super(classifier);
	}

	private void evaluate(Sample sample, String label) throws Exception {
		String result = classifier.test(sample);
		
		MyWriter.writeln(sample.getProperty().getOneLineString());
		MyWriter.write(label + " ");
		MyWriter.writeln(result);
		
		if (result.equals(label)) {
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
			evaluate(sample, composite.getLabel().getText());
		}
		for (Composite sub:composite.getComposites()) {
			evaluate(sub);
		}
	}

}