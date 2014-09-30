package xlong.wm.classifier;

import java.util.TreeMap;
import java.util.TreeSet;

import xlong.wm.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Sample;
import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class StuckPachinkoMultiBaseClassifier extends StuckTopDownMultiBaseClassifier  {
	
	public StuckPachinkoMultiBaseClassifier(ClassifierPartsFactory factory) {
		super(factory);
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
		return test("root", sample);
	}
	
	private String test(String label, Sample sample) throws Exception {
		
		if (sons.get(label) == null) {
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
		if (sons.get(label).size() == 1) {
			return test(sons.get(label).first(), sample);
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			int classID = (int)(selecter.classifyInstance(adapter.adaptSample(vecSample)) + 0.5);
			String subLabel = adapter.getDataSet().classAttribute().value(classID);
			//System.out.println(str);
			return test(subLabel, sample);
		}
	}

}
