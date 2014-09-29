package xlong.classifier;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import weka.core.Instances;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.data.tokenizer.SingleWordTokenizer;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;

public class StuckPachinkoSVMClassifier extends AbstractSingleLabelClassifier  {
	private int numOfFeatures;
	private Map<String, weka.classifiers.Classifier> selecters;
	private Map<String, weka.classifiers.Classifier> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	private Map<String, TreeSet<String>> sons;
	//private static final String OPTION = "-M";

	public StuckPachinkoSVMClassifier() {
		selecters = new TreeMap<String,  weka.classifiers.Classifier>();
		stuckers = new TreeMap<String,  weka.classifiers.Classifier>();
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
		System.out.println(label);
		if (composite.getSamples().size() == 0) {
			stuckers.put(label, null);
		} else {
			weka.classifiers.Classifier stucker = newClassifier();
			
			TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer());
			//System.out.println("build stucker dictionary...");
			converter.buildDictionary(composite);
			//System.out.println("determine stucker dictionary...");
			converter.determineDictionary();
			//System.out.println(converter.getDictionary().size());
			//System.out.println("convert stucker...");
			Composite vecComposite = converter.convert(composite);
			
			int numOfAtts = converter.dictionarySize();
			Vector<String> tags = new Vector<String>();
			tags.add("neg"); tags.add("pos");
			SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, tags);
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
		TreeSet<String> sublabels = new TreeSet<String>();
		for (Composite subcomp:composite.getComposites()) {
			sublabels.add(subcomp.getLabel().getText());
			System.out.println("subcomp: " + subcomp.getLabel().getText());
			
			weka.classifiers.Classifier selecter = newClassifier();
			
			TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer());
			
			Vector<String> tags = new Vector<String>();
			tags.add("neg"); tags.add("pos");
			
			//System.out.println("build selecter dictionary...");
			for (Composite subcompAll:composite.getComposites()) {
				converter.buildDictionary(subcompAll);
			}
			//System.out.println("determine selecter dictionary...");
			converter.determineDictionary();
			//System.out.println(converter.getDictionary().size());
			
			//System.out.println("convert selecter...");
			int numOfAtts = converter.dictionarySize();
			SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, tags);
			Instances instances = adapter.getDataSet();
			for (Composite subcompOther:composite.getComposites()) {
				if (subcompOther.getLabel().compareTo(subcomp.getLabel()) != 0) {
					addAll(instances, adapter, converter.convert(subcomp), "neg");
				} else {
					addAll(instances, adapter, converter.convert(subcomp), "pos");
				}
			}
			System.out.println("train selecter...");
			System.out.println(instances.classAttribute().numValues());
			selecter.buildClassifier(instances);
			
			selecters.put(subcomp.getLabel().getText(), selecter);
			selectAdapters.put(subcomp.getLabel().getText(), adapter);
			selectConverters.put(subcomp.getLabel().getText(), converter);		
		}
		sons.put(label, sublabels);
	}
	
	private weka.classifiers.Classifier newClassifier() throws Exception {
		weka.classifiers.functions.SMO classifier = new weka.classifiers.functions.SMO();
		//classifier.setOptions(weka.core.Utils.splitOptions(OPTION));	
		return classifier;
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
		TreeSet<String> subLabels = sons.get(label);
		if (subLabels == null) {
			return label;
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			int classID = (int)(stucker.classifyInstance(adapter.adaptSample(vecSample)) + 0.5);
			if (classID == 1) {
				return label;
			}
		}
		double maximum = -1;
		String maxLabel = null;
		for (String subLabel:subLabels) {
			weka.classifiers.Classifier selecter = selecters.get(subLabel);
			TextToSparseVectorConverter converter = selectConverters.get(subLabel);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(subLabel);
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			if (probs[1] > maximum) {
				maximum = probs[1];
				maxLabel = subLabel;
			}
		}
		return test(maxLabel, sample);
	}

}
