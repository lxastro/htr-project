package xlong.classifier.converter;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import xlong.sample.Sample;
import xlong.sample.Text;

public class TextSampleToWekaInstanceConverter {
	private Instances instances;
	
	public TextSampleToWekaInstanceConverter(int numOfClass) {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> attVals = new ArrayList<String>(numOfClass);		
		for (int i = 0; i < numOfClass; i++) {
			attVals.add(String.valueOf(i));
		}
		atts.add(new Attribute("class", attVals));
		atts.add(new Attribute("url", (ArrayList<String>) null));
		instances = new Instances("", atts, 0);
		instances.setClassIndex(0);
	}
	
	public void addSample(Sample sample) {
		instances.add(convertSample(sample));
	}
	
	public Instance convertSample (Sample sample) {
		int classID = sample.getLabels().iterator().next().getID();
		return textToInstance((Text) sample.getProperty(), classID);
	}
	
	private DenseInstance textToInstance(Text text, int classID) {
		double[] vals;
		vals = new double[2];
		vals[0] = classID;
		vals[1] = instances.attribute(1).addStringValue(text.getText());
		return new DenseInstance(1.0, vals);
	}
	
	public Instances getInstances() {
		return instances;
	}
}
