package xlong.main;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.TreeSet;

import xlong.classifier.SingleLabelClassifier;
import xlong.classifier.StuckAllPathNBMClassifier;
import xlong.classifier.StuckBeamSearchNBMClassifier;
import xlong.classifier.StuckPachinkoNBMClassifier;
import xlong.data.IO.UrlMapIO;
import xlong.evaluater.AccuracyEvaluater;
import xlong.evaluater.Evaluater;
import xlong.ontology.OntologyTree;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.Labels;
import xlong.sample.Text;
import xlong.sample.Texts;
import xlong.util.PropertiesUtil;
import xlong.util.RunningTime;

public class StuckTopDownNBMTest {

	public static void main(String[] args) throws Exception {
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.init();
		PropertiesUtil.loadProperties();
		Composite treeComposite, train, test;
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read("result/UrlMap.txt");
		System.out.println(urlMap.size());
	
		treeComposite = new Composite(tree);
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			String label = en.getValue().first();
			Sample sample = new Sample(new Text(en.getKey()), Labels.getLabels(tree.getPath(label)));
			treeComposite.addSample(sample);
		}
		System.out.println(treeComposite.countSample());
		treeComposite.inner2outer();
		System.out.println(treeComposite.countSample());
		treeComposite.cutBranch(1);
		treeComposite.save("result/treeAll");	
		
		treeComposite = new Composite("result/treeAll", new Texts());
		
		for (int i = 4; i >= 0; i--) {			

			Vector<Composite> composites = treeComposite.split(new int[] {70, 30}, new Random(123));
			train = composites.get(0);
			System.out.println(train.countSample());
			train.save("result/trainText");	
			test = composites.get(1);
			System.out.println(test.countSample());
			test.save("result/testText");
			
			train = new Composite("result/trainText", new Texts());
			test = new Composite("result/testText", new Texts());
			
			System.out.println("pachincko");
			for (int stemmingMode = 0; stemmingMode < 1; stemmingMode++) {
				SingleLabelClassifier singleLabelClassifier = new StuckPachinkoNBMClassifier(100000, stemmingMode);
				//System.out.println("mode " + stemmingMode);
				RunningTime.start();
				singleLabelClassifier.train(train);
				RunningTime.stop();
				System.out.println("train time: " + RunningTime.get());
				
				Evaluater evaluater = new AccuracyEvaluater(singleLabelClassifier);
				RunningTime.start();
				evaluater.evaluate(test);	
				RunningTime.stop();
				System.out.println("test time: " + RunningTime.get());
				System.out.println("accuracy: " + evaluater.getAccuracy());
			}
	
			System.out.println("beamSearch");
			for (int stemmingMode = 0; stemmingMode < 1; stemmingMode++) {
				SingleLabelClassifier singleLabelClassifier =new StuckBeamSearchNBMClassifier(100000, stemmingMode, 5);
				//System.out.println("mode " + stemmingMode);
				RunningTime.start();
				singleLabelClassifier.train(train);
				RunningTime.stop();
				System.out.println("train time: " + RunningTime.get());
				
				Evaluater evaluater = new AccuracyEvaluater(singleLabelClassifier);
				RunningTime.start();
				evaluater.evaluate(test);	
				RunningTime.stop();
				System.out.println("test time: " + RunningTime.get());
				System.out.println("accuracy: " + evaluater.getAccuracy());
			}
			
			System.out.println("allPath");
			for (int stemmingMode = 0; stemmingMode < 1; stemmingMode++) {
				SingleLabelClassifier singleLabelClassifier = new StuckAllPathNBMClassifier(100000, stemmingMode);
				//System.out.println("mode " + stemmingMode);
				RunningTime.start();
				singleLabelClassifier.train(train);
				RunningTime.stop();
				System.out.println("train time: " + RunningTime.get());
				
				Evaluater evaluater = new AccuracyEvaluater(singleLabelClassifier);
				RunningTime.start();
				evaluater.evaluate(test);	
				RunningTime.stop();
				System.out.println("test time: " + RunningTime.get());
				System.out.println("accuracy: " + evaluater.getAccuracy());
			}
			
			if (i > 0) {
				System.out.println();
				System.out.println("level " + i);
				treeComposite.flatComposite(i);
			}
		}
	}
}
