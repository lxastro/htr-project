package xlong.evaluater;

import xlong.classifier.Classifier;
import xlong.sample.Sample;

public abstract class Evaluater {
	Classifier classifier;
	int numClass;
	
	public Evaluater(Classifier classifier) {
		this.classifier = classifier;
		this.numClass = classifier.getNumOfClass();
	}
	
	public abstract void evaluate(Sample sample) throws Exception;
	
	public abstract double getAccuracy();
	
}
