package xlong.classifier;

import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import xlong.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.sample.Sample;
import xlong.sample.converter.TextToSparseVectorConverter;

public class StuckBeamSearchNBMClassifier extends StuckTopDownNBMClassifier  {
	private int beamWidth;
	
	public StuckBeamSearchNBMClassifier(int numOfFeatures, int stemmingMode, int beamWidth) {
		super(numOfFeatures, stemmingMode);
		this.numOfFeatures = numOfFeatures;
		this.beamWidth = beamWidth;
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
		return test("root", sample).peek().label;
	}
	
	private PriorityQueue<Pair> test(String label, Sample sample) throws Exception {
		PriorityQueue<Pair> pairs = new PriorityQueue<Pair>(new PairComp());
		if (sons.get(label) == null) {
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
		if (sons.get(label).size() == 1) {
			PriorityQueue<Pair> subPairs = test(sons.get(label).first(), sample);
			for (Pair pair:subPairs) {
				pairs.add(new Pair(pair.label, unstuckProb * 1.0 * pair.prob));
			}
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			int n = probs.length;
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
