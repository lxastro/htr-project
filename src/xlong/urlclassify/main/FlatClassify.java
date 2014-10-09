package xlong.urlclassify.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import weka.classifiers.Classifier;
import xlong.nlp.parser.BigramSegmentParser;
import xlong.nlp.parser.Parser;
import xlong.nlp.parser.SnowballStemParser;
import xlong.nlp.parser.TokenizeParser;
import xlong.nlp.parser.UnionParser;
import xlong.nlp.tokenizer.SingleWordTokenizer;
import xlong.nlp.tokenizer.Tokenizer;
import xlong.urlclassify.data.IO.UrlMapIO;
import xlong.util.PropertiesUtil;
import xlong.util.RunningTime;
import xlong.wm.classifier.SingleLabelClassifier;
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

public class FlatClassify {

	public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		LoadProperties.loadProperties();
		Composite train, test;
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		OntologyTree flatTree = tree.toFlatTree();
		BigramSegmentParser.setWeigth(1);
		Parser segParser = new TokenizeParser(null, new SingleWordTokenizer(), new  BigramSegmentParser(null));
		Parser simpleParser = new TokenizeParser(null, new SingleWordTokenizer());
		Parser stemParser = new TokenizeParser(null, new SingleWordTokenizer(), new  BigramSegmentParser(null, new SnowballStemParser(null)));
		Parser parser = new UnionParser(null, segParser, simpleParser);
		Parser urlParser = parser;
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
					.enableIDF()
					//.enableTF()
					.enableDetemineByDocFreq()
					.setMinTermFreq(2)
					.setFilterShortWords(1)
					.setIgnoreSmallFeatures(0)
					//.setWordToKeep(50000)
					;
			}
			@Override
			public Classifier getNewClassifier() {
				//return new weka.classifiers.trees.Id3();
				//return new weka.classifiers.trees.J48();
				//return new weka.classifiers.trees.RandomForest();
				return new weka.classifiers.bayes.NaiveBayesMultinomial();
			}
		};
		Composite flatComposite = new Composite(flatTree);
		
//		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read("result/UrlMap.txt");
//		System.out.println(urlMap.size());
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
//		flatComposite.cutBranch(1);
//		System.out.println(flatComposite.getComposites().size());
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
		
		BufferedWriter out = new BufferedWriter(new FileWriter("result/flatEvaluation"));
		evaluater.output(out);
		out.close();
	}
}
