package xlong.main;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import xlong.classifier.Classifier;
import xlong.classifier.FlatSingleLabelMultiClassClassifier;
import xlong.data.IO.UrlMapIO;
import xlong.evaluater.Evaluater;
import xlong.evaluater.SingleLabelEvaluater;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.Labels;
import xlong.sample.SparseVectors;
import xlong.sample.Text;
import xlong.sample.Texts;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordTokenizer;
import xlong.util.PropertiesUtil;

public class FlatClassify {
	public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.init();
		PropertiesUtil.loadProperties();
		PropertiesUtil.getProperty("DBpedia_instance_types.nt");
		PropertiesUtil.getProperty("DBpedia_external_links.nt");
		PropertiesUtil.getProperty("DBpedia_ontology.owl");
		HashMap<String, TreeSet<String>> urlMap;
		Composite flatComposite;
		TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer(), 100000);
		
//		urlMap = UrlMapIO.read("result/UrlMap.txt");
//		System.out.println(urlMap.size());
//	
//		flatComposite = new Composite();
//		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
//			Sample instance = new Sample(new Text(en.getKey()), Labels.getLabels(en.getValue()));
//			flatComposite.addSample(instance);
//		}
//		System.out.println(flatComposite.countSample());
//		flatComposite.save("result/flatText");
//		
//		flatComposite = new Composite("result/flatText", new Texts());
//		System.out.println(flatComposite.countSample());
//		converter.buildDictionary(flatComposite);
//		System.out.println("determine...");
//		converter.determineDictionary();
//		System.out.println("convert...");
//		flatComposite = converter.convert(flatComposite);
//		System.out.println(flatComposite.countSample());
//		flatComposite.save("result/flat");
//		converter.save("result/dictionary");
		
		converter = TextToSparseVectorConverter.load("result/dictionary");
		flatComposite = new Composite("result/flat", new SparseVectors());
		Vector<Composite> composites = flatComposite.split(new int[] {70, 30}, new Random());
		Composite train = composites.get(0);
		Composite test = composites.get(1);
		
		System.out.println(flatComposite.countSample());
		System.out.println(train.countSample());
		System.out.println(test.countSample());
		System.out.println(converter.getDictionary().size());
		
		Classifier classifier = new FlatSingleLabelMultiClassClassifier(converter.getDictionary().size());
		System.out.println("Train...");
		classifier.train(train);
		System.out.println(classifier.getNumOfClass());
		
		System.out.println("Evaluate...");
		Evaluater evaluater = new SingleLabelEvaluater(classifier);
		int cnt = 0;
		for (Sample sample:test.getSamples()) {
			cnt ++;
			if (cnt % 100000 == 0) {
				System.out.println(cnt);
			}
			evaluater.evaluate(sample);
		}
		
		System.out.println(evaluater.getAccuracy());
	}
}
