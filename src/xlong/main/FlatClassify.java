package xlong.main;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import org.omg.CORBA.PRIVATE_MEMBER;

import xlong.classifier.SingleLabelClassifier;
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

public class FlatClassify {

	public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.init();
		PropertiesUtil.loadProperties();
		Composite train, test;
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		OntologyTree flatTree = tree.toFlatTree();
		Parser urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new BigramSegmentParser(null));
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
		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read("result/UrlMap.txt");
		System.out.println(urlMap.size());
	
		Composite flatComposite = new Composite(flatTree);
//		int cnt = 0;
//		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
//			cnt ++;
//			if (cnt % 100000 == 0) {
//				System.out.println(cnt);
//			}
//			Sample sample = new Sample(new Text(urlParser.parse(en.getKey())), Labels.getLabels(en.getValue()));
//			flatComposite.addSample(sample);
//		}
//		System.out.println(flatComposite.countSample());
//		System.out.println(flatComposite.getComposites().size());
//		flatComposite.cutBranch(1);
//		flatComposite.save("result/flatParsed");
		flatComposite = new Composite("result/flatParsed" ,new Texts());
//		System.out.println(flatComposite.countSample());
//		System.out.println(flatComposite.getComposites().size());
		
		Vector<Composite> composites = flatComposite.split(new int[] {70, 30}, new Random(123));
		train = composites.get(0);
		System.out.println(train.countSample());
		train.save("result/trainFlatText");	
		test = composites.get(1);
		System.out.println(test.countSample());
		test.save("result/testFlatText");
		
		train = new Composite("result/trainFlatText", new Texts());
		test = new Composite("result/testFlatText", new Texts());
		
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
}
