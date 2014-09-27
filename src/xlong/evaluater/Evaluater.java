package xlong.evaluater;

import xlong.classifier.SingleLabelClassifier;
import xlong.sample.Composite;

public abstract class Evaluater {
	SingleLabelClassifier singleLabelClassifier;
    int total;
	
	public Evaluater(SingleLabelClassifier singleLabelClassifier) {
		this.singleLabelClassifier = singleLabelClassifier;
	}
	
	public abstract void evaluate(Composite composite) throws Exception;
	
	public abstract double getAccuracy();
	
	public int getTotal() {
		return total;
	}
}
