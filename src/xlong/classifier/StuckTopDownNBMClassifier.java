package xlong.classifier;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordStemmingTokenizer;
import xlong.sample.tokenizer.Tokenizer;
//import xlong.sample.tokenizer.SingleWordTokenizer;

public abstract class StuckTopDownNBMClassifier extends AbstractSingleLabelClassifier  {
	protected int numOfFeatures;
	protected Map<String, weka.classifiers.Classifier> selecters;
	protected Map<String, weka.classifiers.Classifier> stuckers;
	protected Map<String, TextToSparseVectorConverter> selectConverters;
	protected Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	protected Map<String, TextToSparseVectorConverter> stuckConverters;
	protected Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	protected Map<String, TreeSet<String>> sons;
	protected static final String CLASSIFIER_STRING = "weka.classifiers.bayes.NaiveBayesMultinomial";
	protected final Tokenizer tokenizer;
	
	public StuckTopDownNBMClassifier(int numOfFeatures, int StemmingMode) {
		this.numOfFeatures = numOfFeatures;
		tokenizer = new SingleWordStemmingTokenizer(StemmingMode);
		selecters = new TreeMap<String, weka.classifiers.Classifier>();
		stuckers = new TreeMap<String, weka.classifiers.Classifier>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		selectAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();	
		sons = new TreeMap<String, TreeSet<String>>();
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
			sons.put(label, null);
			return;
		}
		//System.out.println(label);
		if (composite.getSamples().size() == 0) {
			stuckers.put(label, null);
		} else {
			weka.classifiers.Classifier stucker = (Classifier) Class.forName(CLASSIFIER_STRING).newInstance();
			
			TextToSparseVectorConverter converter = new TextToSparseVectorConverter(tokenizer, numOfFeatures);
			//System.out.println("build stucker dictionary...");
			converter.buildDictionary(composite);
			//System.out.println("determine stucker dictionary...");
			converter.determineDictionary();
			//System.out.println(converter.getDictionary().size());
			//System.out.println("convert stucker...");
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
			
			//System.out.println("train stucker...");
			stucker.buildClassifier(instances);
			
			stuckers.put(label, stucker);
			stuckAdapters.put(label, adapter);
			stuckConverters.put(label, converter);
		}
		
		weka.classifiers.Classifier selecter = (Classifier) Class.forName(CLASSIFIER_STRING).newInstance();
		
		TextToSparseVectorConverter converter = new TextToSparseVectorConverter(tokenizer, numOfFeatures);
		
		Vector<String> labels = new Vector<String>();
		//System.out.println("build selecter dictionary...");
		for (Composite subcomp:composite.getComposites()) {
			labels.add(subcomp.getLabel().getText());
			converter.buildDictionary(subcomp);
		}
		sons.put(label, new TreeSet<String>(labels));
		if (labels.size() == 1) {
			return;
		}
		//System.out.println(labels.size());
		//System.out.println("determine selecter dictionary...");
		converter.determineDictionary();
		//System.out.println(converter.getDictionary().size());
		
		//System.out.println("convert selecter...");
		int numOfAtts = converter.getDictionary().size();
		SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, labels);
		Instances instances = adapter.getDataSet();
		for (Composite subcomp:composite.getComposites()) {
			addAll(instances, adapter, converter.convert(subcomp),subcomp.getLabel().getText());
		}
		
		//System.out.println("train selecter...");
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
}