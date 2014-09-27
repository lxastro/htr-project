package xlong.classifier;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Vector;


import weka.classifiers.Classifier;
import weka.core.Instances;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordTokenizer;

public class StuckBeamSearchSingleLabelClassifier extends AbstractClassifier  {
	private int numOfFeatures;
	private Map<String, weka.classifiers.Classifier> selecters;
	private Map<String, weka.classifiers.Classifier> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	private static final String CLASSIFIER_STRING = "weka.classifiers.bayes.NaiveBayesMultinomial";
	private int beamWidth;
	
	public StuckBeamSearchSingleLabelClassifier(int numOfFeatures, int beamWidth) {
		this.numOfFeatures = numOfFeatures;
		this.beamWidth = beamWidth;
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
			weka.classifiers.Classifier stucker = (Classifier) Class.forName(CLASSIFIER_STRING).newInstance();
			
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
		
		weka.classifiers.Classifier selecter = (Classifier) Class.forName(CLASSIFIER_STRING).newInstance();
		
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
		return test("root", sample).peek().label;
	}
	
	class Pair{
		String label;
		double prob;
		public Pair(String label, double prob) {
			this.label = label;
			this.prob = prob;
		}
	}
	
	class PairComp implements Comparator<Pair> {

		@Override
		public int compare(Pair o1, Pair o2) {
			// TODO Auto-generated method stub
			return -(int)Math.signum(o1.prob-o2.prob);
		}
		
	}
	
	private PriorityQueue<Pair> test(String label, Sample sample) throws Exception {
		weka.classifiers.Classifier selecter = selecters.get(label);
		PriorityQueue<Pair> pairs = new PriorityQueue<Pair>(new PairComp());
		if (selecter == null) {
			pairs.add(new Pair(label, 1.0));
			return pairs;
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		double unstuckProb = 1.0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			double[] probs = stucker.distributionForInstance(adapter.adaptSample(vecSample));
			String str = adapter.getDataSet().classAttribute().value(0);
			if (str.equals("pos")) {
				pairs.add(new Pair(label, probs[0]));
				unstuckProb = probs[1];
			} else {
				pairs.add(new Pair(label, probs[1]));
				unstuckProb = probs[0];				
			}
		}
		TextToSparseVectorConverter converter = selectConverters.get(label);
		Sample vecSample = converter.convert(sample);
		SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
		double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
		int n = probs.length - 1;
		PriorityQueue<Pair> nowPairs = new PriorityQueue<Pair>(new PairComp());
		for (int i = 0; i < n; i++) {
			String subLabel = adapter.getDataSet().classAttribute().value(i);
			nowPairs.add(new Pair(subLabel, probs[i]));
		}
		for (int i = 0; i < beamWidth; i++) {
			if (!nowPairs.isEmpty()) {
				Pair exPair = nowPairs.poll();
				PriorityQueue<Pair> subPairs = test(exPair.label, sample);
				for (Pair pair:subPairs) {
					pairs.add(new Pair(pair.label, unstuckProb * exPair.prob * pair.prob));
				}
			} else {
				break;
			}
		}
		if (pairs.size() <= beamWidth) {
			return pairs;
		}
		PriorityQueue<Pair> newPairs = new PriorityQueue<Pair>(new PairComp());
		for (int i = 0; i < beamWidth; i++) {
			if (!pairs.isEmpty()) {
				newPairs.add(pairs.poll());
			} else {
				break;
			}
		}
		return newPairs;
	}
	
}
