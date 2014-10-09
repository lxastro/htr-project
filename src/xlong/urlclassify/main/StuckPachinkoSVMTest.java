package xlong.urlclassify.main;

import java.util.Random;
import java.util.Vector;

import xlong.wm.evaluater.OntologySingleLabelEvaluater;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Texts;
import xlong.util.MyWriter;
import xlong.util.PropertiesUtil;
import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.classifier.StuckPachinkoSVMClassifier;

public class StuckPachinkoSVMTest {

	public static void main(String[] args) throws Exception {
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.showDefaultFile();
		PropertiesUtil.loadProperties();
		Composite treeComposite, train, test;
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		
		treeComposite = new Composite("result/treeParsed", new Texts());
		//treeComposite.treeComposite(1);
		System.out.println(treeComposite.countSample());
		System.out.println(treeComposite.getComposites().size());
		Vector<Composite> composites;
		
//		composites = treeComposite.split(new int[] {1, 90}, new Random(123));
//		treeComposite = composites.firstElement();
//		System.out.println(treeComposite.countSample());
//		treeComposite.cutBranch(10);
//		System.out.println(treeComposite.countSample());
		
		composites = treeComposite.split(new int[] {70, 30}, new Random(123));
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
		
		OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
		System.out.println("test");
		MyWriter.setFile("result/evaluate", false);
		evaluater.evaluate(test);	
		MyWriter.close();
		System.out.println(evaluater.getAccuracy());
		System.out.println("accuracy: " + evaluater.getAccuracy());
		System.out.println("hamming loss: " + evaluater.getAverHammingLoss());
		System.out.println("precision: " + evaluater.getAverPrecision());
		System.out.println("recall: " + evaluater.getAverRecall());
		System.out.println("f1: " + evaluater.getAverF1());
	}

}
