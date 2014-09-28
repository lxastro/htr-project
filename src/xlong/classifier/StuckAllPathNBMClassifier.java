package xlong.classifier;

import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;

public class StuckAllPathNBMClassifier extends StuckTopDownNBMClassifier {
	
	public StuckAllPathNBMClassifier(int numOfFeatures, int stemmingMode) {
		super(numOfFeatures, stemmingMode);
		this.numOfFeatures = numOfFeatures;
		selecters = new TreeMap<String, weka.classifiers.Classifier>();
		stuckers = new TreeMap<String, weka.classifiers.Classifier>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		selectAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();	
		sons = new TreeMap<String, TreeSet<String>>();
	}
	
	@Override
	public String test(Sample sample) throws Exception {
		return test("root", sample).label;
	}
	
	private Pair test(String label, Sample sample) throws Exception {
		
		PriorityQueue<Pair> pairs = new PriorityQueue<Pair>(new PairComp());
		if (sons.get(label) == null) {
			return new Pair(label, 1.0);
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
		if (sons.get(label).size() == 1) {
			Pair subPair = test(sons.get(label).first(), sample);
			pairs.add(new Pair(subPair.label, unstuckProb * 1.0 * subPair.prob));
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			int n = probs.length;
			for (int i = 0; i < n; i++) {
				String subLabel = adapter.getDataSet().classAttribute().value(i);
				Pair subPair = test(subLabel, sample);
				pairs.add(new Pair(subPair.label, unstuckProb * probs[i] * subPair.prob));
			}
		}
		
		return pairs.poll();		
	}
	
}
