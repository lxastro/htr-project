package xlong.evaluater;

import xlong.classifier.Classifier;
import xlong.sample.Composite;

public abstract class Evaluater {
	Classifier classifier;
    int total;
	
	public Evaluater(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public abstract void evaluate(Composite composite) throws Exception;
	
	public abstract double getAccuracy();
	
	public int getTotal() {
		return total;
	}
}
