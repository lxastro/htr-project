package xlong.urlclassify.main;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import xlong.urlclassify.data.IO.UrlMapIO;
import xlong.util.MyWriter;
import xlong.util.PropertiesUtil;
import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.classifier.StuckPachinkoSVMClassifier;
import xlong.wm.evaluater.Evaluater;
import xlong.wm.evaluater.SingleLabelEvaluater;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Labels;
import xlong.wm.sample.Sample;
import xlong.wm.sample.Text;
import xlong.wm.sample.Texts;

public class FlatSVMTest {
	public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.showDefaultFile();
		PropertiesUtil.loadProperties();
		Composite train, test;
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		OntologyTree flatTree = tree.toFlatTree();
		
		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read("result/UrlMap.txt");
		System.out.println(urlMap.size());
	
		Composite flatComposite = new Composite(flatTree);
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			Sample sample = new Sample(new Text(en.getKey()), Labels.getLabels(en.getValue()));
			flatComposite.addSample(sample);
		}
		System.out.println(flatComposite.countSample());
		System.out.println(flatComposite.getComposites().size());
		flatComposite.cutBranch(1);
		System.out.println(flatComposite.countSample());
		System.out.println(flatComposite.getComposites().size());
		Vector<Composite> composites = flatComposite.split(new int[] {1, 99}, new Random(123));
		flatComposite = composites.firstElement();
		System.out.println(flatComposite.countSample());
		flatComposite.cutBranch(10);
		System.out.println(flatComposite.countSample());
		
		composites = flatComposite.split(new int[] {70, 30}, new Random(123));
		train = composites.get(0);
		System.out.println(train.countSample());
		train.save("result/trainText");	
		test = composites.get(1);
		System.out.println(test.countSample());
		test.save("result/testText");
		
		train = new Composite("result/trainText", new Texts());
		test = new Composite("result/testText", new Texts());
		
		SingleLabelClassifier singleLabelClassifier = new StuckPachinkoSVMClassifier();
		System.out.println("train");
		singleLabelClassifier.train(train);
		
		Evaluater evaluater = new SingleLabelEvaluater(singleLabelClassifier);
		System.out.println("test");
		MyWriter.setFile("result/evaluate", false);
		evaluater.evaluate(test);	
		MyWriter.close();
		System.out.println(evaluater.getAccuracy());
	}
}
