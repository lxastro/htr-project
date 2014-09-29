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
import xlong.data.parser.BigramSegmentParser;
import xlong.data.parser.Parser;
import xlong.data.parser.TokenizeParser;
import xlong.data.tokenizer.SingleWordTokenizer;
import xlong.evaluater.AccuracyEvaluater;
import xlong.evaluater.Evaluater;
import xlong.ontology.OntologyTree;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.Labels;
import xlong.sample.Text;
import xlong.sample.Texts;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.util.PropertiesUtil;
import xlong.util.RunningTime;

public class StuckTopDownNBMTest {

	public static void main(String[] args) throws Exception {
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.init();
		PropertiesUtil.loadProperties();
		String[] stopWordsFiles = new String[] {
			"E:/longx/data/stop-words/stop-words_english_1_en.txt",
			"E:/longx/data/stop-words/stop-words_english_2_en.txt",
			"E:/longx/data/stop-words/stop-words_english_3_en.txt",
			"E:/longx/data/stop-words/stop-words_english_4_google_en.txt",
			"E:/longx/data/stop-words/stop-words_english_5_en.txt",
			"E:/longx/data/stop-words/stop-words_english_6_en.txt",
			"E:/longx/data/lxdata/stopwords1.txt",
			"E:/longx/data/lxdata/stopwords2.txt",
			"E:/longx/data/lxdata/stopwordsMySQL.txt",
			"E:/longx/data/lxdata/stopwordsUrl.txt",
		};
		for (String stopWordsFile:stopWordsFiles) {
			TextToSparseVectorConverter.addStopwords(stopWordsFile);
		}	
		Composite treeComposite, train, test;
		
//		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
//		OntologyTree tree = OntologyTree.getTree(ontologyFile);
//		Parser urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new BigramSegmentParser(null));

//		
//		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read("result/UrlMap.txt");
//		System.out.println(urlMap.size());
//		
//		treeComposite = new Composite(tree);
//		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
//			String label = en.getValue().first(); 
//			Sample sample = new Sample(new Text(urlParser.parse(en.getKey())), Labels.getLabels(tree.getPath(label)));
//			treeComposite.addSample(sample);
//		}
//		System.out.println(treeComposite.countSample());
//		treeComposite.cutBranch(1);
//		treeComposite.save("result/treeAll");	
		
		treeComposite = new Composite("result/treeAll", new Texts());
		//treeComposite.inner2outer();
		System.out.println(treeComposite.countSample());
	
		
		for (int i = 4; i >= 0; i--) {			

			Vector<Composite> composites = treeComposite.split(new int[] {70, 30}, new Random(123));
			train = composites.get(0);
			System.out.println(train.countSample());
			test = composites.get(1);
			System.out.println(test.countSample());
			
			System.out.println("pachincko");
			{
				SingleLabelClassifier singleLabelClassifier = new StuckPachinkoNBMClassifier();
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
			{
				SingleLabelClassifier singleLabelClassifier =new StuckBeamSearchNBMClassifier(5);;
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
			{
				SingleLabelClassifier singleLabelClassifier = new StuckAllPathNBMClassifier();
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
