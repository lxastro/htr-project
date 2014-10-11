package xlong.urlclassify.main;

import java.util.Random;
import java.util.Vector;

import weka.classifiers.Classifier;
import xlong.wm.evaluater.OntologySingleLabelEvaluater;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Texts;
import xlong.wm.sample.converter.TextToSparseVectorConverter;
import xlong.nlp.tokenizer.SingleWordTokenizer;
import xlong.nlp.tokenizer.Tokenizer;
import xlong.util.MyWriter;
import xlong.util.PropertiesUtil;
import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.classifier.StuckPachinkoSVMClassifier;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;

public class StuckPachinkoSVMTest {

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
					//.setWordToKeep(100000)
					;
			}
			@Override
			public Classifier getNewClassifier() {
				weka.classifiers.Classifier classifier = new xlong.urlclassify.others.LibLINEAR(); //weka 3-7
				//weka.classifiers.Classifier classifier = new weka.classifiers.functions.LibSVM(); //weka 3-6
				//weka.classifiers.Classifier classifier = new weka.classifiers.functions.SMO();
				//classifier.setOptions(weka.core.Utils.splitOptions(OPTION));	
				return classifier;
			}
		};	
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		
		Composite treeComposite, train, test;
		treeComposite = new Composite("result/flatParsed", new Texts());
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
		
		SingleLabelClassifier singleLabelClassifier = new StuckPachinkoSVMClassifier(factory);
		System.out.println("train");
		singleLabelClassifier.train(train);
		
		OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
		System.out.println("test");
		MyWriter.setFile("result/evaluate", false);
		evaluater.evaluate(test);	
		MyWriter.close();
		System.out.println("accuracy: " + evaluater.getAccuracy());
		System.out.println("hamming loss: " + evaluater.getAverHammingLoss());
		System.out.println("precision: " + evaluater.getAverPrecision());
		System.out.println("recall: " + evaluater.getAverRecall());
		System.out.println("f1: " + evaluater.getAverF1());
	}

}
