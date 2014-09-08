package xlong.main;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.TreeSet;

import weka.core.Instances;
import xlong.classifier.Classifier;
import xlong.classifier.FlatSingleLabelMultiClassClassifier;
import xlong.classifier.TextFlatSingleLabelMultiClassClassifier;
import xlong.classifier.converter.SparseVectorSampleToWekaInstanceConverter;
import xlong.data.Entity;
import xlong.data.IO.NTripleReader;
import xlong.data.IO.UrlEntityMapIO;
import xlong.data.IO.UrlMapIO;
import xlong.data.filter.ExistTypeFilter;
import xlong.data.filter.ExistUrlFilter;
import xlong.data.processer.SimplifyProcesser;
import xlong.data.processer.Triple2PairProcesser;
import xlong.data.processer.UrlNormalizeProcesser;
import xlong.ontology.OntologyTree;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.Labels;
import xlong.sample.SparseVectors;
import xlong.sample.Text;
import xlong.sample.Texts;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordTokenizer;
import xlong.util.MyWriter;
import xlong.util.PropertiesUtil;

public class Classify {
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
		TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer(), 1000);
		
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
		
//		Classifier classifier = new TextFlatSingleLabelMultiClassClassifier(Labels.cntLabel());
//		classifier.train(flatComposite);
//		double[] result = classifier.getDistribution(flatComposite.getSamples().firstElement());
//		for (double i:result) {
//			System.out.println(i);
//		}	
		
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
		System.out.println(flatComposite.countSample());
		System.out.println(converter.getDictionary().size());
		
		Classifier classifier = new FlatSingleLabelMultiClassClassifier(converter.getDictionary().size(), Labels.cntLabel());
		classifier.train(flatComposite);
		
		double[] result = classifier.getDistribution(flatComposite.getSamples().firstElement());
		System.out.println(flatComposite.getSamples().firstElement().getLabels().iterator().next());
		int i = 0;
		for (double d:result) {
			if (d > 0.01) {
				System.out.println(i + " : " + d);
			}
			i++;
		}
	}
}
