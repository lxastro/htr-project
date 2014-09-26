package xlong.classifier;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import weka.core.Instances;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordTokenizer;

public class StuckPachinkoSingleLabelClassifier extends AbstractClassifier  {
	private int numOfFeatures;
	private Map<String, weka.classifiers.Classifier> selecters;
	private Map<String, weka.classifiers.Classifier> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	
	public StuckPachinkoSingleLabelClassifier(int numOfFeatures) {
		this.numOfFeatures = numOfFeatures;
		selecters = new TreeMap<String, weka.classifiers.Classifier>();
		stuckers = new TreeMap<String, weka.classifiers.Classifier>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		selectAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();	
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		train(composite.getLabel().getText(), composite);
		for (Composite subcomp:composite.getComposites()) {
			train(subcomp);
		}
	}
	
	private void train(String label, Composite composite) throws Exception {
		if (composite.getComposites().size() == 0) {
			selecters.put(label, null);
			return;
		}
		System.out.println(label);
		if (composite.getSamples().size() == 0) {
			stuckers.put(label, null);
		} else {
			weka.classifiers.Classifier stucker = new weka.classifiers.bayes.NaiveBayesMultinomial();
			
			TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer(), numOfFeatures);
			System.out.println("build stucker dictionary...");
			converter.buildDictionary(composite);
			System.out.println("determine stucker dictionary...");
			converter.determineDictionary();
			System.out.println(converter.getDictionary().size());
			System.out.println("convert stucker...");
			Composite vecComposite = converter.convert(composite);
			
			int numOfAtts = converter.getDictionary().size();
			Vector<String> labels = new Vector<String>();
			labels.add("neg"); labels.add("pos");
			SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, labels);
			Instances instances = adapter.getDataSet();
			for (Sample sample:vecComposite.getSamples()) {
				instances.add(adapter.adaptSample(sample, "pos"));
			}
			for (Composite subcomp:vecComposite.getComposites()) {
				addAll(instances, adapter, subcomp, "neg");
			}
			
			System.out.println("train stucker...");
			stucker.buildClassifier(instances);
			
			stuckers.put(label, stucker);
			stuckAdapters.put(label, adapter);
			stuckConverters.put(label, converter);
		}
		
		weka.classifiers.Classifier selecter = new weka.classifiers.bayes.NaiveBayesMultinomial();
		
		TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer(), numOfFeatures);
		
		Vector<String> labels = new Vector<String>();
		System.out.println("build selecter dictionary...");
		for (Composite subcomp:composite.getComposites()) {
			labels.add(subcomp.getLabel().getText());
			converter.buildDictionary(subcomp);
		}
		System.out.println(labels.size());
		System.out.println("determine selecter dictionary...");
		converter.determineDictionary();
		System.out.println(converter.getDictionary().size());
		
		System.out.println("convert selecter...");
		int numOfAtts = converter.getDictionary().size();
		SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, labels);
		Instances instances = adapter.getDataSet();
		for (Composite subcomp:composite.getComposites()) {
			addAll(instances, adapter, converter.convert(subcomp),subcomp.getLabel().getText());
		}
		
		System.out.println("train selecter...");
		selecter.buildClassifier(instances);
		
		selecters.put(label, selecter);
		selectAdapters.put(label, adapter);
		selectConverters.put(label, converter);		
	}

	private void addAll(Instances instances, SparseVectorSampleToWekaInstanceAdapter adapter, Composite composite, String label) {
		for (Sample sample:composite.getSamples()) {
			instances.add(adapter.adaptSample(sample, label));
		}
		for (Composite subcomp:composite.getComposites()) {
			addAll(instances, adapter, subcomp, label);
		}
	}
	
	@Override
	public String test(Sample sample) throws Exception {
		return test("root", sample);
	}
	
	private String test(String label, Sample sample) throws Exception {
		weka.classifiers.Classifier selecter = selecters.get(label);
		if (selecter == null) {
			return label;
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			int classID = (int)(stucker.classifyInstance(adapter.adaptSample(vecSample)) + 0.5);
			String str = adapter.getDataSet().classAttribute().value(classID);
			if (str.equals("pos")) {
				return label;
			}
		}
		TextToSparseVectorConverter converter = selectConverters.get(label);
		Sample vecSample = converter.convert(sample);
		SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
		int classID = (int)(selecter.classifyInstance(adapter.adaptSample(vecSample)) + 0.5);
		String subLabel = adapter.getDataSet().classAttribute().value(classID);
		//System.out.println(str);
		return test(subLabel, sample);
	}

}
