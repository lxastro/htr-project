package xlong.classifier;

import java.util.Map;
import java.util.TreeMap;

import javax.print.attribute.standard.Copies;

import weka.core.pmml.jaxbbindings.TrainingInstances;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.sample.Composite;
import xlong.sample.Property;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordTokenizer;

public class TreeSingleLabelClassifier extends AbstractClassifier  {

	private Map<String, weka.classifiers.Classifier> selects;
	private Map<String, TextToSparseVectorConverter> converters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> adapters;
	
	public TreeSingleLabelClassifier() {
		selects = new TreeMap<String, weka.classifiers.Classifier>();
		converters = new TreeMap<String, TextToSparseVectorConverter>();
		adapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
	}
	
	public void train(Composite composite) {
		initialize(composite);
	}

	private void initialize(Composite composite) {
		selects.put(composite.getLabel().toString(), new weka.classifiers.bayes.NaiveBayesMultinomial());
		converters.put(composite.getLabel().toString(), new TextToSparseVectorConverter(new SingleWordTokenizer(), 1000));
		//adapters.put(composite.getLabel().toString(), new SparseVectorSampleToWekaInstanceAdapter());
		for (Composite subComp:composite.getComposites()) {
			initialize(composite);
		}
	}


}
