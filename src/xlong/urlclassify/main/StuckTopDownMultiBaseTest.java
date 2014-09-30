package xlong.urlclassify.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.TreeSet;

import weka.classifiers.Classifier;
import xlong.nlp.parser.BigramSegmentParser;
import xlong.nlp.parser.Parser;
import xlong.nlp.parser.TokenizeParser;
import xlong.nlp.parser.UnionParser;
import xlong.nlp.tokenizer.SingleWordTokenizer;
import xlong.nlp.tokenizer.Tokenizer;
import xlong.urlclassify.data.IO.UrlMapIO;
import xlong.util.PropertiesUtil;
import xlong.util.RunningTime;
import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.classifier.StuckAllPathMultiBaseClassifier;
import xlong.wm.classifier.StuckBeamSearchMultiBaseClassifier;
import xlong.wm.classifier.StuckPachinkoMultiBaseClassifier;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.evaluater.OntologySingleLabelEvaluater;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Labels;
import xlong.wm.sample.Sample;
import xlong.wm.sample.Text;
import xlong.wm.sample.Texts;
import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class StuckTopDownMultiBaseTest {

	public static void main(String[] args) throws Exception {
		// ----------------------------Data process---------------------------------
		// Get properties.
		LoadProperties.loadProperties();
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
		ClassifierPartsFactory factory = new ClassifierPartsFactory() {
			protected final Tokenizer tokenizer = new SingleWordTokenizer();
			@Override
			public TextToSparseVectorConverter getNewConverter() {
				return new TextToSparseVectorConverter(tokenizer)
					.enableLowerCaseToken()
					.enableStopwords()
					//.enableIDF()
					//.enableTF()
					.enableDetemineByDocFreq()
					.setMinTermFreq(2)
					.setFilterShortWords(1)
					.setIgnoreSmallFeatures(0)
					.setWordToKeep(100000)
					;
			}
			@Override
			public Classifier getNewClassifier() {
				//return new weka.classifiers.trees.RandomForest();
				return new weka.classifiers.bayes.NaiveBayesMultinomial();
			}
		};
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		//Parser urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new BigramSegmentParser(null));
		Parser segParser = new TokenizeParser(null, new SingleWordTokenizer(), new  BigramSegmentParser(null));
		Parser simpleParser = new TokenizeParser(null, new SingleWordTokenizer());
		Parser parser = new UnionParser(null, segParser, simpleParser);
		Parser urlParser = parser;

		Composite treeComposite, train, test;
		
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
//		treeComposite.save("result/treeParsed");	
		
		treeComposite = new Composite("result/treeParsed", new Texts());
		//treeComposite.inner2outer();
		System.out.println(treeComposite.countSample());
	
		
		for (int i = 4; i >= 0; i--) {			

			Vector<Composite> composites = treeComposite.split(new int[] {70, 30}, new Random(123));
			train = composites.get(0);
			System.out.println(train.countSample());
			test = composites.get(1);
			System.out.println(test.countSample());
			
			System.out.println("pachinko");
			{
				SingleLabelClassifier singleLabelClassifier = new StuckPachinkoMultiBaseClassifier(factory);
				RunningTime.start();
				singleLabelClassifier.train(train);
				RunningTime.stop();
				System.out.println("train time: " + RunningTime.get());
				
				OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
				RunningTime.start();
				evaluater.evaluate(test);	
				RunningTime.stop();
				System.out.println("test time: " + RunningTime.get());
				System.out.println("accuracy: " + evaluater.getAccuracy());
				System.out.println("hamming loss: " + evaluater.getAverHammingLoss());
				System.out.println("precision: " + evaluater.getAverPrecision());
				System.out.println("recall: " + evaluater.getAverRecall());
				System.out.println("f1: " + evaluater.getAverF1());
				
				BufferedWriter out = new BufferedWriter(new FileWriter("result/pachinkoEvaluation_" + (i+1)));
				evaluater.output(out);
				out.close();
			}
	
			System.out.println("beamSearch");
			{
				SingleLabelClassifier singleLabelClassifier =new StuckBeamSearchMultiBaseClassifier(factory, 5);
				RunningTime.start();
				singleLabelClassifier.train(train);
				RunningTime.stop();
				System.out.println("train time: " + RunningTime.get());
				
				OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
				RunningTime.start();
				evaluater.evaluate(test);	
				RunningTime.stop();
				System.out.println("test time: " + RunningTime.get());
				System.out.println("accuracy: " + evaluater.getAccuracy());
				System.out.println("hamming loss: " + evaluater.getAverHammingLoss());
				System.out.println("precision: " + evaluater.getAverPrecision());
				System.out.println("recall: " + evaluater.getAverRecall());
				System.out.println("f1: " + evaluater.getAverF1());
				
				BufferedWriter out = new BufferedWriter(new FileWriter("result/beamSearchEvaluation_" + (i+1)));
				evaluater.output(out);
				out.close();
			}
		
			
			System.out.println("allPath");
			{
				SingleLabelClassifier singleLabelClassifier = new StuckAllPathMultiBaseClassifier(factory);
				RunningTime.start();
				singleLabelClassifier.train(train);
				RunningTime.stop();
				System.out.println("train time: " + RunningTime.get());
				
				OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
				RunningTime.start();
				evaluater.evaluate(test);	
				RunningTime.stop();
				System.out.println("test time: " + RunningTime.get());
				System.out.println("accuracy: " + evaluater.getAccuracy());
				System.out.println("hamming loss: " + evaluater.getAverHammingLoss());
				System.out.println("precision: " + evaluater.getAverPrecision());
				System.out.println("recall: " + evaluater.getAverRecall());
				System.out.println("f1: " + evaluater.getAverF1());
				
				BufferedWriter out = new BufferedWriter(new FileWriter("result/allPathEvaluation_" + (i+1)));
				evaluater.output(out);
				out.close();
			}
			
			if (i > 0) {
				System.out.println();
				System.out.println("level " + i);
				treeComposite.flatComposite(i);
			}
		}
	}
}
